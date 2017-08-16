/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.xran.controller;

import com.google.common.collect.Sets;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.felix.scr.annotations.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.config.*;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.*;
import org.onosproject.xran.codecs.pdu.*;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibLink;
import org.onosproject.xran.entities.RnibUe;
import org.onosproject.xran.identifiers.LinkId;
import org.onosproject.xran.impl.XranConfig;
import org.onosproject.xran.providers.XranDeviceListener;
import org.onosproject.xran.providers.XranHostListener;
import org.onosproject.xran.wrapper.CellMap;
import org.onosproject.xran.wrapper.LinkMap;
import org.onosproject.xran.wrapper.UeMap;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

import static org.onosproject.net.DeviceId.deviceId;
import static org.onosproject.xran.controller.XranChannelHandler.getSctpMessage;
import static org.onosproject.xran.entities.RnibCell.decodeDeviceId;
import static org.onosproject.xran.entities.RnibCell.uri;
import static org.onosproject.xran.entities.RnibUe.hostIdtoMME;

/**
 * Created by dimitris on 7/20/17.
 */
@Component(immediate = true)
@Service
public class XranControllerImpl implements XranController {
    private static final String XRAN_APP_ID = "org.onosproject.xran";
    private static final Class<XranConfig> CONFIG_CLASS = XranConfig.class;

    private static final Logger log =
            LoggerFactory.getLogger(XranControllerImpl.class);
    /* CONFIG */
    private final InternalNetworkConfigListener configListener =
            new InternalNetworkConfigListener();
    /* VARIABLES */
    private final Controller controller = new Controller();
    private XranConfig xranConfig;
    private ApplicationId appId;
    /* Services */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private HostService hostService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetworkConfigRegistry registry;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetworkConfigService configService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private XranStore xranStore;
    private ConfigFactory<ApplicationId, XranConfig> xranConfigFactory =
            new ConfigFactory<ApplicationId, XranConfig>(
                    SubjectFactories.APP_SUBJECT_FACTORY, CONFIG_CLASS, "xran") {
                @Override
                public XranConfig createConfig() {
                    return new XranConfig();
                }
            };
    /* WRAPPERS */
    private CellMap cellMap;
    private UeMap ueMap;
    private LinkMap linkMap;
    /* MAPS */
    private ConcurrentMap<String, ECGI> legitCells = new ConcurrentHashMap<>();
    private ConcurrentMap<CRNTI, UEContextUpdate> hoContextUpdateMap = new ConcurrentHashMap<>();
    private ConcurrentMap<CRNTI, SynchronousQueue<String>> hoQueue = new ConcurrentHashMap<>();
    private ConcurrentMap<ECGI, SynchronousQueue<String>> RRMCellQueue = new ConcurrentHashMap<>();
    private ConcurrentMap<CRNTI, SynchronousQueue<String>> scellAddQueue = new ConcurrentHashMap<>();
    private ConcurrentMap<CRNTI, SynchronousQueue<String>> scellDeleteQueue = new ConcurrentHashMap<>();
    /* AGENTS */
    private InternalXranDeviceAgent deviceAgent = new InternalXranDeviceAgent();
    private InternalXranHostAgent hostAgent = new InternalXranHostAgent();
    private InternalXranPacketAgent packetAgent = new InternalXranPacketAgent();
    /* LISTENERS */
    private Set<XranDeviceListener> xranDeviceListeners = new CopyOnWriteArraySet<>();
    private Set<XranHostListener> xranHostListeners = new CopyOnWriteArraySet<>();
    private InternalDeviceListener device_listener = new InternalDeviceListener();
    private InternalHostListener host_listener = new InternalHostListener();

    @Activate
    public void activate() {
        appId = coreService.registerApplication(XRAN_APP_ID);

        configService.addListener(configListener);
        registry.registerConfigFactory(xranConfigFactory);
        deviceService.addListener(device_listener);
        hostService.addListener(host_listener);

        cellMap = new CellMap(xranStore);
        ueMap = new UeMap(xranStore);
        linkMap = new LinkMap(xranStore);

        xranStore.setController(this);

        log.info("XRAN Controller Started");
    }

    @Deactivate
    public void deactivate() {
        controller.stop();

        deviceService.removeListener(device_listener);
        hostService.removeListener(host_listener);

        legitCells.clear();

        configService.removeListener(configListener);
        registry.unregisterConfigFactory(xranConfigFactory);

        log.info("XRAN Controller Stopped");
    }

    @Override
    public SynchronousQueue<String> sendHORequest(RnibLink newLink, RnibLink oldLink) {
        ECGI newEcgi = newLink.getLinkId().getEcgi(),
                oldEcgi = oldLink.getLinkId().getEcgi();
        CRNTI crnti = linkMap.getCrnti(newLink.getLinkId().getMmeues1apid());
        ChannelHandlerContext newCtx = cellMap.getCtx(newEcgi),
                oldCtx = cellMap.getCtx(oldEcgi);

        try {
            XrancPdu xrancPdu = HORequest.constructPacket(crnti, oldEcgi, newEcgi);
            newCtx.writeAndFlush(getSctpMessage(xrancPdu));
            oldCtx.writeAndFlush(getSctpMessage(xrancPdu));
        } catch (IOException e) {
            e.printStackTrace();
        }

        SynchronousQueue<String> queue = new SynchronousQueue<>();
        hoQueue.put(crnti, queue);

        return queue;
    }

    @Override
    public void addListener(XranDeviceListener listener) {
        xranDeviceListeners.add(listener);
    }

    @Override
    public void addListener(XranHostListener listener) {
        xranHostListeners.add(listener);
    }

    @Override
    public void removeListener(XranDeviceListener listener) {
        xranDeviceListeners.remove(listener);
    }

    @Override
    public void removeListener(XranHostListener listener) {
        xranHostListeners.remove(listener);
    }

    @Override
    public SynchronousQueue<String> sendModifiedRRMConf(RRMConfig rrmConfig, boolean xICIC) {
        ECGI ecgi = rrmConfig.getEcgi();
        ChannelHandlerContext ctx = cellMap.getCtx(ecgi);
        try {
            XrancPdu pdu;
            if (xICIC) {
                pdu = XICICConfig.constructPacket(rrmConfig);
            } else {
                pdu = RRMConfig.constructPacket(rrmConfig);
            }
            ctx.writeAndFlush(getSctpMessage(pdu));
        } catch (IOException e) {
            e.printStackTrace();
        }
        SynchronousQueue<String> queue = new SynchronousQueue<>();
        RRMCellQueue.put(ecgi, queue);

        return queue;
    }

    @Override
    public SynchronousQueue<String> sendScellAdd(RnibLink link) {
        RnibCell secondaryCell = link.getLinkId().getCell(),
                primaryCell = linkMap.getPrimaryCell(link.getLinkId().getUe());
        ECGI primaryEcgi = primaryCell.getEcgi();
        ChannelHandlerContext ctx = cellMap.getCtx(primaryEcgi);

        CRNTI crnti = linkMap.getCrnti(link.getLinkId().getMmeues1apid());

        CellConfigReport cellReport = secondaryCell.getConf();

        if (cellReport != null) {
            PCIARFCN pciarfcn = new PCIARFCN();
            pciarfcn.setPci(cellReport.getPci());
            pciarfcn.setEarfcnDl(cellReport.getEarfcnDl());

            PropScell propScell = new PropScell();
            propScell.setPciArfcn(pciarfcn);

            XrancPdu pdu = ScellAdd.constructPacket(primaryEcgi, crnti, propScell);
            try {
                ctx.writeAndFlush(getSctpMessage(pdu));
                SynchronousQueue<String> queue = new SynchronousQueue<>();
                scellAddQueue.put(crnti, queue);

                return queue;
            } catch (IOException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean sendScellDelete(RnibLink link) {
        RnibCell secondaryCell = link.getLinkId().getCell(),
                primaryCell = linkMap.getPrimaryCell(link.getLinkId().getUe());
        ECGI primaryEcgi = primaryCell.getEcgi();
        ChannelHandlerContext ctx = cellMap.getCtx(primaryEcgi);

        CRNTI crnti = linkMap.getCrnti(link.getLinkId().getMmeues1apid());

        CellConfigReport cellReport = secondaryCell.getConf();

        if (cellReport != null) {
            PCIARFCN pciarfcn = new PCIARFCN();
            pciarfcn.setPci(cellReport.getPci());
            pciarfcn.setEarfcnDl(cellReport.getEarfcnDl());

            XrancPdu pdu = ScellDelete.constructPacket(primaryEcgi, crnti, pciarfcn);

            try {
                ctx.writeAndFlush(getSctpMessage(pdu));
                link.setType(RnibLink.Type.NON_SERVING);
                return true;
            } catch (IOException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
            }
        }
        return false;
    }

    private void restartTimer(RnibUe ue) {
        Timer timer = new Timer();
        ue.setTimer(timer);
        log.info("Starting UE timer...");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ue.getState() == RnibUe.State.IDLE) {
                    hostAgent.removeConnectedHost(ue);
                    log.info("UE is removed after 10 seconds of IDLE");
                } else {
                    log.info("UE not removed cause its ACTIVE");
                }
            }
        }, 10000);
    }

    private void restartTimer(RnibLink link) {
        Timer timer = new Timer();
        link.setTimer(timer);
        log.info("Starting Link timer...");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LinkId linkId = link.getLinkId();
                xranStore.removeLink(linkId);
                log.info("Link is removed after not receiving Meas Reports for 10 seconds");
            }
        }, 10000);

    }

    class InternalDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            log.info("Device Event {}", event);
            switch (event.type()) {
                case DEVICE_ADDED: {
                    try {
                        ECGI ecgi = decodeDeviceId(event.subject().id());
                        RnibCell cell = cellMap.get(ecgi);
                        if (cell != null) {
                            Timer timer = new Timer();
                            timer.scheduleAtFixedRate(
                                    new TimerTask() {
                                        @Override
                                        public void run() {
                                            CellConfigReport conf = cell.getConf();
                                            if (conf == null) {
                                                try {
                                                    ChannelHandlerContext ctx = cellMap.getCtx(ecgi);
                                                    XrancPdu xrancPdu = CellConfigRequest.constructPacket(ecgi);
                                                    ctx.writeAndFlush(getSctpMessage(xrancPdu));
                                                } catch (IOException e) {
                                                    log.error(ExceptionUtils.getFullStackTrace(e));
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                // FIXME: maybe remove this map.
                                                cellMap.putPciArfcn(cell);
                                                try {
                                                    ChannelHandlerContext ctx = cellMap.
                                                            getCtx(ecgi);
                                                    XrancPdu xrancPdu = L2MeasConfig.constructPacket(ecgi, xranConfig.getL2MeasInterval());
                                                    cell.setMeasConfig(xrancPdu.getBody().getL2MeasConfig());
                                                    SctpMessage sctpMessage = getSctpMessage(xrancPdu);
                                                    ctx.writeAndFlush(sctpMessage);
                                                } catch (IOException e) {
                                                    log.error(ExceptionUtils.getFullStackTrace(e));
                                                    e.printStackTrace();
                                                }
                                                timer.cancel();
                                                timer.purge();
                                            }
                                        }
                                    },
                                    0,
                                    xranConfig.getConfigRequestInterval() * 1000
                            );
                        }
                    } catch (IOException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        e.printStackTrace();
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    class InternalHostListener implements HostListener {

        @Override
        public void event(HostEvent event) {
            log.info("Host Event {}", event);
            switch (event.type()) {
                case HOST_ADDED:
                case HOST_MOVED: {
                    RnibUe ue = ueMap.get(hostIdtoMME(event.subject().id()));
                    if (ue != null) {
                        ECGI ecgi_primary = linkMap.getPrimaryCell(ue).getEcgi();
                        RnibCell primary = cellMap.get(ecgi_primary);
                        ue.setMeasConfig(null);
                        if (primary != null) {
                            Timer timer = new Timer();
                            timer.scheduleAtFixedRate(
                                    new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (ue.getCapability() == null) {
                                                try {
                                                    ChannelHandlerContext ctx = cellMap.getCtx(primary.getEcgi());
                                                    XrancPdu xrancPdu = UECapabilityEnquiry.constructPacket(
                                                            primary.getEcgi(),
                                                            ue.getRanId());
                                                    ctx.writeAndFlush(getSctpMessage(xrancPdu));
                                                } catch (IOException e) {
                                                    log.warn(ExceptionUtils.getFullStackTrace(e));
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                if (ue.getMeasConfig() == null) {
                                                    try {
                                                        ChannelHandlerContext ctx = cellMap.getCtx(primary.getEcgi());
                                                        RXSigMeasConfig.MeasCells measCells = new RXSigMeasConfig.MeasCells();
                                                        xranStore.getCellNodes().forEach(cell -> {
                                                            CellConfigReport cellReport = cell.getConf();
                                                            if (cellReport != null) {
                                                                PCIARFCN pciarfcn = new PCIARFCN();
                                                                pciarfcn.setPci(cellReport.getPci());
                                                                pciarfcn.setEarfcnDl(cellReport.getEarfcnDl());
                                                                measCells.setPCIARFCN(pciarfcn);
                                                            }
                                                        });
                                                        XrancPdu xrancPdu = RXSigMeasConfig.constructPacket(
                                                                primary.getEcgi(),
                                                                ue.getRanId(),
                                                                measCells,
                                                                xranConfig.getRxSignalInterval()
                                                        );
                                                        ue.setMeasConfig(xrancPdu.getBody().getRXSigMeasConfig());
                                                        ctx.writeAndFlush(getSctpMessage(xrancPdu));
                                                    } catch (IOException e) {
                                                        log.warn(ExceptionUtils.getFullStackTrace(e));
                                                        e.printStackTrace();
                                                    }
                                                }
                                                timer.cancel();
                                                timer.purge();
                                            }
                                        }
                                    },
                                    0,
                                    xranConfig.getConfigRequestInterval() * 1000
                            );
                        }
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    public class InternalXranDeviceAgent implements XranDeviceAgent {

        private final Logger log = LoggerFactory.getLogger(InternalXranDeviceAgent.class);

        @Override
        public boolean addConnectedCell(String host, ChannelHandlerContext ctx) {
            ECGI ecgi = legitCells.get(host);

            if (ecgi == null) {
                log.error("Device is not a legit source; ignoring...");
            } else {
                log.info("Device exists in configuration; registering...");
                RnibCell storeCell = cellMap.get(ecgi);
                if (storeCell == null) {
                    storeCell = new RnibCell();
                    storeCell.setEcgi(ecgi);
                    cellMap.put(storeCell, ctx);

                    for (XranDeviceListener l : xranDeviceListeners) {
                        l.deviceAdded(storeCell);
                    }
                    return true;
                } else {
                    log.error("Device already registered; ignoring...");
                }
            }
            ctx.close();
            return false;
        }

        @Override
        public boolean removeConnectedCell(String host) {
            ECGI ecgi = legitCells.get(host);
            List<RnibLink> linksByECGI = xranStore.getLinksByECGI(ecgi);

            linksByECGI.forEach(rnibLink -> xranStore.removeLink(rnibLink.getLinkId()));

            if (cellMap.remove(ecgi)) {
                for (XranDeviceListener l : xranDeviceListeners) {
                    l.deviceRemoved(deviceId(uri(ecgi)));
                }
                return true;
            }
            return false;
        }
    }

    public class InternalXranHostAgent implements XranHostAgent {

        @Override
        public boolean addConnectedHost(RnibUe ue, RnibCell cell, ChannelHandlerContext ctx) {

            if (ueMap.get(ue.getMmeS1apId()) != null) {
                linkMap.putPrimaryLink(cell, ue);

                Set<ECGI> ecgiSet = xranStore.getLinksByUeId(ue.getMmeS1apId().longValue())
                        .stream()
                        .map(l -> l.getLinkId().getEcgi())
                        .collect(Collectors.toSet());

                for (XranHostListener l : xranHostListeners) {
                    l.hostAdded(ue, ecgiSet);
                }
                return true;
            } else {
                ueMap.put(ue);
                linkMap.putPrimaryLink(cell, ue);

                Set<ECGI> ecgiSet = Sets.newConcurrentHashSet();
                ecgiSet.add(cell.getEcgi());
                for (XranHostListener l : xranHostListeners) {
                    l.hostAdded(ue, ecgiSet);
                }
                return true;
            }

        }

        @Override
        public boolean removeConnectedHost(RnibUe ue) {
            List<RnibLink> links = xranStore.getLinksByUeId(ue.getMmeS1apId().longValue());
            links.forEach(rnibLink -> xranStore.removeLink(rnibLink.getLinkId()));
            if (ueMap.remove(ue.getMmeS1apId())) {
                for (XranHostListener l : xranHostListeners) {
                    l.hostRemoved(ue.getHostId());
                }
                return true;
            }
            return false;
        }
    }

    public class InternalXranPacketAgent implements XranPacketProcessor {
        @Override
        public void handlePacket(XrancPdu recv_pdu, ChannelHandlerContext ctx) throws IOException {
            XrancPdu send_pdu;

            int apiID = recv_pdu.getHdr().getApiId().intValue();
            log.debug("Received message: {}", recv_pdu);
            switch (apiID) {
                case 1: {
                    // Decode Cell config report.
                    CellConfigReport report = recv_pdu.getBody().getCellConfigReport();

                    ECGI ecgi = report.getEcgi();

                    RnibCell cell = xranStore.getCell(ecgi);
                    cell.setVersion(recv_pdu.getHdr().getVer().toString());
                    cell.setConf(report);

                    break;
                }
                case 2: {
                    // Decode UE Admission Request.
                    UEAdmissionRequest ueAdmissionRequest = recv_pdu.getBody().getUEAdmissionRequest();

                    ECGI ecgi = ueAdmissionRequest.getEcgi();
                    if (xranStore.getCell(ecgi) != null) {
                        CRNTI crnti = ueAdmissionRequest.getCrnti();
                        send_pdu = UEAdmissionResponse.constructPacket(ecgi, crnti, xranConfig.admissionFlag());
                        ctx.writeAndFlush(getSctpMessage(send_pdu));
                    } else {
                        log.warn("Could not find ECGI in registered cells: {}", ecgi);
                    }
                    break;
                }
                case 4: {
                    // Decode UE Admission Status.
                    UEAdmissionStatus ueAdmissionStatus = recv_pdu.getBody().getUEAdmissionStatus();

                    RnibUe ue = ueMap.get(ueAdmissionStatus.getCrnti());
                    if (ue != null) {
                        if (ueAdmissionStatus.getAdmEstStatus().value.intValue() == 0) {
                            ue.setState(RnibUe.State.ACTIVE);
                        } else {
                            ue.setState(RnibUe.State.IDLE);
                        }
                    }
                    break;
                }
                case 5: {
                    // Decode UE Admission Context Update.
                    UEContextUpdate ueContextUpdate = recv_pdu.getBody().getUEContextUpdate();

                    RnibUe ue = ueMap.get(ueContextUpdate.getMMEUES1APID());
                    if (ue != null && hoQueue.keySet().contains(ue.getRanId())) {
                        CRNTI crnti = ueContextUpdate.getCrnti();
                        hoContextUpdateMap.put(crnti, ueContextUpdate);
                        hoQueue.remove(ue.getRanId());
                    } else {
                        RnibCell cell = xranStore.getCell(ueContextUpdate.getEcgi());
                        if (ue == null) {
                            ue = new RnibUe();
                        }

                        ue.setMmeS1apId(ueContextUpdate.getMMEUES1APID());
                        ue.setEnbS1apId(ueContextUpdate.getENBUES1APID());
                        ue.setRanId(ueContextUpdate.getCrnti());

                        hostAgent.addConnectedHost(ue, cell, ctx);
                    }
                    break;
                }
                case 6: {
                    // Decode UE Reconfig_Ind.
                    UEReconfigInd ueReconfigInd = recv_pdu.getBody().getUEReconfigInd();
                    RnibUe ue = ueMap.get(ueReconfigInd.getCrntiOld());

                    if (ue != null) {
                        ue.setRanId(ueReconfigInd.getCrntiNew());
                    } else {
                        log.warn("Could not find UE with this CRNTI: {}", ueReconfigInd.getCrntiOld());
                    }
                    break;
                }
                case 7: {
                    // If xRANc wants to deactivate UE, we pass UEReleaseInd from xRANc to eNB.
                    // Decode UE Release_Ind.
                    UEReleaseInd ueReleaseInd = recv_pdu.getBody().getUEReleaseInd();
                    RnibUe ue = ueMap.get(ueReleaseInd.getCrnti());
                    if (ue != null) {
                        ue.setState(RnibUe.State.IDLE);
                        restartTimer(ue);
                    }
                    break;
                }
                case 8: {
                    // Decode Bearer Adm Request
                    BearerAdmissionRequest bearerAdmissionRequest = recv_pdu.getBody().getBearerAdmissionRequest();

                    ECGI ecgi = bearerAdmissionRequest.getEcgi();
                    CRNTI crnti = bearerAdmissionRequest.getCrnti();
                    ERABParams erabParams = bearerAdmissionRequest.getErabParams();
                    RnibLink link = linkMap.get(ecgi, crnti);
                    if (link != null) {
                        link.setBearerParameters(erabParams);
                    } else {
                        log.warn("Could not find link between {}-{}", ecgi, crnti);
                    }

                    BerInteger numErabs = bearerAdmissionRequest.getNumErabs();
                    // Encode and send Bearer Admission Response
                    send_pdu = BearerAdmissionResponse.constructPacket(ecgi, crnti, erabParams, numErabs, xranConfig.bearerFlag());
                    ctx.writeAndFlush(getSctpMessage(send_pdu));
                    break;
                }
                case 10: {
                    //Decode Bearer Admission Status
                    BearerAdmissionStatus bearerAdmissionStatus = recv_pdu.getBody().getBearerAdmissionStatus();
                    break;
//                    ECGI ecgi = bearerAdmissionStatus.getEcgi();
//                    CRNTI crnti = bearerAdmissionStatus.getCrnti();
//
//                    RnibLink link = linkMap.get(ecgi, crnti);
                }
                case 11: {
                    //Decode Bearer Release Ind
                    BearerReleaseInd bearerReleaseInd = recv_pdu.getBody().getBearerReleaseInd();

                    ECGI ecgi = bearerReleaseInd.getEcgi();
                    CRNTI crnti = bearerReleaseInd.getCrnti();
                    RnibLink link = linkMap.get(ecgi, crnti);

                    List<ERABID> erabidsRelease = bearerReleaseInd.getErabIds().getERABID();
                    List<ERABParamsItem> erabParamsItem = link.getBearerParameters().getERABParamsItem();

                    List<ERABParamsItem> unreleased = erabParamsItem
                            .stream()
                            .filter(item -> {
                                Optional<ERABID> any = erabidsRelease.stream().filter(id -> id.equals(item.getId())).findAny();
                                return !any.isPresent();
                            }).collect(Collectors.toList());

                    link.getBearerParameters().setERABParamsItem(new ArrayList<>(unreleased));
                    break;
                }
                case 13: {
                    HOFailure hoFailure = recv_pdu.getBody().getHOFailure();

                    try {
                        hoQueue.get(hoFailure.getCrnti())
                                .put("Hand Over Failed with cause: " + hoFailure.getCause());
                    } catch (InterruptedException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        e.printStackTrace();
                    } finally {
                        hoQueue.remove(hoFailure.getCrnti());
                    }
                    break;

                }
                case 14: {
                    HOComplete hoComplete = recv_pdu.getBody().getHOComplete();

                    RnibLink oldLink = linkMap.get(hoComplete.getEcgiS(), hoComplete.getCrntiNew()),
                            newLink = linkMap.get(hoComplete.getEcgiT(), hoComplete.getCrntiNew());

                    oldLink.setType(RnibLink.Type.NON_SERVING);
                    newLink.setType(RnibLink.Type.SERVING_PRIMARY);

                    try {
                        hoQueue.get(hoComplete.getCrntiNew())
                                .put("Hand Over Completed");
                    } catch (InterruptedException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        e.printStackTrace();
                    } finally {
                        hoQueue.remove(hoComplete.getCrntiNew());

                        UEContextUpdate ueContextUpdate = hoContextUpdateMap.get(hoComplete.getCrntiNew());

                        RnibUe ue = ueMap.get(ueContextUpdate.getMMEUES1APID());
                        RnibCell cell = xranStore.getCell(ueContextUpdate.getEcgi());
                        if (ueMap.get(ueContextUpdate.getMMEUES1APID()) == null) {
                            ue = new RnibUe();
                        }

                        ue.setMmeS1apId(ueContextUpdate.getMMEUES1APID());
                        ue.setEnbS1apId(ueContextUpdate.getENBUES1APID());
                        ue.setRanId(ueContextUpdate.getCrnti());

                        hostAgent.addConnectedHost(ue, cell, ctx);
                    }
                    break;
                }

                case 16: {
                    // Decode RX Sig Meas Report.
                    RXSigMeasReport rxSigMeasReport = recv_pdu.getBody().getRXSigMeasReport();
                    List<RXSigReport> rxSigReportList = rxSigMeasReport.getCellMeasReports().getRXSigReport();

                    if (!rxSigReportList.isEmpty()) {
                        rxSigReportList.forEach(rxSigReport -> {
                            RnibCell cell = cellMap.get(rxSigReport.getPciArfcn());
                            if (cell != null) {
                                ECGI ecgi = cell.getEcgi();
                                RnibLink link = linkMap.get(ecgi, rxSigMeasReport.getCrnti());
                                if (link == null) {
                                    log.warn("Could not find link between: {}-{} | Creating non-serving link..", ecgi, rxSigMeasReport.getCrnti());
                                    link = linkMap.putNonServingLink(cell, rxSigMeasReport.getCrnti());

                                    if (link != null) {
                                        restartTimer(link);
                                    }
                                }

                                if (link != null) {
                                    RSRQRange rsrq = rxSigReport.getRsrq();
                                    RSRPRange rsrp = rxSigReport.getRsrp();

                                    RnibLink.LinkQuality quality = link.getQuality();
                                    quality.setRsrp(rsrp.value.intValue() - 140);
                                    quality.setRsrq((rsrq.value.intValue() * 0.5) - 19.5);
                                }
                            } else {
                                log.warn("Could not find cell with PCI-ARFCN: {}", rxSigReport.getPciArfcn());
                            }
                        });
                    }
                    break;
                }
                case 18: {
                    RadioMeasReportPerUE radioMeasReportPerUE = recv_pdu.getBody().getRadioMeasReportPerUE();

                    List<RadioRepPerServCell> servCells = radioMeasReportPerUE.getRadioReportServCells().getRadioRepPerServCell();

                    servCells.forEach(servCell -> {
                        RnibCell cell = cellMap.get(servCell.getPciArfcn());
                        if (cell != null) {
                            RnibLink link = linkMap.get(cell.getEcgi(), radioMeasReportPerUE.getCrnti());
                            if (link != null) {
                                RadioRepPerServCell.CqiHist cqiHist = servCell.getCqiHist();
                                RnibLink.LinkQuality quality = link.getQuality();
                                quality.setCqiHist(cqiHist);

                                final double[] values = {0, 0, 0};
                                int i = 1;
                                cqiHist.getBerInteger().forEach(value -> {
                                    values[0] = Math.max(values[0], value.intValue());
                                    values[1] += i * value.intValue();
                                    values[2] += value.intValue();
                                });

                                quality.setCqiMode(values[0]);
                                quality.setCqiMean(values[1] / values[2]);

                            } else {
                                log.warn("Could not find link between: {}-{}", cell.getEcgi(), radioMeasReportPerUE.getCrnti());
                            }
                        } else {
                            log.warn("Could not find cell with PCI-ARFCN: {}", servCell.getPciArfcn());
                        }
                    });
                    break;
                }
                case 19: {
                    RadioMeasReportPerCell radioMeasReportPerCell = recv_pdu.getBody().getRadioMeasReportPerCell();
                    break;
                }
                case 20: {
                    SchedMeasReportPerUE schedMeasReportPerUE = recv_pdu.getBody().getSchedMeasReportPerUE();
                    List<SchedMeasRepPerServCell> servCells = schedMeasReportPerUE.getSchedReportServCells()
                            .getSchedMeasRepPerServCell();

                    servCells.forEach(servCell -> {
                        RnibCell cell = cellMap.get(servCell.getPciArfcn());
                        if (cell != null) {
                            RnibLink link = linkMap.get(cell.getEcgi(), schedMeasReportPerUE.getCrnti());
                            if (link != null) {
                                link.getQuality().setMcs_dl(servCell.getMcsDl());
                                link.getQuality().setMcs_ul(servCell.getMcsUl());

                                link.getResourceUsage().setDl(servCell.getPrbUsage().getPrbUsageDl());
                                link.getResourceUsage().setUl(servCell.getPrbUsage().getPrbUsageUl());
                            } else {
                                log.warn("Could not find link between: {}-{}", cell.getEcgi(),
                                        schedMeasReportPerUE.getCrnti());
                            }
                        } else {
                            log.warn("Could not find cell with PCI-ARFCN: {}", servCell.getPciArfcn());
                        }
                    });
                    break;
                }
                case 21: {
                    SchedMeasReportPerCell schedMeasReportPerCell = recv_pdu.getBody().getSchedMeasReportPerCell();
                    RnibCell cell = cellMap.get(schedMeasReportPerCell.getEcgi());
                    if (cell != null) {
                        cell.setPrimaryPrbUsage(schedMeasReportPerCell.getPrbUsagePcell());
                        cell.setSecondaryPrbUsage(schedMeasReportPerCell.getPrbUsageScell());
                        cell.setQci(schedMeasReportPerCell.getQciVals());
                    } else {
                        log.warn("Could not find cell with ECGI: {}", schedMeasReportPerCell.getEcgi());
                    }
                    break;
                }
                case 22: {
                    PDCPMeasReportPerUe pdcpMeasReportPerUe = recv_pdu.getBody().getPDCPMeasReportPerUe();

                    RnibLink link = linkMap.get(pdcpMeasReportPerUe.getEcgi(), pdcpMeasReportPerUe.getCrnti());
                    if (link != null) {
                        link.getPdcpThroughput().setDl(pdcpMeasReportPerUe.getThroughputDl());
                        link.getPdcpThroughput().setUl(pdcpMeasReportPerUe.getThroughputUl());
                        link.getPdcpPackDelay().setDl(pdcpMeasReportPerUe.getPktDelayDl());
                        link.getPdcpPackDelay().setUl(pdcpMeasReportPerUe.getPktDelayUl());
                    } else {
                        log.warn("Could not find link between: {}-{}", pdcpMeasReportPerUe.getEcgi(),
                                pdcpMeasReportPerUe.getCrnti());
                    }
                    break;
                }
                case 24: {
                    // Decode UE Capability Info
                    UECapabilityInfo capabilityInfo = recv_pdu.getBody().getUECapabilityInfo();

                    RnibUe ue = ueMap.get(capabilityInfo.getCrnti());
                    if (ue != null) {
                        ue.setCapability(capabilityInfo);
                    } else {
                        log.warn("Could not find UE with this CRNTI: {}", capabilityInfo.getCrnti());
                    }
                    break;
                }
                case 25: {
                    // Don't know what will invoke sending UE CAPABILITY ENQUIRY
                    // Encode and send UE CAPABILITY ENQUIRY
                    UECapabilityEnquiry ueCapabilityEnquiry = recv_pdu.getBody().getUECapabilityEnquiry();
                    XrancPdu xrancPdu = UECapabilityEnquiry.constructPacket(ueCapabilityEnquiry.getEcgi(), ueCapabilityEnquiry.getCrnti());
                    ctx.writeAndFlush(getSctpMessage(xrancPdu));
                    break;
                }
                case 27: {
                    //Decode ScellAddStatus
                    ScellAddStatus scellAddStatus = recv_pdu.getBody().getScellAddStatus();
                    try {
                        scellAddQueue.get(scellAddStatus.getCrnti()).put("Scell's status: " + scellAddStatus.getStatus());
                        if (scellAddStatus.getStatus().getBerEnum().get(0).value.intValue() == 0) {

                            scellAddStatus.getScellsInd().getPCIARFCN().forEach(
                                    pciarfcn -> {
                                        RnibCell cell = cellMap.get(pciarfcn);
                                        RnibLink link = linkMap.get(cell.getEcgi(), scellAddStatus.getCrnti());
                                        link.setType(RnibLink.Type.SERVING_SECONDARY_CA);
                                    }
                            );
                        } else {
                            log.error("Scell addition failed.");
                        }
                    } catch (InterruptedException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        e.printStackTrace();
                    } finally {
                        scellAddQueue.remove(scellAddStatus.getCrnti());
                    }
                    break;
                }
                // TODO: 28: ScellDelete

                case 30: {
                    // Decode RRMConfig Status
                    RRMConfigStatus rrmConfigStatus = recv_pdu.getBody().getRRMConfigStatus();
                    try {
                        RRMCellQueue.get(rrmConfigStatus.getEcgi())
                                .put("RRM Config's status: " + rrmConfigStatus.getStatus());
                    } catch (InterruptedException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        e.printStackTrace();
                    } finally {
                        RRMCellQueue.remove(rrmConfigStatus.getEcgi());
                    }
                    break;
                }
                //TODO Case 31: SeNBAdd 32: SeNBAddStatus 33: SeNBDelete
                case 34: {
                    TrafficSplitConfig trafficSplitConfig = recv_pdu.getBody().getTrafficSplitConfig();

                    List<TrafficSplitPercentage> splitPercentages = trafficSplitConfig.getTrafficSplitPercent().getTrafficSplitPercentage();

                    splitPercentages.forEach(trafficSplitPercentage -> {
                        RnibCell cell = cellMap.get(trafficSplitPercentage.getEcgi());
                        if (cell != null) {
                            RnibLink link = linkMap.get(cell.getEcgi(), trafficSplitConfig.getCrnti());
                            if (link != null) {
                                link.setTrafficPercent(trafficSplitPercentage);
                            } else {
                                log.warn("Could not find link between: {}-{}", cell.getEcgi(), trafficSplitConfig.getCrnti());
                            }
                        } else {
                            log.warn("Could not find cell with ECGI: {}", trafficSplitConfig.getEcgi());
                        }
                    });
                    break;
                }
                default: {
                    log.warn("Wrong API ID");
                    break;
                }
            }

        }
    }

    class InternalNetworkConfigListener implements NetworkConfigListener {

        @Override
        public void event(NetworkConfigEvent event) {
            switch (event.type()) {
                case CONFIG_REGISTERED:
                    break;
                case CONFIG_UNREGISTERED:
                    break;
                case CONFIG_ADDED:
                case CONFIG_UPDATED:
                    if (event.configClass() == CONFIG_CLASS) {
                        handleConfigEvent(event.config());
                    }
                    break;
                case CONFIG_REMOVED:
                    break;
                default:
                    break;
            }
        }

        private void handleConfigEvent(Optional<Config> config) {
            if (!config.isPresent()) {
                return;
            }

            xranConfig = (XranConfig) config.get();

            legitCells.putAll(xranConfig.activeCellSet());

            controller.start(deviceAgent, hostAgent, packetAgent, xranConfig.getXrancPort());
        }
    }
}

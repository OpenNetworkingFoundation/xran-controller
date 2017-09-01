/*
 * Copyright 2015-present Open Networking Foundation
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
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.config.Config;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.CRNTI;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.ERABID;
import org.onosproject.xran.codecs.api.ERABParams;
import org.onosproject.xran.codecs.api.ERABParamsItem;
import org.onosproject.xran.codecs.api.PCIARFCN;
import org.onosproject.xran.codecs.api.PropScell;
import org.onosproject.xran.codecs.api.RSRPRange;
import org.onosproject.xran.codecs.api.RSRQRange;
import org.onosproject.xran.codecs.api.RXSigReport;
import org.onosproject.xran.codecs.api.RadioRepPerServCell;
import org.onosproject.xran.codecs.api.SchedMeasRepPerServCell;
import org.onosproject.xran.codecs.api.TrafficSplitPercentage;
import org.onosproject.xran.codecs.ber.types.BerInteger;
import org.onosproject.xran.codecs.pdu.BearerAdmissionRequest;
import org.onosproject.xran.codecs.pdu.BearerAdmissionResponse;
import org.onosproject.xran.codecs.pdu.BearerAdmissionStatus;
import org.onosproject.xran.codecs.pdu.BearerReleaseInd;
import org.onosproject.xran.codecs.pdu.CellConfigReport;
import org.onosproject.xran.codecs.pdu.CellConfigRequest;
import org.onosproject.xran.codecs.pdu.HOComplete;
import org.onosproject.xran.codecs.pdu.HOFailure;
import org.onosproject.xran.codecs.pdu.HORequest;
import org.onosproject.xran.codecs.pdu.L2MeasConfig;
import org.onosproject.xran.codecs.pdu.PDCPMeasReportPerUe;
import org.onosproject.xran.codecs.pdu.RRMConfig;
import org.onosproject.xran.codecs.pdu.RRMConfigStatus;
import org.onosproject.xran.codecs.pdu.RXSigMeasConfig;
import org.onosproject.xran.codecs.pdu.RXSigMeasReport;
import org.onosproject.xran.codecs.pdu.RadioMeasReportPerCell;
import org.onosproject.xran.codecs.pdu.RadioMeasReportPerUE;
import org.onosproject.xran.codecs.pdu.ScellAdd;
import org.onosproject.xran.codecs.pdu.ScellAddStatus;
import org.onosproject.xran.codecs.pdu.ScellDelete;
import org.onosproject.xran.codecs.pdu.SchedMeasReportPerCell;
import org.onosproject.xran.codecs.pdu.SchedMeasReportPerUE;
import org.onosproject.xran.codecs.pdu.TrafficSplitConfig;
import org.onosproject.xran.codecs.pdu.UEAdmissionRequest;
import org.onosproject.xran.codecs.pdu.UEAdmissionResponse;
import org.onosproject.xran.codecs.pdu.UEAdmissionStatus;
import org.onosproject.xran.codecs.pdu.UECapabilityEnquiry;
import org.onosproject.xran.codecs.pdu.UECapabilityInfo;
import org.onosproject.xran.codecs.pdu.UEContextUpdate;
import org.onosproject.xran.codecs.pdu.UEReconfigInd;
import org.onosproject.xran.codecs.pdu.UEReleaseInd;
import org.onosproject.xran.codecs.pdu.XICICConfig;
import org.onosproject.xran.codecs.pdu.XrancPdu;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibLink;
import org.onosproject.xran.entities.RnibUe;
import org.onosproject.xran.identifiers.ContextUpdateHandler;
import org.onosproject.xran.identifiers.EcgiCrntiPair;
import org.onosproject.xran.identifiers.LinkId;
import org.onosproject.xran.impl.XranConfig;
import org.onosproject.xran.providers.XranDeviceListener;
import org.onosproject.xran.providers.XranHostListener;
import org.onosproject.xran.wrapper.CellMap;
import org.onosproject.xran.wrapper.LinkMap;
import org.onosproject.xran.wrapper.UeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

import static org.onosproject.net.DeviceId.deviceId;
import static org.onosproject.xran.controller.XranChannelHandler.getSctpMessage;
import static org.onosproject.xran.entities.RnibCell.decodeDeviceId;
import static org.onosproject.xran.entities.RnibCell.uri;
import static org.onosproject.xran.entities.RnibUe.hostIdtoUEId;

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
    private int northboundTimeout;
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
    private ConcurrentMap<IpAddress, ECGI> legitCells = new ConcurrentHashMap<>();
    private ConcurrentMap<ECGI, SynchronousQueue<String>> hoMap = new ConcurrentHashMap<>();
    private ConcurrentMap<ECGI, SynchronousQueue<String>> rrmcellMap = new ConcurrentHashMap<>();
    private ConcurrentMap<CRNTI, SynchronousQueue<String>> scellAddMap = new ConcurrentHashMap<>();
    // Map used to keep messages in pairs (HO Complete - CTX Update, Adm Status - CTX Update)
    private ConcurrentMap<EcgiCrntiPair, ContextUpdateHandler> contextUpdateMap = new ConcurrentHashMap<>();
    /* QUEUE */
    private BlockingQueue<Long> ueIdQueue = new LinkedBlockingQueue<>();
    /* AGENTS */
    private InternalXranDeviceAgent deviceAgent = new InternalXranDeviceAgent();
    private InternalXranHostAgent hostAgent = new InternalXranHostAgent();
    private InternalXranPacketAgent packetAgent = new InternalXranPacketAgent();
    /* LISTENERS */
    private Set<XranDeviceListener> xranDeviceListeners = new CopyOnWriteArraySet<>();
    private Set<XranHostListener> xranHostListeners = new CopyOnWriteArraySet<>();
    private InternalDeviceListener deviceListener = new InternalDeviceListener();
    private InternalHostListener hostListener = new InternalHostListener();

    @Activate
    public void activate() {
        appId = coreService.registerApplication(XRAN_APP_ID);

        configService.addListener(configListener);
        registry.registerConfigFactory(xranConfigFactory);
        deviceService.addListener(deviceListener);
        hostService.addListener(hostListener);

        cellMap = new CellMap(xranStore);
        ueMap = new UeMap(xranStore);
        linkMap = new LinkMap(xranStore, ueMap);

        xranStore.setController(this);

        log.info("XRAN Controller Started");
    }

    @Deactivate
    public void deactivate() {
        controller.stop();

        deviceService.removeListener(deviceListener);
        hostService.removeListener(hostListener);

        legitCells.clear();

        configService.removeListener(configListener);
        registry.unregisterConfigFactory(xranConfigFactory);

        log.info("XRAN Controller Stopped");
    }

    @Override
    public SynchronousQueue<String> sendHORequest(RnibLink linkT, RnibLink linkS) throws InterruptedException {
        ECGI ecgiT = linkT.getLinkId().getEcgi(),
                ecgiS = linkS.getLinkId().getEcgi();

        CRNTI crnti = linkMap.getCrnti(linkT.getLinkId().getUeId());
        ChannelHandlerContext ctxT = cellMap.getCtx(ecgiT),
                ctxS = cellMap.getCtx(ecgiS);

        SynchronousQueue<String> queue = new SynchronousQueue<>();
        try {
            XrancPdu xrancPdu = HORequest.constructPacket(crnti, ecgiS, ecgiT);

            // temporary map that has ECGI source of a handoff to a queue waiting for REST response.
            hoMap.put(ecgiS, queue);

            ctxT.writeAndFlush(getSctpMessage(xrancPdu));
            ctxS.writeAndFlush(getSctpMessage(xrancPdu));

            // FIXME: only works for one HO at a time.
            ueIdQueue.put(linkT.getLinkId().getUeId());
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    public int getNorthboundTimeout() {
        return northboundTimeout;
    }

    @Override
    public SynchronousQueue<String> sendmodifiedrrmconf(RRMConfig rrmConfig, boolean xicic) {
        ECGI ecgi = rrmConfig.getEcgi();
        ChannelHandlerContext ctx = cellMap.getCtx(ecgi);
        try {
            XrancPdu pdu;

            if (xicic) {
                CellConfigReport cellConfigReport = cellMap.get(ecgi).getConf();
                if (cellConfigReport != null) {
                    pdu = XICICConfig.constructPacket(rrmConfig, cellConfigReport);
                    ctx.writeAndFlush(getSctpMessage(pdu));
                }
            } else {
                pdu = RRMConfig.constructPacket(rrmConfig);
                ctx.writeAndFlush(getSctpMessage(pdu));
                SynchronousQueue<String> queue = new SynchronousQueue<>();
                rrmcellMap.put(ecgi, queue);
                return queue;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public SynchronousQueue<String> sendScellAdd(RnibLink link) {
        RnibCell secondaryCell = link.getLinkId().getCell(),
                primaryCell = linkMap.getPrimaryCell(link.getLinkId().getUe());
        ECGI primaryEcgi = primaryCell.getEcgi();
        ChannelHandlerContext ctx = cellMap.getCtx(primaryEcgi);

        CRNTI crnti = linkMap.getCrnti(link.getLinkId().getUeId());

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
                scellAddMap.put(crnti, queue);

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

        CRNTI crnti = linkMap.getCrnti(link.getLinkId().getUeId());

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

    /**
     * Timer to delete UE after being IDLE.
     *
     * @param ue UE entity
     */
    private void restartTimer(RnibUe ue) {
        Timer timer = new Timer();
        ue.setTimer(timer);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ue.getState() == RnibUe.State.IDLE) {
                    hostAgent.removeConnectedHost(ue);
                    log.info("UE is removed after {} ms of IDLE", xranConfig.getIdleUeRemoval());
                } else {
                    log.info("UE not removed cause its ACTIVE");
                }
            }
        }, xranConfig.getIdleUeRemoval());
    }

    /**
     * Timer to delete LINK after not receiving measurements.
     *
     * @param link LINK entity
     */
    private void restartTimer(RnibLink link) {
        Timer timer = new Timer();
        link.setTimer(timer);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LinkId linkId = link.getLinkId();
                xranStore.removeLink(linkId);
                log.info("Link is removed after not receiving Meas Reports for {} ms",
                        xranConfig.getNoMeasLinkRemoval());
            }
        }, xranConfig.getNoMeasLinkRemoval());

    }

    /**
     * Request measurement configuration field of specified UE.
     *
     * @param primary primary CELL
     * @param ue      UE entity
     */
    private void populateMeasConfig(RnibCell primary, RnibUe ue) {
        try {
            ChannelHandlerContext ctx = cellMap.getCtx(primary.getEcgi());
            RXSigMeasConfig.MeasCells measCells = new RXSigMeasConfig.MeasCells();
            xranStore.getcellnodes().forEach(cell -> {
                CellConfigReport cellReport = ((RnibCell) cell).getConf();
                if (cellReport != null) {
                    PCIARFCN pciarfcn = new PCIARFCN();
                    pciarfcn.setPci(cellReport.getPci());
                    pciarfcn.setEarfcnDl(cellReport.getEarfcnDl());
                    measCells.setPCIARFCN(pciarfcn);
                }
            });
            XrancPdu xrancPdu = RXSigMeasConfig.constructPacket(
                    primary.getEcgi(),
                    ue.getCrnti(),
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

    /**
     * Internal device listener.
     */
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
                                                List<Object> ueNodes = xranStore.getuenodes();
                                                ueNodes.forEach(object -> {
                                                    RnibUe ue = (RnibUe) object;
                                                    try {
                                                        ECGI primaryEcgi = linkMap.getPrimaryCell(ue).getEcgi();
                                                        ChannelHandlerContext ctx = cellMap.getCtx(primaryEcgi);
                                                        RXSigMeasConfig.MeasCells measCells =
                                                                new RXSigMeasConfig.MeasCells();
                                                        xranStore.getcellnodes().forEach(cell -> {
                                                            CellConfigReport cellReport = ((RnibCell) cell).getConf();
                                                            if (cellReport != null) {
                                                                PCIARFCN pciarfcn = new PCIARFCN();
                                                                pciarfcn.setPci(cellReport.getPci());
                                                                pciarfcn.setEarfcnDl(cellReport.getEarfcnDl());
                                                                measCells.setPCIARFCN(pciarfcn);
                                                            }
                                                        });
                                                        XrancPdu xrancPdu = RXSigMeasConfig.constructPacket(
                                                                primaryEcgi,
                                                                ue.getCrnti(),
                                                                measCells,
                                                                xranConfig.getRxSignalInterval()
                                                        );
                                                        ue.setMeasConfig(xrancPdu.getBody().getRXSigMeasConfig());
                                                        ctx.writeAndFlush(getSctpMessage(xrancPdu));
                                                    } catch (IOException e) {
                                                        log.warn(ExceptionUtils.getFullStackTrace(e));
                                                        e.printStackTrace();
                                                    }
                                                });

                                                try {
                                                    ChannelHandlerContext ctx = cellMap.getCtx(ecgi);
                                                    XrancPdu xrancPdu = L2MeasConfig
                                                            .constructPacket(ecgi, xranConfig.getL2MeasInterval());
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

    /**
     * Internal host listener.
     */
    class InternalHostListener implements HostListener {

        @Override
        public void event(HostEvent event) {
            log.info("Host Event {}", event);
            switch (event.type()) {
                case HOST_ADDED:
                case HOST_MOVED: {
                    RnibUe ue = ueMap.get(hostIdtoUEId(event.subject().id()));
                    if (ue != null) {
                        ECGI ecgiPrimary = linkMap.getPrimaryCell(ue).getEcgi();
                        RnibCell primary = cellMap.get(ecgiPrimary);
                        ue.setMeasConfig(null);
                        if (primary != null) {
                            Timer timer = new Timer();
                            timer.scheduleAtFixedRate(
                                    new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (ue.getCapability() == null && primary.getVersion() >= 3) {
                                                try {
                                                    ChannelHandlerContext ctx = cellMap.getCtx(primary.getEcgi());
                                                    XrancPdu xrancPdu = UECapabilityEnquiry.constructPacket(
                                                            primary.getEcgi(),
                                                            ue.getCrnti());
                                                    ctx.writeAndFlush(getSctpMessage(xrancPdu));
                                                } catch (IOException e) {
                                                    log.warn(ExceptionUtils.getFullStackTrace(e));
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                timer.cancel();
                                                timer.purge();
                                            }
                                        }
                                    },
                                    0,
                                    xranConfig.getConfigRequestInterval() * 1000
                            );
                            if (ue.getMeasConfig() == null) {
                                populateMeasConfig(primary, ue);
                            }
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

    /**
     * Internal xran device agent.
     */
    public class InternalXranDeviceAgent implements XranDeviceAgent {

        private final Logger log = LoggerFactory.getLogger(InternalXranDeviceAgent.class);

        @Override
        public boolean addConnectedCell(String host, ChannelHandlerContext ctx) {
            ECGI ecgi = legitCells.get(IpAddress.valueOf(host));

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
            ECGI ecgi = legitCells.get(IpAddress.valueOf(host));
            List<RnibLink> linksbyecgi = xranStore.getlinksbyecgi(ecgi);

            linksbyecgi.forEach(rnibLink -> xranStore.removeLink(rnibLink.getLinkId()));

            if (cellMap.remove(ecgi)) {
                for (XranDeviceListener l : xranDeviceListeners) {
                    l.deviceRemoved(deviceId(uri(ecgi)));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Internal xran host agent.
     */
    public class InternalXranHostAgent implements XranHostAgent {

        @Override
        public boolean addConnectedHost(RnibUe ue, RnibCell cell, ChannelHandlerContext ctx) {

            if (ue.getId() != null && ueMap.get(ue.getId()) != null) {
                linkMap.putPrimaryLink(cell, ue);

                Set<ECGI> ecgiSet = Sets.newConcurrentHashSet();

                xranStore.getlinksbyueid(ue.getId())
                        .stream()
                        .filter(l -> l.getType().equals(RnibLink.Type.SERVING_PRIMARY))
                        .findFirst()
                        .ifPresent(l -> ecgiSet.add(l.getLinkId().getEcgi()));

                for (XranHostListener l : xranHostListeners) {
                    l.hostAdded(ue, ecgiSet);
                }
                return true;
            } else {
                ueMap.put(cell, ue);
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
            List<RnibLink> links = xranStore.getlinksbyueid(ue.getId());
            links.forEach(rnibLink -> xranStore.removeLink(rnibLink.getLinkId()));
            if (ueMap.remove(ue.getId())) {
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
        public void handlePacket(XrancPdu recvPdu, ChannelHandlerContext ctx)
                throws IOException, InterruptedException {
            XrancPdu sendPdu;

            int apiID = recvPdu.getHdr().getApiId().intValue();
            log.debug("Received message: {}", recvPdu);
            switch (apiID) {
                case 1: {
                    // Decode Cell config report.
                    CellConfigReport report = recvPdu.getBody().getCellConfigReport();
                    handleCellconfigreport(report, recvPdu.getHdr().getVer().toString());
                    break;
                }
                case 2: {
                    // Decode UE Admission Request.
                    UEAdmissionRequest ueAdmissionRequest = recvPdu.getBody().getUEAdmissionRequest();
                    handleUeadmissionrequest(ueAdmissionRequest, ctx);
                    break;
                }
                case 4: {
                    // Decode UE Admission Status.
                    UEAdmissionStatus ueAdmissionStatus = recvPdu.getBody().getUEAdmissionStatus();
                    handleAdmissionstatus(ueAdmissionStatus, ctx);
                    break;
                }
                case 5: {
                    // Decode UE Context Update.
                    UEContextUpdate ueContextUpdate = recvPdu.getBody().getUEContextUpdate();
                    handleUecontextupdate(ueContextUpdate, ctx);

                    break;
                }
                case 6: {
                    // Decode UE Reconfig_Ind.
                    UEReconfigInd ueReconfigInd = recvPdu.getBody().getUEReconfigInd();
                    handleUereconfigind(ueReconfigInd);
                    break;
                }
                case 7: {
                    // If xRANc wants to deactivate UE, we pass UEReleaseInd from xRANc to eNB.
                    // Decode UE Release_Ind.
                    UEReleaseInd ueReleaseInd = recvPdu.getBody().getUEReleaseInd();
                    handleUereleaseind(ueReleaseInd);
                    break;
                }
                case 8: {
                    // Decode Bearer Adm Request
                    BearerAdmissionRequest bearerAdmissionRequest = recvPdu.getBody().getBearerAdmissionRequest();
                    handleBeareradmissionrequest(bearerAdmissionRequest, ctx);
                    break;
                }
                case 10: {
                    //Decode Bearer Admission Status
                    BearerAdmissionStatus bearerAdmissionStatus = recvPdu.getBody().getBearerAdmissionStatus();
                    break;
                }
                case 11: {
                    //Decode Bearer Release Ind
                    BearerReleaseInd bearerReleaseInd = recvPdu.getBody().getBearerReleaseInd();
                    handleBearerreleaseind(bearerReleaseInd);
                    break;
                }
                case 13: {
                    HOFailure hoFailure = recvPdu.getBody().getHOFailure();
                    handleHofailure(hoFailure);
                    break;

                }
                case 14: {
                    HOComplete hoComplete = recvPdu.getBody().getHOComplete();
                    handleHocomplete(hoComplete, ctx);
                    break;
                }

                case 16: {
                    // Decode Rx Sig Meas Report.
                    RXSigMeasReport rxSigMeasReport = recvPdu.getBody().getRXSigMeasReport();
                    handleRxsigmeasreport(rxSigMeasReport);
                    break;
                }
                case 18: {
                    RadioMeasReportPerUE radioMeasReportPerUE = recvPdu.getBody().getRadioMeasReportPerUE();
                    handleRadionmeasreportperue(radioMeasReportPerUE);
                    break;
                }
                case 19: {
                    RadioMeasReportPerCell radioMeasReportPerCell = recvPdu.getBody().getRadioMeasReportPerCell();
                    break;
                }
                case 20: {
                    SchedMeasReportPerUE schedMeasReportPerUE = recvPdu.getBody().getSchedMeasReportPerUE();
                    handleSchedmeasreportperue(schedMeasReportPerUE);
                    break;
                }
                case 21: {
                    SchedMeasReportPerCell schedMeasReportPerCell = recvPdu.getBody().getSchedMeasReportPerCell();
                    handleSchedmeasreportpercell(schedMeasReportPerCell);
                    break;
                }
                case 22: {
                    PDCPMeasReportPerUe pdcpMeasReportPerUe = recvPdu.getBody().getPDCPMeasReportPerUe();
                    handlePdcpmeasreportperue(pdcpMeasReportPerUe);
                    break;
                }
                case 24: {
                    // Decode UE Capability Info
                    UECapabilityInfo capabilityInfo = recvPdu.getBody().getUECapabilityInfo();
                    handleCapabilityinfo(capabilityInfo);
                    break;
                }
                case 25: {
                    // Don't know what will invoke sending UE CAPABILITY ENQUIRY
                    // Encode and send UE CAPABILITY ENQUIRY
                    UECapabilityEnquiry ueCapabilityEnquiry = recvPdu.getBody().getUECapabilityEnquiry();
                    handleUecapabilityenquiry(ueCapabilityEnquiry, ctx);
                    break;
                }
                case 27: {
                    //Decode ScellAddStatus
                    ScellAddStatus scellAddStatus = recvPdu.getBody().getScellAddStatus();
                    handleScelladdstatus(scellAddStatus);
                    break;
                }
                case 30: {
                    // Decode RRMConfig Status
                    RRMConfigStatus rrmConfigStatus = recvPdu.getBody().getRRMConfigStatus();
                    handleRrmconfigstatus(rrmConfigStatus);
                    break;
                }
                //TODO Case 31: SeNBAdd 32: SeNBAddStatus 33: SeNBDelete
                case 34: {
                    TrafficSplitConfig trafficSplitConfig = recvPdu.getBody().getTrafficSplitConfig();
                    handleTrafficSplitConfig(trafficSplitConfig);
                    break;
                }
                default: {
                    log.warn("Wrong API ID: {}", recvPdu);
                    break;
                }
            }

        }

        /**
         * Handle Cellconfigreport.
         * @param report CellConfigReport
         * @param version String version ID
         */
        private void handleCellconfigreport(CellConfigReport report, String version) {
            ECGI ecgi = report.getEcgi();

            RnibCell cell = xranStore.getCell(ecgi);
            cell.setVersion(version);
            cell.setConf(report);
            cellMap.putPciArfcn(cell);
        }

        /**
         * Handle Ueadmissionrequest.
         * @param ueAdmissionRequest UEAdmissionRequest
         * @param ctx ChannelHandlerContext
         * @throws IOException IO Exception
         */
        private void handleUeadmissionrequest(UEAdmissionRequest ueAdmissionRequest, ChannelHandlerContext ctx)
                throws IOException {
            ECGI ecgi = ueAdmissionRequest.getEcgi();
            if (xranStore.getCell(ecgi) != null) {
                CRNTI crnti = ueAdmissionRequest.getCrnti();
                XrancPdu sendPdu = UEAdmissionResponse.constructPacket(ecgi, crnti, xranConfig.admissionFlag());
                ctx.writeAndFlush(getSctpMessage(sendPdu));
            } else {
                log.warn("Could not find ECGI in registered cells: {}", ecgi);
            }
        }

        /**
         * Handle UEAdmissionStatus.
         * @param ueAdmissionStatus UEAdmissionStatus
         * @param ctx ChannelHandlerContext
         */
        private void handleAdmissionstatus(UEAdmissionStatus ueAdmissionStatus, ChannelHandlerContext ctx) {
            RnibUe ue = ueMap.get(ueAdmissionStatus.getEcgi(), ueAdmissionStatus.getCrnti());
            if (ue != null) {
                if (ueAdmissionStatus.getAdmEstStatus().value.intValue() == 0) {
                    ue.setState(RnibUe.State.ACTIVE);
                } else {
                    ue.setState(RnibUe.State.IDLE);
                }
            }

            if (ueAdmissionStatus.getAdmEstStatus().value.intValue() == 0) {
                EcgiCrntiPair ecgiCrntiPair = EcgiCrntiPair
                        .valueOf(ueAdmissionStatus.getEcgi(), ueAdmissionStatus.getCrnti());
                contextUpdateMap.compute(ecgiCrntiPair, (k, v) -> {
                    if (v == null) {
                        v = new ContextUpdateHandler();
                    }
                    if (v.setAdmissionStatus(ueAdmissionStatus)) {
                        handlePairedPackets(v.getContextUpdate(), ctx, false);
                        v.reset();
                    }
                    return v;
                });
            }
        }

        /**
         * Handle UEContextUpdate.
         * @param ueContextUpdate UEContextUpdate
         * @param ctx ChannelHandlerContext
         */
        private void handleUecontextupdate(UEContextUpdate ueContextUpdate, ChannelHandlerContext ctx) {
            EcgiCrntiPair ecgiCrntiPair = EcgiCrntiPair
                    .valueOf(ueContextUpdate.getEcgi(), ueContextUpdate.getCrnti());

            contextUpdateMap.compute(ecgiCrntiPair, (k, v) -> {
                if (v == null) {
                    v = new ContextUpdateHandler();
                }
                if (v.setContextUpdate(ueContextUpdate)) {
                    HOComplete hoComplete = v.getHoComplete();
                    handlePairedPackets(ueContextUpdate, ctx, hoComplete != null);
                    if (hoComplete != null) {
                        try {
                            hoMap.get(hoComplete.getEcgiS()).put("Hand Over Completed");
                        } catch (InterruptedException e) {
                            log.error(ExceptionUtils.getFullStackTrace(e));
                            e.printStackTrace();
                        } finally {
                            hoMap.remove(hoComplete.getEcgiS());
                        }
                    }
                    v.reset();
                }
                return v;
            });
        }

        /**
         * Handle UEReconfigInd.
         * @param ueReconfigInd UEReconfigInd
         */
        private void handleUereconfigind(UEReconfigInd ueReconfigInd) {
            RnibUe ue = ueMap.get(ueReconfigInd.getEcgi(), ueReconfigInd.getCrntiOld());
            RnibCell cell = cellMap.get(ueReconfigInd.getEcgi());

            if (ue != null && cell != null) {
                ue.setCrnti(ueReconfigInd.getCrntiNew());
                ueMap.putCrnti(cell, ue);
            } else {
                log.warn("Could not find UE with this CRNTI: {}", ueReconfigInd.getCrntiOld());
            }
        }

        /**
         * Handle UEReleaseInd.
         * @param ueReleaseInd UEReleaseInd
         */
        private void handleUereleaseind(UEReleaseInd ueReleaseInd) {
            ECGI ecgi = ueReleaseInd.getEcgi();
            CRNTI crnti = ueReleaseInd.getCrnti();
            RnibUe ue = ueMap.get(ecgi, crnti);

            // Check if there is an ongoing handoff and only remove if ue is not part of the handoff.
            Long peek = ueIdQueue.peek();
            if (peek != null) {
                EcgiCrntiPair ecgiCrntiPair = ueMap.getCrntUe().inverse().get(peek);
                if (ecgiCrntiPair != null && ecgiCrntiPair.equals(EcgiCrntiPair.valueOf(ecgi, crnti))) {
                    return;
                }
            }

            if (ue != null) {
                ue.setState(RnibUe.State.IDLE);
                restartTimer(ue);
            } else {
                log.warn("Cannot release UE from non primary link.");
            }
        }

        /**
         * Handle BearerAdmissionRequest.
         * @param bearerAdmissionRequest BearerAdmissionRequest
         * @param ctx ChannelHandlerContext
         * @throws IOException IO Exception
         */
        private void handleBeareradmissionrequest(BearerAdmissionRequest bearerAdmissionRequest,
                                                  ChannelHandlerContext ctx) throws IOException {
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
            XrancPdu sendPdu = BearerAdmissionResponse
                    .constructPacket(ecgi, crnti, erabParams, numErabs, xranConfig.bearerFlag());
            ctx.writeAndFlush(getSctpMessage(sendPdu));
        }

        /**
         * Handle BearerReleaseInd.
         * @param bearerReleaseInd
         */
        private void handleBearerreleaseind(BearerReleaseInd bearerReleaseInd) {
            ECGI ecgi = bearerReleaseInd.getEcgi();
            CRNTI crnti = bearerReleaseInd.getCrnti();
            RnibLink link = linkMap.get(ecgi, crnti);

            List<ERABID> erabidsRelease = bearerReleaseInd.getErabIds().getERABID();
            List<ERABParamsItem> erabParamsItem = link.getBearerParameters().getERABParamsItem();

            List<ERABParamsItem> unreleased = erabParamsItem
                    .stream()
                    .filter(item -> {
                        Optional<ERABID> any = erabidsRelease.stream()
                                .filter(id -> id.equals(item.getId())).findAny();
                        return !any.isPresent();
                    }).collect(Collectors.toList());

            link.getBearerParameters().setERABParamsItem(new ArrayList<>(unreleased));
        }

        /**
         * Handle HOFailure.
         * @param hoFailure HOFailure
         * @throws InterruptedException ueIdQueue interruption
         */
        private void handleHofailure(HOFailure hoFailure) throws InterruptedException {
            try {
                hoMap.get(hoFailure.getEcgi())
                        .put("Hand Over Failed with cause: " + hoFailure.getCause());
            } catch (InterruptedException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
            } finally {
                hoMap.remove(hoFailure.getEcgi());
                ueIdQueue.take();
            }
        }

        /**
         * Handle HOComplete.
         * @param hoComplete HOComplete
         * @param ctx ChannelHandlerContext
         */
        private void handleHocomplete(HOComplete hoComplete, ChannelHandlerContext ctx) {
            EcgiCrntiPair ecgiCrntiPair = EcgiCrntiPair.valueOf(hoComplete.getEcgiT(),
                    hoComplete.getCrntiNew());
            contextUpdateMap.compute(ecgiCrntiPair, (k, v) -> {
                if (v == null) {
                    v = new ContextUpdateHandler();
                }
                if (v.setHoComplete(hoComplete)) {
                    handlePairedPackets(v.getContextUpdate(), ctx, true);

                    try {
                        hoMap.get(hoComplete.getEcgiS()).put("Hand Over Completed");
                    } catch (InterruptedException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        e.printStackTrace();
                    } finally {
                        hoMap.remove(hoComplete.getEcgiS());
                    }
                    v.reset();
                }
                return v;
            });
        }

        /**
         * Handle RXSigMeasReport.
         * @param rxSigMeasReport RXSigMeasReport
         */
        private void handleRxsigmeasreport(RXSigMeasReport rxSigMeasReport) {
            List<RXSigReport> rxSigReportList = rxSigMeasReport.getCellMeasReports().getRXSigReport();

            RnibUe ue = ueMap.get(rxSigMeasReport.getEcgi(), rxSigMeasReport.getCrnti());
            if (ue != null) {
                Long ueId = ue.getId();

                if (!rxSigReportList.isEmpty()) {
                    rxSigReportList.forEach(rxSigReport -> {
                        RnibCell cell = cellMap.get(rxSigReport.getPciArfcn());
                        if (cell != null) {
                            ECGI ecgi = cell.getEcgi();

                            RnibLink link = linkMap.get(ecgi, ueId);
                            if (link == null) {
                                log.warn("Could not find link between: {}-{} | Creating non-serving link..",
                                        ecgi, ueId);
                                link = linkMap.putNonServingLink(cell, ueId);
                            }

                            if (link != null) {
                                if (link.getType().equals(RnibLink.Type.NON_SERVING)) {
                                    restartTimer(link);
                                }

                                RSRQRange rsrq = rxSigReport.getRsrq();
                                RSRPRange rsrp = rxSigReport.getRsrp();

                                RnibLink.LinkQuality quality = link.getQuality();
                                quality.setRx(new RnibLink.LinkQuality.Rx(
                                        rsrp.value.intValue() - 140,
                                        (rsrq.value.intValue() * 0.5) - 19.5
                                ));
                            }
                        } else {
                            log.warn("case 16: Could not find cell with PCI-ARFCN: {}",
                                    rxSigReport.getPciArfcn());
                        }
                    });
                }
            }
        }

        /**
         * Handle RadioMeasReportPerUE.
         * @param radioMeasReportPerUE RadioMeasReportPerUE
         */
        private void handleRadionmeasreportperue(RadioMeasReportPerUE radioMeasReportPerUE) {
            RnibUe ue = ueMap.get(radioMeasReportPerUE.getEcgi(), radioMeasReportPerUE.getCrnti());
            if (ue != null) {
                Long ueId = ue.getId();
                List<RadioRepPerServCell> servCells = radioMeasReportPerUE.getRadioReportServCells()
                        .getRadioRepPerServCell();

                servCells.forEach(servCell -> {
                    RnibCell cell = cellMap.get(servCell.getPciArfcn());
                    if (cell != null) {
                        RnibLink link = linkMap.get(cell.getEcgi(), ueId);
                        if (link != null) {
                            RadioRepPerServCell.CqiHist cqiHist = servCell.getCqiHist();
                            RnibLink.LinkQuality quality = link.getQuality();

                            final double[] values = {0, 0, 0};
                            final int[] i = {1};
                            cqiHist.getBerInteger().forEach(value -> {
                                values[0] = Math.max(values[0], value.intValue());
                                values[1] += i[0] * value.intValue();
                                values[2] += value.intValue();
                                i[0]++;
                            });

                            quality.setCqi(new RnibLink.LinkQuality.Cqi(
                                    cqiHist,
                                    values[0],
                                    values[1] / values[0]
                            ));

                        } else {
                            log.warn("Could not find link between: {}-{}", cell.getEcgi(), ueId);
                        }
                    } else {
                        log.warn("case 18: Could not find cell with PCI-ARFCN: {}", servCell.getPciArfcn());
                    }
                });
            }
        }

        /**
         * Handle SchedMeasReportPerUE.
         * @param schedMeasReportPerUE SchedMeasReportPerUE
         */
        private void handleSchedmeasreportperue(SchedMeasReportPerUE schedMeasReportPerUE) {
            RnibUe ue = ueMap.get(schedMeasReportPerUE.getEcgi(), schedMeasReportPerUE.getCrnti());
            if (ue != null) {
                Long ueId = ue.getId();

                List<SchedMeasRepPerServCell> servCells = schedMeasReportPerUE.getSchedReportServCells()
                        .getSchedMeasRepPerServCell();

                servCells.forEach(servCell -> {
                    RnibCell cell = cellMap.get(servCell.getPciArfcn());
                    if (cell != null) {
                        RnibLink link = linkMap.get(cell.getEcgi(), ueId);
                        if (link != null) {
                            link.getQuality().setMcs(new RnibLink.LinkQuality.Mcs(
                                    servCell.getMcsDl(),
                                    servCell.getMcsUl()
                            ));

                            link.setResourceUsage(new RnibLink.ResourceUsage(
                                    servCell.getPrbUsage().getPrbUsageDl(),
                                    servCell.getPrbUsage().getPrbUsageUl()
                            ));
                        } else {
                            log.warn("Could not find link between: {}-{}", cell.getEcgi(), ueId);
                        }
                    } else {
                        log.warn("case 20: Could not find cell with PCI-ARFCN: {}", servCell.getPciArfcn());
                    }
                });
            }
        }

        /**
         * Handle SchedMeasReportPerCell.
         * @param schedMeasReportPerCell SchedMeasReportPerCell
         */
        private void handleSchedmeasreportpercell(SchedMeasReportPerCell schedMeasReportPerCell) {
            RnibCell cell = cellMap.get(schedMeasReportPerCell.getEcgi());
            if (cell != null) {
                cell.setPrbUsage(new RnibCell.PrbUsageContainer(
                        schedMeasReportPerCell.getPrbUsagePcell(),
                        schedMeasReportPerCell.getPrbUsageScell()
                ));

                cell.setQci(schedMeasReportPerCell.getQciVals());
            } else {
                log.warn("Could not find cell with ECGI: {}", schedMeasReportPerCell.getEcgi());
            }
        }

        /**
         * Handle PDCPMeasReportPerUe.
         * @param pdcpMeasReportPerUe PDCPMeasReportPerUe
         */
        private void handlePdcpmeasreportperue(PDCPMeasReportPerUe pdcpMeasReportPerUe) {
            RnibUe ue = ueMap.get(pdcpMeasReportPerUe.getEcgi(), pdcpMeasReportPerUe.getCrnti());
            if (ue != null) {
                Long ueId = ue.getId();
                RnibLink link = linkMap.get(pdcpMeasReportPerUe.getEcgi(), ueId);
                if (link != null) {
                    link.setPdcpThroughput(new RnibLink.PdcpThroughput(
                            pdcpMeasReportPerUe.getThroughputDl(),
                            pdcpMeasReportPerUe.getThroughputUl()
                    ));

                    link.setPdcpPackDelay(new RnibLink.PdcpPacketdelay(
                            pdcpMeasReportPerUe.getPktDelayDl(),
                            pdcpMeasReportPerUe.getPktDelayUl()
                    ));
                } else {
                    log.warn("Could not find link between: {}-{}", pdcpMeasReportPerUe.getEcgi(), ueId);
                }
            }
        }

        /**
         * Handle UECapabilityInfo.
         * @param capabilityInfo UECapabilityInfo
         */
        private void handleCapabilityinfo(UECapabilityInfo capabilityInfo) {
            RnibUe ue = ueMap.get(capabilityInfo.getEcgi(), capabilityInfo.getCrnti());
            if (ue != null) {
                ue.setCapability(capabilityInfo);
            } else {
                log.warn("Could not find UE with this CRNTI: {}", capabilityInfo.getCrnti());
            }
        }

        /**
         * Handle UECapabilityEnquiry.
         * @param ueCapabilityEnquiry UECapabilityEnquiry
         * @param ctx ChannelHandlerContext
         * @throws IOException IO Exception
         */
        private void handleUecapabilityenquiry(UECapabilityEnquiry ueCapabilityEnquiry, ChannelHandlerContext ctx)
                throws IOException {
            XrancPdu xrancPdu = UECapabilityEnquiry.constructPacket(ueCapabilityEnquiry.getEcgi(),
                    ueCapabilityEnquiry.getCrnti());
            ctx.writeAndFlush(getSctpMessage(xrancPdu));
        }

        /**
         * Handle ScellAddStatus.
         * @param scellAddStatus ScellAddStatus
         */
        private void handleScelladdstatus(ScellAddStatus scellAddStatus) {
            RnibUe ue = ueMap.get(scellAddStatus.getEcgi(), scellAddStatus.getCrnti());
            if (ue != null) {
                Long ueId = ue.getId();
                try {
                    scellAddMap.get(scellAddStatus.getCrnti()).put("Scell's status: " +
                            scellAddStatus.getStatus());
                    final int[] i = {0};
                    scellAddStatus.getScellsInd().getPCIARFCN().forEach(
                            pciarfcn -> {
                                if (scellAddStatus.getStatus().getBerEnum().get(i[0]).value.intValue() == 0) {
                                    RnibCell cell = cellMap.get(pciarfcn);
                                    RnibLink link = linkMap.get(cell.getEcgi(), ueId);
                                    link.setType(RnibLink.Type.SERVING_SECONDARY_CA);
                                }
                                i[0]++;
                            }
                    );

                } catch (InterruptedException e) {
                    log.error(ExceptionUtils.getFullStackTrace(e));
                    e.printStackTrace();
                } finally {
                    scellAddMap.remove(scellAddStatus.getCrnti());
                }
            }
        }

        /**
         * Handle RRMConfigStatus.
         * @param rrmConfigStatus RRMConfigStatus
         */
        private void handleRrmconfigstatus(RRMConfigStatus rrmConfigStatus) {
            try {
                rrmcellMap.get(rrmConfigStatus.getEcgi())
                        .put("RRM Config's status: " + rrmConfigStatus.getStatus());
            } catch (InterruptedException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
            } finally {
                rrmcellMap.remove(rrmConfigStatus.getEcgi());
            }
        }

        /**
         * Handle TrafficSplitConfig.
         * @param trafficSplitConfig TrafficSplitConfig
         */
        private void handleTrafficSplitConfig(TrafficSplitConfig trafficSplitConfig) {
            RnibUe ue = ueMap.get(trafficSplitConfig.getEcgi(), trafficSplitConfig.getCrnti());
            if (ue != null) {
                Long ueId = ue.getId();
                List<TrafficSplitPercentage> splitPercentages = trafficSplitConfig
                        .getTrafficSplitPercent().getTrafficSplitPercentage();

                splitPercentages.forEach(trafficSplitPercentage -> {
                    RnibCell cell = cellMap.get(trafficSplitPercentage.getEcgi());
                    if (cell != null) {
                        RnibLink link = linkMap.get(cell.getEcgi(), ueId);
                        if (link != null) {
                            link.setTrafficPercent(trafficSplitPercentage);
                        } else {
                            log.warn("Could not find link between: {}-{}", cell.getEcgi(), ueId);
                        }
                    } else {
                        log.warn("Could not find cell with ECGI: {}", trafficSplitConfig.getEcgi());
                    }
                });
            }
        }

        /**
         * Handle context update depending if its handoff or not.
         *
         * @param contextUpdate context update packet
         * @param ctx           channel context for the CELL
         * @param handoff       true if we handle a Hand Off
         */
        private void handlePairedPackets(UEContextUpdate contextUpdate, ChannelHandlerContext ctx, boolean handoff) {
            RnibUe ue;
            RnibCell cell = xranStore.getCell(contextUpdate.getEcgi());

            if (handoff) {
                try {
                    ue = ueMap.get(ueIdQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error(ExceptionUtils.getFullStackTrace(e));
                    ue = new RnibUe();
                }
            } else {
                ue = new RnibUe();
            }

            ue.setMmeS1apId(contextUpdate.getMMEUES1APID());
            ue.setEnbS1apId(contextUpdate.getENBUES1APID());
            ue.setCrnti(contextUpdate.getCrnti());

            hostAgent.addConnectedHost(ue, cell, ctx);
        }
    }

    /**
     * Internal class for NetworkConfigListener.
     */
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

        /**
         * Handle config event.
         *
         * @param config
         */
        private void handleConfigEvent(Optional<Config> config) {
            if (!config.isPresent()) {
                return;
            }

            xranConfig = (XranConfig) config.get();

            northboundTimeout = xranConfig.getNorthBoundTimeout();

            legitCells.putAll(xranConfig.activeCellSet());

            controller.start(deviceAgent, hostAgent, packetAgent, xranConfig.getXrancPort());
        }
    }
}

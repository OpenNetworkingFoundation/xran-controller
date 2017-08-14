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

package org.onosproject.xran.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.felix.scr.annotations.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.store.AbstractStore;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.EUTRANCellIdentifier;
import org.onosproject.xran.codecs.api.MMEUES1APID;
import org.onosproject.xran.controller.XranController;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibLink;
import org.onosproject.xran.entities.RnibSlice;
import org.onosproject.xran.entities.RnibUe;
import org.onosproject.xran.identifiers.LinkId;
import org.slf4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by dimitris on 7/22/17.
 */
@Component(immediate = true)
@Service
public class DefaultXranStore extends AbstractStore implements XranStore {
    private static final String XRAN_APP_ID = "org.onosproject.xran";

    private final Logger log = getLogger(getClass());
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;
    private ConcurrentMap<LinkId, RnibLink> linkMap = new ConcurrentHashMap<>();
    private ConcurrentMap<ECGI, RnibCell> cellMap = new ConcurrentHashMap<>();
    private ConcurrentMap<MMEUES1APID, RnibUe> ueMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Object, RnibSlice> sliceMap = new ConcurrentHashMap<>();
    private XranController controller;

    @Activate
    public void activate() {
        ApplicationId appId = coreService.getAppId(XRAN_APP_ID);
        log.info("XRAN Default Store Started");
    }

    @Deactivate
    public void deactive() {
        log.info("XRAN Default Store Stopped");
    }

    @Override
    public List<RnibLink> getLinks() {
        List<RnibLink> list = Lists.newArrayList();
        list.addAll(linkMap.values());
        return list;
    }

    @Override
    public List<RnibLink> getLinksByECGI(ECGI ecgi) {
        List<RnibLink> list = Lists.newArrayList();
        list.addAll(
                linkMap.keySet()
                        .stream()
                        .filter(k -> k.getEcgi().equals(ecgi))
                        .map(v -> linkMap.get(v))
                        .collect(Collectors.toList()));

        return list;
    }

    @Override
    public List<RnibLink> getLinksByCellId(String eciHex) {
        List<RnibLink> list = Lists.newArrayList();
        EUTRANCellIdentifier eci = hexToECI(eciHex);

        list.addAll(
                linkMap.keySet()
                        .stream()
                        .filter(k -> k.getEcgi().getEUTRANcellIdentifier().equals(eci))
                        .map(v -> linkMap.get(v))
                        .collect(Collectors.toList()));

        return list;
    }

    @Override
    public List<RnibLink> getLinksByUeId(long euId) {
        List<RnibLink> list = Lists.newArrayList();
        MMEUES1APID mme = new MMEUES1APID(euId);

        list.addAll(
                linkMap.keySet()
                        .stream()
                        .filter(k -> k.getMmeues1apid().equals(mme))
                        .map(v -> linkMap.get(v))
                        .collect(Collectors.toList()));

        return list;
    }

    @Override
    public RnibLink getLinkBetweenCellIdUeId(String eciHex, long euId) {
        EUTRANCellIdentifier eci = hexToECI(eciHex);
        MMEUES1APID mme = new MMEUES1APID(euId);

        Optional<LinkId> first = linkMap.keySet()
                .stream()
                .filter(linkId -> linkId.getEcgi().getEUTRANcellIdentifier().equals(eci))
                .filter(linkId -> linkId.getMmeues1apid().equals(mme))
                .findFirst();

        return first.map(linkId -> linkMap.get(linkId)).orElse(null);
    }

    @Override
    public boolean createLinkBetweenCellIdUeId(String eciHex, long euId, String type) {
        RnibCell cell = getCell(eciHex);
        RnibUe ue = getUe(euId);

        if (cell != null && ue != null) {
            RnibLink link = new RnibLink(cell, ue);

            // TODO: check logic for each type
            try {
                RnibLink.Type linkType = RnibLink.Type.valueOf(type);
                switch (linkType) {
                    case NON_SERVING:
                        break;
                    case SERVING_PRIMARY:
                        break;
                    case SERVING_SECONDARY:
                        break;
                }
            } catch (Exception e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
            }
            linkMap.put(link.getLinkId(), link);
            return true;
        }

        return false;
    }

    @Override
    public void storeLink(RnibLink link) {
        if (link.getLinkId() != null) {
            linkMap.put(link.getLinkId(), link);
        }
    }

    @Override
    public boolean removeLink(LinkId link) {
        return linkMap.remove(link) != null;
    }

    @Override
    public RnibLink getLink(ECGI ecgi, MMEUES1APID mme) {

        LinkId linkId = LinkId.valueOf(ecgi, mme);
        return linkMap.get(linkId);
    }

    @Override
    public void modifyLinkRrmConf(RnibLink link, JsonNode rrmConf) {
        link.modifyRrmParameters(rrmConf);
    }

    @Override
    public List<Object> getNodes() {
        List<Object> list = Lists.newArrayList();
        list.add(cellMap.values());
        list.add(ueMap.values());
        return list;
    }

    @Override
    public List<RnibCell> getCellNodes() {
        List<RnibCell> list = Lists.newArrayList();
        list.addAll(cellMap.values());
        return list;
    }

    @Override
    public List<RnibUe> getUeNodes() {
        List<RnibUe> list = Lists.newArrayList();
        list.addAll(ueMap.values());
        return list;
    }

    @Override
    public Object getByNodeId(String nodeId) {
        try {
            return getCell(nodeId);
        } catch (Exception e) {

        }
        return getUe(Long.parseLong(nodeId));
    }

    @Override
    public void storeCell(RnibCell cell) {
        if (cell.getEcgi() != null) {
            cellMap.putIfAbsent(cell.getEcgi(), cell);
        }
    }

    @Override
    public boolean removeCell(ECGI ecgi) {
        return cellMap.remove(ecgi) != null;
    }

    @Override
    public RnibCell getCell(String hexeci) {
        EUTRANCellIdentifier eci = hexToECI(hexeci);
        Optional<ECGI> first = cellMap.keySet().stream().filter(ecgi -> ecgi.getEUTRANcellIdentifier().equals(eci)).findFirst();
        return first.map(ecgi -> cellMap.get(ecgi)).orElse(null);
    }

    @Override
    public RnibCell getCell(ECGI ecgi) {
        return cellMap.get(ecgi);
    }

    @Override
    public void modifyCellRrmConf(RnibCell cell, JsonNode rrmConf) {
        List<RnibLink> linkList = getLinksByECGI(cell.getEcgi());
        List<RnibUe> ueList = linkList.stream().map(link -> link.getLinkId().getUe()).collect(Collectors.toList());

        cell.modifyRrmConfig(rrmConf, ueList);
    }

    @Override
    public RnibSlice getSlice(long sliceId) {
        if (sliceMap.containsKey(sliceId)) {
            return sliceMap.get(sliceId);
        }
        return null;
    }

    @Override
    public boolean createSlice(ObjectNode attributes) {
        return false;
    }

    @Override
    public boolean removeCell(long sliceId) {
        return sliceMap.remove(sliceId) != null;
    }

    @Override
    public XranController getController() {
        return controller;
    }

    @Override
    public void setController(XranController controller) {
        this.controller = controller;
    }

    @Override
    public void storeUe(RnibUe ue) {
        if (ue.getMmeS1apId() != null) {
            ueMap.putIfAbsent(ue.getMmeS1apId(), ue);
        }
    }

    @Override
    public boolean removeUe(MMEUES1APID mme) {
        return ueMap.remove(mme) != null;
    }

    @Override
    public RnibUe getUe(long euId) {
        MMEUES1APID mme = new MMEUES1APID(euId);
        return ueMap.get(mme);
    }

    @Override
    public RnibUe getUe(MMEUES1APID mme) {
        return ueMap.get(mme);
    }

    private EUTRANCellIdentifier hexToECI(String eciHex) {
        byte[] hexBinary = DatatypeConverter.parseHexBinary(eciHex);
        return new EUTRANCellIdentifier(hexBinary, 28);
    }
}

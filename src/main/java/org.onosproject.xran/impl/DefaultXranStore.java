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

package org.onosproject.xran.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.core.IdGenerator;
import org.onosproject.store.AbstractStore;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.EUTRANCellIdentifier;
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
 * Default xran store.
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
    private ConcurrentMap<Long, RnibUe> ueMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Object, RnibSlice> sliceMap = new ConcurrentHashMap<>();
    private XranController controller;

    private IdGenerator ueIdGenerator;

    @Activate
    public void activate() {
        ApplicationId appId = coreService.getAppId(XRAN_APP_ID);

        // create ue id generator
        ueIdGenerator = coreService.getIdGenerator("xran-ue-id");

        log.info("XRAN Default Store Started");
    }

    @Deactivate
    public void deactivate() {
        log.info("XRAN Default Store Stopped");
    }

    @Override
    public List<RnibLink> getLinks() {
        List<RnibLink> list = Lists.newArrayList();
        list.addAll(linkMap.values());
        return list;
    }

    @Override
    public List<RnibLink> getlinksbyecgi(ECGI ecgi) {
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
    public List<RnibLink> getlinksbycellid(String eciHex) {
        List<RnibLink> list = Lists.newArrayList();
        EUTRANCellIdentifier eci = hexToEci(eciHex);

        list.addAll(
                linkMap.keySet()
                        .stream()
                        .filter(k -> k.getEcgi().getEUTRANcellIdentifier().equals(eci))
                        .map(v -> linkMap.get(v))
                        .collect(Collectors.toList()));

        return list;
    }

    @Override
    public List<RnibLink> getlinksbyueid(long euId) {
        List<RnibLink> list = Lists.newArrayList();

        list.addAll(
                linkMap.keySet()
                        .stream()
                        .filter(k -> k.getUeId().equals(euId))
                        .map(v -> linkMap.get(v))
                        .collect(Collectors.toList()));

        return list;
    }


    @Override
    public RnibLink getlinkbetweencellidueid(String eciHex, long euId) {
        EUTRANCellIdentifier eci = hexToEci(eciHex);

        Optional<LinkId> first = linkMap.keySet()
                .stream()
                .filter(linkId -> linkId.getEcgi().getEUTRANcellIdentifier().equals(eci) &&
                        linkId.getUeId().equals(euId))
                .findFirst();

        return first.map(linkId -> linkMap.get(linkId)).orElse(null);
    }

    @Override
    public void storeLink(RnibLink link) {
        synchronized (this) {
            if (link.getLinkId() != null) {
                // if we add a primary link then change the primary to non serving
                if (link.getType().equals(RnibLink.Type.SERVING_PRIMARY)) {
                    RnibUe ue = link.getLinkId().getUe();
                    getlinksbyueid(ue.getId())
                            .stream()
                            .filter(l -> l.getType().equals(RnibLink.Type.SERVING_PRIMARY))
                            .forEach(l -> l.setType(RnibLink.Type.NON_SERVING));
                }
                linkMap.put(link.getLinkId(), link);
            }
        }
    }

    @Override
    public boolean removeLink(LinkId link) {
        return linkMap.remove(link) != null;
    }

    @Override
    public RnibLink getLink(ECGI ecgi, Long ueId) {
        LinkId linkId = LinkId.valueOf(ecgi, ueId);
        return linkMap.get(linkId);
    }

    @Override
    public void modifylinkrrmconf(RnibLink link, JsonNode rrmConf) {
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
    public List<Object> getcellnodes() {
        List<Object> list = Lists.newArrayList();
        list.addAll(cellMap.values());
        return list;
    }

    @Override
    public List<Object> getuenodes() {
        List<Object> list = Lists.newArrayList();
        list.addAll(ueMap.values());
        return list;
    }

    @Override
    public Object getbynodeid(String nodeId) {
        try {
            return getCell(nodeId);
        } catch (Exception ignored) {
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
        EUTRANCellIdentifier eci = hexToEci(hexeci);
        Optional<ECGI> first = cellMap.keySet()
                .stream()
                .filter(ecgi -> ecgi.getEUTRANcellIdentifier().equals(eci))
                .findFirst();
        return first.map(ecgi -> cellMap.get(ecgi))
                .orElse(null);
    }

    @Override
    public RnibCell getCell(ECGI ecgi) {
        return cellMap.get(ecgi);
    }

    @Override
    public void modifycellrrmconf(RnibCell cell, JsonNode rrmConf) throws Exception {
        List<RnibLink> linkList = getlinksbyecgi(cell.getEcgi());
        List<RnibUe> ueList = linkList.stream()
                .map(link -> link.getLinkId().getUe())
                .collect(Collectors.toList());

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
        long newId = ueIdGenerator.getNewId();
        ue.setId(newId);
        ueMap.put(newId, ue);
    }

    @Override
    public boolean removeUe(long ueId) {
        return ueMap.remove(ueId) != null;
    }

    @Override
    public RnibUe getUe(long ueId) {
        return ueMap.get(ueId);
    }

    /**
     * Get from HEX string the according ECI class object.
     *
     * @param eciHex HEX string
     * @return ECI object if created successfully
     */
    private EUTRANCellIdentifier hexToEci(String eciHex) {
        byte[] hexBinary = DatatypeConverter.parseHexBinary(eciHex);
        return new EUTRANCellIdentifier(hexBinary, 28);
    }
}

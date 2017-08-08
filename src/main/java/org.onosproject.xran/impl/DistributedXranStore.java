///*
// * Copyright 2015-present Open Networking Laboratory
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.onosproject.xran.impl;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.google.common.collect.Lists;
//import org.apache.commons.lang3.tuple.ImmutablePair;
//import org.apache.felix.scr.annotations.Activate;
//import org.apache.felix.scr.annotations.Deactivate;
//import org.apache.felix.scr.annotations.Reference;
//import org.apache.felix.scr.annotations.ReferenceCardinality;
//import org.onlab.util.KryoNamespace;
//import org.onosproject.core.ApplicationId;
//import org.onosproject.core.CoreService;
//import org.onosproject.core.IdGenerator;
//import org.onosproject.store.AbstractStore;
//import org.onosproject.store.serializers.KryoNamespaces;
//import org.onosproject.store.service.ConsistentMap;
//import org.onosproject.store.service.Serializer;
//import org.onosproject.store.service.StorageService;
//import org.onosproject.store.service.Versioned;
//import org.onosproject.xran.XranStore;
//import org.onosproject.xran.codecs.api.ECGI;
//import org.onosproject.xran.codecs.api.ENBUES1APID;
//import org.onosproject.xran.codecs.api.MMEUES1APID;
//import org.onosproject.xran.codecs.pdu.CellConfigReport;
//import org.onosproject.xran.controller.XranController;
//import org.onosproject.xran.entities.RnibCell;
//import org.onosproject.xran.entities.RnibLink;
//import org.onosproject.xran.entities.RnibSlice;
//import org.onosproject.xran.entities.RnibUe;
//import org.onosproject.xran.identifiers.CellId;
//import org.onosproject.xran.identifiers.LinkId;
//import org.onosproject.xran.identifiers.SliceId;
//import org.onosproject.xran.identifiers.UeId;
//import org.slf4j.Logger;
//
//import java.util.List;
//
//import static org.slf4j.LoggerFactory.getLogger;
//
///**
// * Created by dimitris on 7/22/17.
// */
////@Component(immediate = true)
////@Service
//public class DistributedXranStore extends AbstractStore implements XranStore {
//    private static final String XRAN_APP_ID = "org.onosproject.xran";
//
//    private final Logger log = getLogger(getClass());
//
//    private ConsistentMap<LinkId, RnibLink> linkMap;
//    private ConsistentMap<CellId, RnibCell> cellMap;
//    private ConsistentMap<UeId, RnibUe> ueMap;
//    private ConsistentMap<SliceId, RnibSlice> sliceMap;
//
//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    protected StorageService storageService;
//
//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    protected CoreService coreService;
//
//    private IdGenerator cellIdGenerator;
//    private IdGenerator ueIdGenerator;
//    private IdGenerator linkIdGenerator;
//    private IdGenerator sliceIdGenerator;
//
//    private XranController controller;
//
//    private final String XRAN_CELL_ID = "xran-cell-ids";
//    private final String XRAN_UE_ID = "xran-eu-ids";
//    private final String XRAN_LINK_ID = "xran-link-ids";
//    private final String XRAN_SLICE_ID = "xran-slice-ids";
//
//    @Activate
//    public void activate() {
//        ApplicationId appId = coreService.getAppId(XRAN_APP_ID);
//
//        cellIdGenerator = coreService.getIdGenerator(XRAN_CELL_ID);
//        ueIdGenerator = coreService.getIdGenerator(XRAN_UE_ID);
//        linkIdGenerator = coreService.getIdGenerator(XRAN_LINK_ID);
//        sliceIdGenerator = coreService.getIdGenerator(XRAN_SLICE_ID);
//
//        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
//                .register(KryoNamespaces.API)
//                .register(RnibCell.class)
//                .register(RnibSlice.class)
//                .register(RnibUe.class)
//                .register(RnibLink.class)
//                .register(LinkId.class)
//                .register(CellId.class)
//                .register(UeId.class)
//                .register(SliceId.class)
//                .register(ImmutablePair.class)
//                .register(ENBUES1APID.class)
//                .register(MMEUES1APID.class)
//                .register(CellConfigReport.class)
//                .register(ECGI.class);
//
//        linkMap = storageService.<LinkId, RnibLink>consistentMapBuilder()
//                .withSerializer(Serializer.using(serializer.build()))
//                .withName("xran-link-map")
//                .withApplicationId(appId)
//                .withPurgeOnUninstall()
//                .build();
//
//        cellMap = storageService.<CellId, RnibCell>consistentMapBuilder()
//                .withSerializer(Serializer.using(serializer.build()))
//                .withName("xran-cell-map")
//                .withApplicationId(appId)
//                .withPurgeOnUninstall()
//                .build();
//
//        ueMap = storageService.<UeId, RnibUe>consistentMapBuilder()
//                .withSerializer(Serializer.using(serializer.build()))
//                .withName("xran-ue-map")
//                .withApplicationId(appId)
//                .withPurgeOnUninstall()
//                .build();
//
//        sliceMap = storageService.<SliceId, RnibSlice>consistentMapBuilder()
//                .withSerializer(Serializer.using(serializer.build()))
//                .withName("xran-slice-map")
//                .withApplicationId(appId)
//                .withPurgeOnUninstall()
//                .build();
//
//        log.info("XRAN Distributed Store Started");
//    }
//
//    @Deactivate
//    public void deactive() {
//        log.info("XRAN Distributed Store Stopped");
//    }
//
//    @Override
//    public List<RnibLink> getLinksByCellId(long cellId) {
//        List<RnibLink> list = Lists.newArrayList();
//        CellId cell = CellId.valueOf(cellId);
//        linkMap.keySet().forEach(
//                pair -> {
//                    if (pair.equals(cell)) {
//                        list.add(linkMap.get(pair).value());
//                    }
//                }
//        );
//        return list;
//    }
//
//    @Override
//    public List<RnibLink> getLinksByUeId(long euId) {
//        List<RnibLink> list = Lists.newArrayList();
//        UeId ue = UeId.valueOf(euId);
//        linkMap.keySet().forEach(
//                pair -> {
//                    if (pair.equals(ue)) {
//                        list.add(linkMap.get(pair).value());
//                    }
//                }
//        );
//        return list;
//    }
//
//    @Override
//    public RnibLink getLinkBetweenCellIdUeId(long cellId, long euId) {
//        LinkId linkId = LinkId.valueOf(cellId, euId);
//        final Versioned<RnibLink> rnibLinkVersioned = linkMap.get(linkId);
//        if (rnibLinkVersioned != null) {
//            return rnibLinkVersioned.value();
//        }
//        return null;
//    }
//
//    @Override
//    public boolean modifyTypeOfLink(long cellId, long euId, String type) {
//        final RnibLink link = getLinkBetweenCellIdUeId(cellId, euId);
//        if (link != null) {
//            link.setType(type);
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean modifyTrafficPercentOfLink(long cellId, long euId, long trafficPercent) {
//        final RnibLink link = getLinkBetweenCellIdUeId(cellId, euId);
//        if (link != null) {
//            link.setTrafficPercent(trafficPercent);
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean createLinkBetweenCellIdUeId(long cellId, long euId, String type) {
//        LinkId linkId = LinkId.valueOf(cellId, euId);
//        if (linkMap.containsKey(linkId)) {
//            return false;
//        }
//        RnibLink link = new RnibLink(linkId);
//        link.setType(type);
//        linkMap.putPrimaryLink(linkId, link);
//        return true;
//    }
//
//    @Override
//    public boolean deleteLink(long linkId) {
//        return false;
//    }
//
//    @Override
//    public List<Object> getNodes() {
//        List<Object> list = Lists.newArrayList();
//        cellMap.values().forEach(v -> list.add(v.value()));
//        ueMap.values().forEach(v -> list.add(v.value()));
//        return list;
//    }
//
//    @Override
//    public List<RnibCell> getCellNodes() {
//        List<RnibCell> list = Lists.newArrayList();
//        cellMap.values().forEach(v -> list.add(v.value()));
//        return list;
//    }
//
//    @Override
//    public List<RnibUe> getUeNodes() {
//        List<RnibUe> list = Lists.newArrayList();
//        ueMap.values().forEach(v -> list.add(v.value()));
//        return list;
//    }
//
//    @Override
//    public Object getByNodeId(long nodeId) {
//        CellId cellId = CellId.valueOf(nodeId);
//        if (cellMap.containsKey(cellId)) {
//            return cellMap.get(cellId).value();
//        }
//        UeId ueId = UeId.valueOf(nodeId);
//        if (ueMap.containsKey(ueId)) {
//            return ueMap.get(ueId).value();
//        }
//        return null;
//    }
//
//    @Override
//    public void storeCell(RnibCell cell) {
//        final CellId cellId = CellId.valueOf(cellIdGenerator.getNewId());
//        cell.setCellId(cellId);
//        cellMap.putIfAbsent(cellId, cell);
//    }
//
//    @Override
//    public RnibCell getCell(long cellId) {
//        CellId cell = CellId.valueOf(cellId);
//        if (cellMap.containsKey(cell)) {
////            controller.sendMsg(cellMap.get(cell).value().getDevId(), "skata");
//            return cellMap.get(cell).value();
//        }
//        return null;
//    }
//
//    @Override
//    public boolean modifyCellRrmConf(JsonNode rrmConf) {
//        return false;
//    }
//
//    @Override
//    public RnibSlice getSlice(long sliceId) {
//        SliceId slice = SliceId.valueOf(sliceId);
//        if (sliceMap.containsKey(slice)) {
//            return sliceMap.get(slice).value();
//        }
//        return null;
//    }
//
//    @Override
//    public boolean createSlice(ObjectNode attributes) {
//        return false;
//    }
//
//    public XranController getController() {
//        return controller;
//    }
//
//    @Override
//    public void storeUe(RnibUe ue) {
//        final UeId ueId = UeId.valueOf(ueIdGenerator.getNewId());
//        ue.setUeId(ueId);
//        ueMap.putIfAbsent(ueId, ue);
//    }
//
//    public void setController(XranController controller) {
//        this.controller = controller;
//    }
//}

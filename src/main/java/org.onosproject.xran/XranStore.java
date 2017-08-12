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

package org.onosproject.xran;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.store.Store;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.MMEUES1APID;
import org.onosproject.xran.controller.XranController;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibLink;
import org.onosproject.xran.entities.RnibSlice;
import org.onosproject.xran.entities.RnibUe;
import org.onosproject.xran.identifiers.LinkId;

import java.util.List;

/**
 * Created by dimitris on 7/22/17.
 */
public interface XranStore extends Store {

    // LINKS STORE

    List<RnibLink> getLinks();

    List<RnibLink> getLinksByECGI(ECGI ecgi);

    List<RnibLink> getLinksByCellId(String eciHex);

    List<RnibLink> getLinksByUeId(long euId);

    RnibLink getLinkBetweenCellIdUeId(String cellId, long euId);

    boolean createLinkBetweenCellIdUeId(String cellId, long euId, String type);

    RnibLink getLink(ECGI ecgi, MMEUES1APID mme);

    void storeLink(RnibLink link);

    boolean removeLink(LinkId link);

    // NODES

    List<Object> getNodes();

    List<RnibCell> getCellNodes();

    List<RnibUe> getUeNodes();

    Object getByNodeId(String nodeId);

    // CELL

    RnibCell getCell(String eci);

    RnibCell getCell(ECGI cellId);

    boolean modifyCellRrmConf(RnibCell cell, JsonNode rrmConf);

    void storeCell(RnibCell cell);

    boolean removeCell(ECGI ecgi);

    // SLICE

    RnibSlice getSlice(long sliceId);

    boolean createSlice(ObjectNode attributes);

    boolean removeCell(long sliceId);

    // CONTROLLER

    XranController getController();

    void setController(XranController controller);

    // UE

    RnibUe getUe(long euId);

    RnibUe getUe(MMEUES1APID mme);

    void storeUe(RnibUe ue);

    boolean removeUe(MMEUES1APID mme);
}

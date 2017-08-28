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

package org.onosproject.xran;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.store.Store;
import org.onosproject.xran.codecs.api.ECGI;
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

    /**
     * Get all active links.
     *
     * @return list of links
     */
    List<RnibLink> getLinks();

    /**
     * Get all links for that CELL based on ECGI.
     *
     * @param ecgi CELL ECGI
     * @return list of links
     */
    List<RnibLink> getlinksbyecgi(ECGI ecgi);

    /**
     * Get all links for that CELL based on ECI.
     *
     * @param eciHex HEX string of ECI
     * @return list of links
     */
    List<RnibLink> getlinksbycellid(String eciHex);

    /**
     * Get all links for the UE based on UE ID.
     *
     * @param ueId UE ID
     * @return list of links
     */
    List<RnibLink> getlinksbyueid(long ueId);

    /**
     * Get a link between a CELL and UE.
     *
     * @param cellId HEX string ECI
     * @param ueId   UE id
     * @return link
     */
    RnibLink getlinkbetweencellidueid(String cellId, long ueId);

    /**
     * Get a link between a CELL's ECGI and UE's id.
     *
     * @param ecgi CELL ECGI
     * @param ueId UE id
     * @return link
     */
    RnibLink getLink(ECGI ecgi, Long ueId);

    /**
     * Modify specified link's RRM Configuration.
     *
     * @param link    LINK entity
     * @param rrmConf json node of RRM Configuration
     */
    void modifylinkrrmconf(RnibLink link, JsonNode rrmConf);

    /**
     * Put new link to store.
     *
     * @param link LINK entity
     */
    void storeLink(RnibLink link);

    /**
     * Remove link from store.
     *
     * @param link LINK entity
     * @return true if remove succeeded
     */
    boolean removeLink(LinkId link);

    // NODES

    /**
     * Get all CELLs and UEs.
     *
     * @return list of UEs and CELLs
     */
    List<Object> getNodes();

    /**
     * Get all CELLs.
     *
     * @return list of CELLs
     */
    List<Object> getcellnodes();

    /**
     * Get all UEs.
     *
     * @return list of UEs
     */
    List<Object> getuenodes();

    /**
     * Get node by node id.
     *
     * @param nodeId HEX string ECI or UE id
     * @return CELL or UE
     */
    Object getbynodeid(String nodeId);

    // CELL

    /**
     * Get cell based on HEX string ECI.
     *
     * @param eci HEX string ECI
     * @return CELL if found
     */
    RnibCell getCell(String eci);

    /**
     * Get cell based on ECGI.
     *
     * @param cellId CELL ECGI
     * @return CELL if found
     */
    RnibCell getCell(ECGI cellId);

    /**
     * Modify CELL's RRM Configuration.
     *
     * @param cell    CELL entity
     * @param rrmConf json node of RRM Configuration
     * @throws Exception exception
     */
    void modifycellrrmconf(RnibCell cell, JsonNode rrmConf) throws Exception;

    /**
     * Put new CELL to the store.
     *
     * @param cell CELL entity
     */
    void storeCell(RnibCell cell);

    /**
     * Remove CELL from the store.
     *
     * @param ecgi CELL's ECGI
     * @return ture if remove succeeded
     */
    boolean removeCell(ECGI ecgi);

    // SLICE

    /**
     * Get SLICE based on SLICE id.
     *
     * @param sliceId SLICE id
     * @return SLICE
     */
    RnibSlice getSlice(long sliceId);

    /**
     * Put new SLICE to the store.
     *
     * @param attributes json node of SLICE attributes
     * @return true if put succeeded
     */
    boolean createSlice(ObjectNode attributes);

    /**
     * Remove SLICE based on SLICE id.
     *
     * @param sliceId SLICE id
     * @return true if remove succeeded
     */
    boolean removeCell(long sliceId);

    // CONTROLLER

    /**
     * Get the xran controller instance.
     *
     * @return xran controller
     */
    XranController getController();

    /**
     * Set the xran controller instance.
     *
     * @param controller xran controller
     */
    void setController(XranController controller);

    // UE

    /**
     * Get UE based on UE id.
     *
     * @param euId UE id
     * @return UE entity
     */
    RnibUe getUe(long euId);

    /**
     * Put new UE to store.
     *
     * @param ue UE entity
     */
    void storeUe(RnibUe ue);

    /**
     * Remove UE from store.
     *
     * @param ueId UE id
     * @return true if remove succeeded
     */
    boolean removeUe(long ueId);
}

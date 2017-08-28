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

package org.onosproject.xran.wrapper;

import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.CRNTI;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibLink;
import org.onosproject.xran.entities.RnibUe;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * LINK wrapper to help put/get/remove.
 */
public class LinkMap {
    private static final Logger log = getLogger(LinkMap.class);

    private final XranStore xranStore;

    private UeMap ueMap;

    public LinkMap(XranStore xranStore, UeMap ueMap) {
        this.xranStore = xranStore;
        this.ueMap = ueMap;
    }

    /**
     * Put a new primary link between a CELL and a UE.
     *
     * @param cell CELL entity
     * @param ue   UE entity
     */
    public void putPrimaryLink(RnibCell cell, RnibUe ue) {
        RnibLink link = new RnibLink(cell, ue);
        // set link to primary before storing
        link.setType(RnibLink.Type.SERVING_PRIMARY);
        xranStore.storeLink(link);

        ueMap.putCrnti(cell, ue);
    }

    /**
     * Put non-serving link based on CELL and CRNTI.
     *
     * @param cell  CELL entity
     * @param crnti CRNTI
     * @return new link after creation
     */
    public RnibLink putNonServingLink(RnibCell cell, CRNTI crnti) {
        RnibLink link = null;
        RnibUe ue = ueMap.get(cell.getEcgi(), crnti);

        if (ue != null) {
            link = new RnibLink(cell, ue);
            xranStore.storeLink(link);
        } else {
            log.error("Could not find mapping for CRNTI to UE. Aborting creation of non-serving link");
        }
        return link;
    }

    /**
     * Put non-serving link based on CELL and UE id.
     *
     * @param cell CELL entity
     * @param ueId UE id
     * @return new link after creation
     */
    public RnibLink putNonServingLink(RnibCell cell, Long ueId) {
        RnibLink link = null;
        RnibUe ue = ueMap.get(ueId);

        if (ue != null) {
            link = new RnibLink(cell, ue);
            xranStore.storeLink(link);
        } else {
            log.error("Could not find mapping for CRNTI to UE. Aborting creation of non-serving link");
        }
        return link;
    }

    /**
     * Get link based on ECGI and UE id.
     *
     * @param src CELL ECGI
     * @param dst UE ID
     * @return link if found
     */
    public RnibLink get(ECGI src, Long dst) {
        if (src != null && dst != null) {
            return xranStore.getLink(src, dst);
        }
        return null;
    }

    /**
     * Get link based on ECGI and CRNTI.
     *
     * @param src CELL ECGI
     * @param dst CELL unique CRNTI
     * @return link if found
     */
    public RnibLink get(ECGI src, CRNTI dst) {
        RnibUe ue = ueMap.get(src, dst);

        if (ue != null) {
            return xranStore.getLink(src, ue.getId());
        }
        return null;
    }

    /**
     * Get CRNTI based on UE id.
     *
     * @param ueId UE id
     * @return UE if found
     */
    public CRNTI getCrnti(Long ueId) {
        return ueMap.getCrntUe().inverse().get(ueId).getValue();
    }

    /**
     * Get primary CELL for specified UE.
     *
     * @param ue UE entity
     * @return primary CELL if found
     */
    public RnibCell getPrimaryCell(RnibUe ue) {
        List<RnibLink> linksByUeId = xranStore.getlinksbyueid(ue.getId());

        // TODO: search for primary link from crntUe in UeMap because it has the primary links only!
        // search all links for this UE and find PRIMARY.
        Optional<RnibLink> primary = linksByUeId.stream()
                .filter(l -> l.getType().equals(RnibLink.Type.SERVING_PRIMARY))
                .findFirst();

        if (primary.isPresent()) {
            return primary.get().getLinkId().getCell();
        }
        return null;
    }
}
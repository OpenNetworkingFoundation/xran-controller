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

public class LinkMap {
    private static final Logger log = getLogger(LinkMap.class);

    private final XranStore xranStore;

    private UeMap ueMap;

    public LinkMap(XranStore xranStore, UeMap ueMap) {
        this.xranStore = xranStore;
        this.ueMap = ueMap;
    }

    public void putPrimaryLink(RnibCell cell, RnibUe ue) {
        RnibLink link = new RnibLink(cell, ue);
        link.setType(RnibLink.Type.SERVING_PRIMARY);
        xranStore.storeLink(link);

        ueMap.putCrnti(cell, ue);
    }

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

    public RnibLink get(ECGI src, Long dst) {
        if (src != null && dst != null) {
            return xranStore.getLink(src, dst);
        }
        return null;
    }

    public RnibLink get(ECGI src, CRNTI dst) {
        RnibUe ue = ueMap.get(src, dst);

        if (ue != null) {
            return xranStore.getLink(src, ue.getId());
        }
        return null;
    }

    public CRNTI getCrnti(Long ueId) {
        return ueMap.getCrntUe().inverse().get(ueId).getValue();
    }

    public RnibCell getPrimaryCell(RnibUe ue) {
        List<RnibLink> linksByUeId = xranStore.getLinksByUeId(ue.getId());
        Optional<RnibLink> primary = linksByUeId.stream()
                .filter(l -> l.getType().equals(RnibLink.Type.SERVING_PRIMARY))
                .findFirst();

        if (primary.isPresent()) {
            return primary.get().getLinkId().getCell();
        }
        return null;
    }
}
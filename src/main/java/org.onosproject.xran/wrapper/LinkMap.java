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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.CRNTI;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.MMEUES1APID;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibLink;
import org.onosproject.xran.entities.RnibUe;
import org.onosproject.xran.identifiers.LinkId;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class LinkMap {
    private static final Logger log = getLogger(LinkMap.class);
    private final XranStore xranStore;
    private BiMap<CRNTI, MMEUES1APID> crntiMme = HashBiMap.create();

    public LinkMap(XranStore xranStore) {
        this.xranStore = xranStore;
    }

    public void putPrimaryLink(RnibCell cell, RnibUe ue) {
        RnibLink link = new RnibLink(cell, ue);
        link.setType(RnibLink.Type.SERVING_PRIMARY);
        xranStore.storeLink(link);
        crntiMme.put(ue.getRanId(), ue.getMmeS1apId());
    }

    public RnibLink putNonServingLink(RnibCell cell, CRNTI crnti) {
        RnibLink link = null;
        MMEUES1APID mmeues1APID = crntiMme.get(crnti);

        if (mmeues1APID != null) {
            RnibUe ue = xranStore.getUe(mmeues1APID);
            link = new RnibLink(cell, ue);
            xranStore.storeLink(link);
        } else {
            log.error("Could not find mapping for CRNTI to UE. Aborting creation of non-serving link");
        }
        return link;
    }

    public RnibLink get(ECGI src, MMEUES1APID dst) {
        if (src != null && dst != null) {
            return xranStore.getLink(src, dst);
        }
        return null;
    }

    public RnibLink get(ECGI src, CRNTI dst) {
        MMEUES1APID mmeues1APID = crntiMme.get(dst);

        if (mmeues1APID != null) {
            return xranStore.getLink(src, mmeues1APID);
        }
        return null;
    }

    public CRNTI getCrnti(MMEUES1APID mme) {
        return crntiMme.inverse().get(mme);
    }

    public boolean remove(ECGI src, MMEUES1APID dst) {
        RnibLink link = xranStore.getLink(src, dst);

        if (link != null) {
            LinkId linkId = link.getLinkId();
            if (linkId != null) {
                return xranStore.removeLink(linkId);
            }
        }
        return false;
    }

    public boolean remove(ECGI src, CRNTI dst) {
        MMEUES1APID mmeues1APID = crntiMme.get(dst);

        RnibLink link = xranStore.getLink(src, mmeues1APID);
        if (link != null) {
            LinkId linkId = link.getLinkId();
            if (linkId != null) {
                return xranStore.removeLink(linkId);
            }
        }
        return false;
    }

    public RnibCell getPrimaryCell(RnibUe ue) {
        List<RnibLink> linksByUeId = xranStore.getLinksByUeId(ue.getMmeS1apId().longValue());
        Optional<RnibLink> primary = linksByUeId.stream()
                .filter(l -> l.getType().equals(RnibLink.Type.SERVING_PRIMARY))
                .findFirst();

        if (primary.isPresent()) {
            return primary.get().getLinkId().getCell();
        }
        return null;
    }
}
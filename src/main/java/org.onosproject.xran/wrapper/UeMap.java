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
import org.onosproject.xran.entities.RnibUe;
import org.onosproject.xran.identifiers.EcgiCrntiPair;

public class UeMap {
    private BiMap<EcgiCrntiPair, Long> crntUe = HashBiMap.create();

    private XranStore xranStore;

    public UeMap(XranStore xranStore) {
        this.xranStore = xranStore;
    }

    public BiMap<EcgiCrntiPair, Long> getCrntUe() {
        return crntUe;
    }

    public void putCrnti(RnibCell cell, RnibUe ue) {
        CRNTI ranId = ue.getCrnti();
        ECGI ecgi = cell.getEcgi();
        if (ranId != null && ecgi != null) {
            EcgiCrntiPair oldPair = crntUe.inverse().get(ue.getId()),
                    newPair = EcgiCrntiPair.valueOf(cell.getEcgi(), ue.getCrnti());
            if (oldPair == null) {
                crntUe.put(newPair, ue.getId());
            } else {
                crntUe.inverse().remove(ue.getId());
                crntUe.put(newPair, ue.getId());
            }
        }
    }

    public void put(RnibCell cell, RnibUe ue) {
        xranStore.storeUe(ue);
        putCrnti(cell, ue);
    }

    public RnibUe get(ECGI ecgi, CRNTI crnti) {
        Long aLong = crntUe.get(EcgiCrntiPair.valueOf(ecgi, crnti));
        if (aLong != null) {
            return xranStore.getUe(aLong);
        }
        return null;
    }

    public RnibUe get(Long ueId) {
        return xranStore.getUe(ueId);
    }

    public boolean remove(Long ueId) {
        crntUe.inverse().remove(ueId);
        return xranStore.removeUe(ueId);
    }
}

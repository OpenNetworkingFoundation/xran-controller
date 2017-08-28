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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.CRNTI;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibUe;
import org.onosproject.xran.identifiers.EcgiCrntiPair;

/**
 * UE wrapper to help put/get/remove.
 */
public class UeMap {
    // ECGI, CRNTI pair of primary cell for specified UE.
    private BiMap<EcgiCrntiPair, Long> crntUe = HashBiMap.create();

    private XranStore xranStore;

    public UeMap(XranStore xranStore) {
        this.xranStore = xranStore;
    }

    /**
     * Get the ECGI, CRNTI to UE bimap.
     *
     * @return BiMap of EcgiCrntiPair to Long
     */
    public BiMap<EcgiCrntiPair, Long> getCrntUe() {
        return crntUe;
    }

    /**
     * Put new ECGI, CRNTI pair of primary link to UE and remove old one.
     *
     * @param cell new primary CELL
     * @param ue   UE
     */
    public void putCrnti(RnibCell cell, RnibUe ue) {
        CRNTI crnti = ue.getCrnti();
        ECGI ecgi = cell.getEcgi();

        if (crnti != null && ecgi != null) {
            // check if there is an ecgi, crnti pair for this UE id.
            EcgiCrntiPair oldPair = crntUe.inverse().get(ue.getId()),
                    newPair = EcgiCrntiPair.valueOf(cell.getEcgi(), ue.getCrnti());
            if (oldPair == null) {
                crntUe.put(newPair, ue.getId());
            } else {
                // remove old pair and add the new pair which corresponds to the primary cell.
                crntUe.inverse().remove(ue.getId());
                crntUe.put(newPair, ue.getId());
            }
        }
    }

    /**
     * Put new UE to the store and update the ECGI, CRNTI pair.
     *
     * @param cell new primary CELL
     * @param ue   UE
     */
    public void put(RnibCell cell, RnibUe ue) {
        xranStore.storeUe(ue);
        // after adding new primary cell update the bimap as well.
        putCrnti(cell, ue);
    }

    /**
     * Get UE based on ECGI and CRNTI.
     *
     * @param ecgi  CELL ECGI
     * @param crnti CELL unique CRNTI
     * @return UE entity if found
     */
    public RnibUe get(ECGI ecgi, CRNTI crnti) {
        Long aLong = crntUe.get(EcgiCrntiPair.valueOf(ecgi, crnti));
        if (aLong != null) {
            return xranStore.getUe(aLong);
        }
        return null;
    }

    /**
     * Get UE based on its id.
     *
     * @param ueId UE id
     * @return UE entity if found
     */
    public RnibUe get(Long ueId) {
        return xranStore.getUe(ueId);
    }

    /**
     * Remove UE based on its id.
     *
     * @param ueId UE id
     * @return true if remove succeeded
     */
    public boolean remove(Long ueId) {
        crntUe.inverse().remove(ueId);
        return xranStore.removeUe(ueId);
    }
}

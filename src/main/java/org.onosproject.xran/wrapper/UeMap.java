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

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.channel.ChannelHandlerContext;
import javafx.util.Pair;
import org.onosproject.net.HostId;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.CRNTI;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.ENBUES1APID;
import org.onosproject.xran.codecs.api.MMEUES1APID;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibUe;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UeMap {
    private BiMap<EcgiCrntiPair, MMEUES1APID> crntUe = HashBiMap.create();

    private XranStore xranStore;

    public UeMap(XranStore xranStore) {
        this.xranStore = xranStore;
    }

    public BiMap<EcgiCrntiPair, MMEUES1APID> getCrntUe() {
        return crntUe;
    }

    public void putCrnti(RnibCell cell, RnibUe ue) {
        CRNTI ranId = ue.getRanId();
        ECGI ecgi = cell.getEcgi();
        if (ranId != null && ecgi != null) {
            crntUe.put(EcgiCrntiPair.valueOf(cell.getEcgi(),ue.getRanId()), ue.getMmeS1apId());
        }
    }

    public void put(RnibCell cell, RnibUe ue) {

        MMEUES1APID mmeS1apId = ue.getMmeS1apId();
        if (mmeS1apId != null) {
            xranStore.storeUe(ue);
            putCrnti(cell, ue);
        }
    }

    public RnibUe get(ECGI ecgi, CRNTI crnti) {
        MMEUES1APID mme = crntUe.get(EcgiCrntiPair.valueOf(ecgi, crnti));
        if (mme != null) {
            return xranStore.getUe(mme);
        }
        return null;
    }

    public RnibUe get(MMEUES1APID mme) {
        if (mme != null) {
            return xranStore.getUe(mme);
        }
        return null;
    }

    public boolean remove(MMEUES1APID id) {
        crntUe.inverse().remove(id);
        return xranStore.removeUe(id);
    }

    public static class EcgiCrntiPair extends Pair<ECGI, CRNTI> {

        /**
         * Creates a new pair
         *
         * @param key   The key for this pair
         * @param value The value to use for this pair
         */
        public EcgiCrntiPair(ECGI key, CRNTI value) {
            super(key, value);
        }

        public static EcgiCrntiPair valueOf(ECGI key, CRNTI value) {
            return new EcgiCrntiPair(key, value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKey(), getValue());
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof EcgiCrntiPair) {
                return ((EcgiCrntiPair) o).getKey().equals(getKey()) &&
                        ((EcgiCrntiPair) o).getValue().equals(getValue());
            }
            return super.equals(o);
        }
    }
}

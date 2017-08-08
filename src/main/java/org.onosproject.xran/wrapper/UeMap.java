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
import io.netty.channel.ChannelHandlerContext;
import org.onosproject.net.HostId;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.CRNTI;
import org.onosproject.xran.codecs.api.ENBUES1APID;
import org.onosproject.xran.codecs.api.MMEUES1APID;
import org.onosproject.xran.entities.RnibUe;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UeMap {
    private BiMap<CRNTI, MMEUES1APID> crntUe = HashBiMap.create();

    private XranStore xranStore;

    public UeMap(XranStore xranStore) {
        this.xranStore = xranStore;
    }

    public void put(RnibUe ue) {

        MMEUES1APID mmeS1apId = ue.getMmeS1apId();
        if (mmeS1apId != null) {
            xranStore.storeUe(ue);
        }

        CRNTI ranId = ue.getRanId();
        if (ranId != null) {
            crntUe.put(ranId, ue.getMmeS1apId());
        }
    }

    public RnibUe get(CRNTI id) {
        MMEUES1APID mme = crntUe.get(id);
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
}

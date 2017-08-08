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
import org.onosproject.net.DeviceId;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.PCIARFCN;
import org.onosproject.xran.entities.RnibCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CellMap {
    private ConcurrentMap<ECGI, ChannelHandlerContext> ecgiCtx = new ConcurrentHashMap<>();

    private BiMap<PCIARFCN, ECGI> pciarfcnMap = HashBiMap.create();
    private XranStore xranStore;

    public CellMap(XranStore xranStore) {
        this.xranStore = xranStore;
    }

    public void putPciArfcn(RnibCell value) {
        PCIARFCN pciarfcn = new PCIARFCN();
        pciarfcn.setPci(value.getConf().getPci());
        pciarfcn.setEarfcnDl(value.getConf().getEarfcnDl());
        pciarfcnMap.put(pciarfcn, value.getEcgi());
    }

    public void put(RnibCell value, ChannelHandlerContext ctx) {
        if (value.getEcgi() != null) {
            ecgiCtx.put(value.getEcgi(), ctx);
            xranStore.storeCell(value);
        }
    }

    public RnibCell get(PCIARFCN id) {
        ECGI ecgi = null;
        ecgi = pciarfcnMap.get(id);

        if (ecgi != null) {
            return xranStore.getCell(ecgi);
        }
        return null;
    }

    public RnibCell get(ECGI ecgi) {
        if (ecgi != null) {
            return xranStore.getCell(ecgi);
        }
        return null;
    }

    public boolean remove(Object key) {
        ECGI ecgi = null;
        if (key instanceof ECGI) {
            ecgi = (ECGI) key;
        } else if (key instanceof PCIARFCN) {
            ecgi = pciarfcnMap.get(key);
        }

        if (ecgi != null) {
            pciarfcnMap.inverse().remove(ecgi);
            ecgiCtx.remove(ecgi);
        }

        return ecgi != null && xranStore.removeCell(ecgi);
    }

    public ChannelHandlerContext getCtx(ECGI id) {
        return ecgiCtx.get(id);
    }

}

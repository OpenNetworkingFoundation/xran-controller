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
import io.netty.channel.ChannelHandlerContext;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.PCIARFCN;
import org.onosproject.xran.entities.RnibCell;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * CELL wrapper to help put/get/remove.
 */
public class CellMap {
    // map to get the context channel based on ecgi
    private ConcurrentMap<ECGI, ChannelHandlerContext> ecgiCtx = new ConcurrentHashMap<>();

    // pci-arfcn to ecgi bimap
    private BiMap<PCIARFCN, ECGI> pciarfcnMap = HashBiMap.create();
    private XranStore xranStore;

    public CellMap(XranStore xranStore) {
        this.xranStore = xranStore;
    }

    /**
     * Put the PCI-ARFCN to ECGI map from new cell.
     *
     * @param value CELL entity
     */
    public void putPciArfcn(RnibCell value) {
        PCIARFCN pciarfcn = PCIARFCN.valueOf(value.getConf().getPci(), value.getConf().getEarfcnDl());
        pciarfcnMap.put(pciarfcn, value.getEcgi());
    }

    /**
     * Put inside ECGI to CTX map.
     *
     * @param value CELL entity to get ECGI from
     * @param ctx   context channel
     */
    public void put(RnibCell value, ChannelHandlerContext ctx) {
        if (value.getEcgi() != null) {
            ecgiCtx.put(value.getEcgi(), ctx);
            xranStore.storeCell(value);
        }
    }

    /**
     * Get cell based on PCI-ARFCN.
     *
     * @param id PCI-ARFCN
     * @return CELL entity if found
     */
    public RnibCell get(PCIARFCN id) {
        ECGI ecgi;
        ecgi = pciarfcnMap.get(id);

        if (ecgi != null) {
            return xranStore.getCell(ecgi);
        }
        return null;
    }

    /**
     * Get cell based on ECGI.
     *
     * @param ecgi CELL ECGI
     * @return CELL entity if found
     */
    public RnibCell get(ECGI ecgi) {
        if (ecgi != null) {
            return xranStore.getCell(ecgi);
        }
        return null;
    }

    /**
     * Remove cell from three maps based on ECGI or PCI-ARFCN.
     *
     * @param key ecgECGIi or pci-arfcn of cell to remove
     * @return true if remove succeeded
     */
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

    /**
     * Get context handler for specified ECGI.
     *
     * @param id CELL ECGI
     * @return context handler if found
     */
    public ChannelHandlerContext getCtx(ECGI id) {
        return ecgiCtx.get(id);
    }

}

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

package org.onosproject.xran.identifiers;

import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.MMEUES1APID;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibUe;

public class LinkId {
    private RnibCell cell;
    private RnibUe ue;

    private LinkId(RnibCell cell, RnibUe ue) {
        this.cell = cell;
        this.ue = ue;
    }

    public static LinkId valueOf(RnibCell cell, RnibUe ue) {
        return new LinkId(cell, ue);
    }

    public static LinkId valueOf(ECGI ecgi, MMEUES1APID mmeues1APID) {
        RnibCell cell = new RnibCell();
        RnibUe ue = new RnibUe();

        cell.setEcgi(ecgi);
        ue.setMmeS1apId(mmeues1APID);
        return new LinkId(cell, ue);
    }

    public ECGI getEcgi() {
        return cell.getEcgi();
    }

    public void setEcgi(ECGI sourceId) {
        cell.setEcgi(sourceId);
    }

    public MMEUES1APID getMmeues1apid() {
        return ue.getMmeS1apId();
    }

    public void setMmeues1apid(MMEUES1APID destinationId) {
        ue.setMmeS1apId(destinationId);
    }

    public RnibCell getCell() {
        return cell;
    }

    public void setCell(RnibCell cell) {
        this.cell = cell;
    }

    public RnibUe getUe() {
        return ue;
    }

    public void setUe(RnibUe ue) {
        this.ue = ue;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                o != null &&
                        o instanceof LinkId &&
                        cell.getEcgi().equals(((LinkId) o).cell.getEcgi()) &&
                        ue.getMmeS1apId().equals(((LinkId) o).ue.getMmeS1apId());

    }

    @Override
    public int hashCode() {
        int result = cell.getEcgi().hashCode();
        result = 31 * result + ue.getMmeS1apId().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append(cell != null ? "\"cell\":" + cell : "")
                .append(ue != null ? ",\n\"ue\":" + ue : "")
                .append("\n}\n");
        return sb.toString();
    }
}

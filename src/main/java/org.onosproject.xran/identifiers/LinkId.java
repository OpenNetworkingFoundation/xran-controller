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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibUe;

@JsonPropertyOrder({
        "ECGI",
        "UEID"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkId {
    @JsonIgnore
    private RnibCell cell;
    @JsonIgnore
    private RnibUe ue;

    private LinkId(RnibCell cell, RnibUe ue) {
        this.cell = cell;
        this.ue = ue;
    }

    public static LinkId valueOf(RnibCell cell, RnibUe ue) {
        return new LinkId(cell, ue);
    }

    public static LinkId valueOf(ECGI ecgi, Long UeId) {
        RnibCell cell = new RnibCell();
        RnibUe ue = new RnibUe();

        cell.setEcgi(ecgi);
        ue.setId(UeId);
        return new LinkId(cell, ue);
    }

    @JsonProperty("ECGI")
    public ECGI getEcgi() {
        return cell.getEcgi();
    }

    @JsonProperty("ECGI")
    public void setEcgi(ECGI sourceId) {
        cell.setEcgi(sourceId);
    }

    @JsonProperty("UEID")
    public Long getUeId() {
        return ue.getId();
    }

    @JsonProperty("UEID")
    public void setUeId(Long destinationId) {
        ue.setId(destinationId);
    }

    @JsonIgnore
    public RnibCell getCell() {
        return cell;
    }

    @JsonIgnore
    public void setCell(RnibCell cell) {
        this.cell = cell;
    }

    @JsonIgnore
    public RnibUe getUe() {
        return ue;
    }

    @JsonIgnore
    public void setUe(RnibUe ue) {
        this.ue = ue;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                o != null &&
                        o instanceof LinkId &&
                        cell.getEcgi().equals(((LinkId) o).cell.getEcgi()) &&
                        ue.getId().equals(((LinkId) o).ue.getId());

    }

    @Override
    public int hashCode() {
        int result = cell.getEcgi().hashCode();
        result = 31 * result + ue.getId().hashCode();
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

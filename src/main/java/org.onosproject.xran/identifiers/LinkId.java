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
    private ECGI source;
    private MMEUES1APID destination;

    public LinkId(ECGI source, MMEUES1APID destination) {
        this.source = source;
        this.destination = destination;
    }

    public static LinkId valueOf(RnibCell cell, RnibUe ue) {
        return new LinkId(cell.getEcgi(), ue.getMmeS1apId());
    }

    public ECGI getSource() {
        return source;
    }

    public void setSource(ECGI source) {
        this.source = source;
    }

    public MMEUES1APID getDestination() {
        return destination;
    }

    public void setDestination(MMEUES1APID destination) {
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                o != null &&
                        o instanceof LinkId &&
                        destination.equals(((LinkId) o).destination) &&
                        source.equals(((LinkId) o).source);

    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + destination.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append(source != null ? "\"cell\":" + source : "")
                .append(destination != null ? ",\n\"ue\":" + destination : "")
                .append("\n}\n");
        return sb.toString();
    }
}

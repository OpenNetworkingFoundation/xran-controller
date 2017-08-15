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

package org.onosproject.xran.entities;

import com.fasterxml.jackson.databind.JsonNode;
import org.onosproject.net.DeviceId;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.PRBUsage;
import org.onosproject.xran.codecs.pdu.CellConfigReport;
import org.onosproject.xran.codecs.pdu.L2MeasConfig;
import org.onosproject.xran.codecs.pdu.RRMConfig;
import org.onosproject.xran.codecs.pdu.SchedMeasReportPerCell;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerInteger;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by dimitris on 7/22/17.
 */
public class RnibCell {
    private static final String SCHEME = "xran";

    private ECGI ecgi;
    private CellConfigReport conf;
    private PrbUsageContainer prbUsage;
    private SchedMeasReportPerCell.QciVals qci;
    private RRMConfig rrmConfig;
    private L2MeasConfig measConfig;

    private String version;

    public RnibCell() {
        prbUsage = new PrbUsageContainer();
        version = "3";

        rrmConfig = new RRMConfig();
        rrmConfig.setEcgi(ecgi);
    }

    public static URI uri(ECGI ecgi) {
        if (ecgi != null) {
            try {
                BerByteArrayOutputStream os = new BerByteArrayOutputStream(4096);
                ecgi.encode(os);
                String message = DatatypeConverter.printHexBinary(os.getArray());
                return new URI(SCHEME, message, null);
            } catch (URISyntaxException | IOException e) {
                return null;
            }
        }
        return null;
    }

    public static ECGI decodeDeviceId(DeviceId deviceId) throws IOException {
        String uri = deviceId.toString();
        String hexEcgi = uri.substring(uri.lastIndexOf("xran:") + 5);

        ECGI ecgi = new ECGI();
        byte[] bytearray = DatatypeConverter.parseHexBinary(hexEcgi);
        InputStream inputStream = new ByteArrayInputStream(bytearray);

        ecgi.decode(inputStream);
        return ecgi;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public RRMConfig getRrmConfig() {
        return rrmConfig;
    }

    public void setRrmConfig(RRMConfig rrmConfig) {
        this.rrmConfig = rrmConfig;
    }

    public PrbUsageContainer getPrbUsage() {
        return prbUsage;
    }

    public void setPrbUsage(PrbUsageContainer prbUsage) {
        this.prbUsage = prbUsage;
    }

    public ECGI getEcgi() {
        return ecgi;
    }

    public void setEcgi(ECGI ecgi) {
        this.ecgi = ecgi;
    }

    public CellConfigReport getConf() {
        return conf;
    }

    /*public RRMConfig getRrmConfig() {
        return rrmConfig;
    }*/

    public void setConf(CellConfigReport conf) {
        this.conf = conf;
    }

    public void modifyRrmConfig(JsonNode rrmConfigNode, List<RnibUe> ueList) {
        RRMConfig.Crnti crnti = new RRMConfig.Crnti();
        ueList.forEach(ue -> crnti.addCRNTI(ue.getRanId()));

        {
            JsonNode start_prb_dl = rrmConfigNode.get("start_prb_dl");
            if (start_prb_dl != null) {
                RRMConfig.StartPrbDl startPrbDl = new RRMConfig.StartPrbDl();
                if (start_prb_dl.isArray()) {
                    if (ueList.size() == start_prb_dl.size()) {
                        List<BerInteger> collect = Stream.of(start_prb_dl)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        startPrbDl.setSeqOf(collect);
                    }
                }
                rrmConfig.setStartPrbDl(startPrbDl);
            }
        }

        {
            JsonNode end_prb_dl = rrmConfigNode.get("end_prb_dl");
            if (end_prb_dl != null) {
                RRMConfig.EndPrbDl endPrbDl = new RRMConfig.EndPrbDl();
                if (end_prb_dl.isArray()) {
                    if (ueList.size() == end_prb_dl.size()) {
                        List<BerInteger> collect = Stream.of(end_prb_dl)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        endPrbDl.setSeqOf(collect);
                    }
                }
                rrmConfig.setEndPrbDl(endPrbDl);
            }
        }

        {
            JsonNode start_prb_ul = rrmConfigNode.get("start_prb_ul");
            if (start_prb_ul != null) {
                RRMConfig.StartPrbUl startPrbUl = new RRMConfig.StartPrbUl();
                if (start_prb_ul.isArray()) {
                    if (ueList.size() == start_prb_ul.size()) {
                        List<BerInteger> collect = Stream.of(start_prb_ul)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        startPrbUl.setSeqOf(collect);
                    }
                }
                rrmConfig.setStartPrbUl(startPrbUl);
            }
        }

        {
            JsonNode end_prb_ul = rrmConfigNode.get("end_prb_ul");
            if (end_prb_ul != null) {
                RRMConfig.EndPrbUl endPrbUl = new RRMConfig.EndPrbUl();
                if (end_prb_ul.isArray()) {
                    if (ueList.size() == end_prb_ul.size()) {
                        List<BerInteger> collect = Stream.of(end_prb_ul)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        endPrbUl.setSeqOf(collect);
                    }
                }
                rrmConfig.setEndPrbUl(endPrbUl);
            }
        }

        rrmConfig.setCrnti(crnti);
    }

    public SchedMeasReportPerCell.QciVals getQci() {
        return qci;
    }

    public void setQci(SchedMeasReportPerCell.QciVals qci) {
        this.qci = qci;
    }

    public void setPrimaryPrbUsage(PRBUsage primary) {
        this.prbUsage.primary = primary;
    }

    public void setSecondaryPrbUsage(PRBUsage secondary) {
        this.prbUsage.secondary = secondary;
    }

    public L2MeasConfig getMeasConfig() {
        return measConfig;
    }

    public void setMeasConfig(L2MeasConfig measConfig) {
        this.measConfig = measConfig;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append(ecgi != null ? "\"ecgi\":" + ecgi : "")
                .append(conf != null ? ",\n\"config-report\":" + conf : "")
                .append(prbUsage != null ? ",\n\"prb-usage\":" + prbUsage : "")
                .append(qci != null ? ",\n\"qci-vals\":" + qci : "")
                .append(rrmConfig != null ? ",\n\"rrm-config\":" + rrmConfig : "")
                .append(measConfig != null ? ",\n\"l2-meas-config\":" + measConfig : "")
                .append("\n}\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RnibCell rnibCell = (RnibCell) o;

        return ecgi.equals(rnibCell.ecgi);
    }

    @Override
    public int hashCode() {
        return ecgi.hashCode();
    }

    class PrbUsageContainer {
        PRBUsage primary;
        PRBUsage secondary;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n")
                    .append(primary != null ? "\"primary\":" + primary : "")
                    .append(secondary != null ? ",\n\"secondary\":" + secondary : "")
                    .append("\n}\n");
            return sb.toString();
        }
    }
}

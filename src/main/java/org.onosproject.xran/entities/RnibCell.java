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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.onosproject.net.DeviceId;
import org.onosproject.store.Timestamp;
import org.onosproject.store.service.WallClockTimestamp;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.PRBUsage;
import org.onosproject.xran.codecs.api.XICICPA;
import org.onosproject.xran.codecs.ber.BerByteArrayOutputStream;
import org.onosproject.xran.codecs.ber.types.BerBitString;
import org.onosproject.xran.codecs.ber.types.BerInteger;
import org.onosproject.xran.codecs.pdu.CellConfigReport;
import org.onosproject.xran.codecs.pdu.L2MeasConfig;
import org.onosproject.xran.codecs.pdu.RRMConfig;
import org.onosproject.xran.codecs.pdu.SchedMeasReportPerCell;

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

@JsonPropertyOrder({
        "ECGI",
        "Configuration",
        "PRB-Usage",
        "QCI",
        "RRMConfiguration",
        "MeasurementConfiguration"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class RnibCell {
    @JsonIgnore
    private static final String SCHEME = "xran";

    @JsonProperty("ECGI")
    private ECGI ecgi;
    @JsonProperty("Configuration")
    private CellConfigReport conf;
    @JsonProperty("PRB-Usage")
    private PrbUsageContainer prbUsage;
    @JsonProperty("QCI")
    private SchedMeasReportPerCell.QciVals qci;
    @JsonProperty("RRMConfiguration")
    private RRMConfig rrmConfig;
    @JsonProperty("MeasurementConfiguration")
    private L2MeasConfig measConfig;

    @JsonIgnore
    private String version;

    public RnibCell() {
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

    public int getVersion() {
        return Integer.parseInt(version);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("RRMConfiguration")
    public RRMConfig getRrmConfig() {
        return rrmConfig;
    }

    @JsonProperty("RRMConfiguration")
    public void setRrmConfig(RRMConfig rrmConfig) {
        this.rrmConfig = rrmConfig;
    }

    @JsonProperty("PRB-Usage")
    public PrbUsageContainer getPrbUsage() {
        return prbUsage;
    }

    @JsonProperty("PRB-Usage")
    public void setPrbUsage(PrbUsageContainer prbUsage) {
        this.prbUsage = prbUsage;
    }

    @JsonProperty("ECGI")
    public ECGI getEcgi() {
        return ecgi;
    }

    @JsonProperty("ECGI")
    public void setEcgi(ECGI ecgi) {
        this.ecgi = ecgi;
    }

    @JsonProperty("Configuration")
    public CellConfigReport getConf() {
        return conf;
    }

    @JsonProperty("Configuration")
    public void setConf(CellConfigReport conf) {
        this.conf = conf;
    }

    public void modifyRrmConfig(JsonNode rrmConfigNode, List<RnibUe> ueList) throws Exception {
        RRMConfig.Crnti crnti = new RRMConfig.Crnti();
        ueList.forEach(ue -> crnti.addCRNTI(ue.getRanId()));

        {
            JsonNode p_a = rrmConfigNode.path("p_a");
            if (!p_a.isMissingNode()) {
                RRMConfig.Pa pa = new RRMConfig.Pa();
                if (p_a.isArray()) {
                    if (ueList.size() == p_a.size()) {
                        List<XICICPA> collect = Stream.of(p_a)
                                .map(val -> new XICICPA(val.asInt()))
                                .collect(Collectors.toList());
                        pa.setXICICPA(collect);
                    } else {
                        throw new Exception("p_a size is not the same as UE size");
                    }
                }
                rrmConfig.setPa(pa);
            }
        }

        {
            JsonNode start_prb_dl = rrmConfigNode.path("start_prb_dl");
            if (!start_prb_dl.isMissingNode()) {
                RRMConfig.StartPrbDl startPrbDl = new RRMConfig.StartPrbDl();
                if (start_prb_dl.isArray()) {
                    if (ueList.size() == start_prb_dl.size()) {
                        List<BerInteger> collect = Stream.of(start_prb_dl)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        startPrbDl.setSeqOf(collect);
                    } else {
                        throw new Exception("start_prb_dl size is not the same as UE size");
                    }
                }
                rrmConfig.setStartPrbDl(startPrbDl);
            }
        }

        {
            JsonNode end_prb_dl = rrmConfigNode.path("end_prb_dl");
            if (!end_prb_dl.isMissingNode()) {
                RRMConfig.EndPrbDl endPrbDl = new RRMConfig.EndPrbDl();
                if (end_prb_dl.isArray()) {
                    if (ueList.size() == end_prb_dl.size()) {
                        List<BerInteger> collect = Stream.of(end_prb_dl)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        endPrbDl.setSeqOf(collect);
                    } else {
                        throw new Exception("end_prb_dl size is not the same as UE size");
                    }
                }
                rrmConfig.setEndPrbDl(endPrbDl);
            }
        }

        {
            JsonNode sub_frame_bitmask_dl = rrmConfigNode.path("sub_frame_bitmask_dl");
            if (!sub_frame_bitmask_dl.isMissingNode()) {
                RRMConfig.SubframeBitmaskDl subframeBitmaskDl = new RRMConfig.SubframeBitmaskDl();
                if (sub_frame_bitmask_dl.isArray()) {
                    List<BerBitString> collect = Stream.of(sub_frame_bitmask_dl)
                            .map(val -> new BerBitString(DatatypeConverter.parseHexBinary(val.asText()), 10))
                            .collect(Collectors.toList());

                    subframeBitmaskDl.setSeqOf(collect);
                } else {
                    throw new Exception("sub_frame_bitmask_dl size is not the same as UE size");
                }
                rrmConfig.setSubframeBitmaskDl(subframeBitmaskDl);
            }
        }

        {
            JsonNode start_prb_ul = rrmConfigNode.path("start_prb_ul");
            if (!start_prb_ul.isMissingNode()) {
                RRMConfig.StartPrbUl startPrbUl = new RRMConfig.StartPrbUl();
                if (start_prb_ul.isArray()) {
                    if (ueList.size() == start_prb_ul.size()) {
                        List<BerInteger> collect = Stream.of(start_prb_ul)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        startPrbUl.setSeqOf(collect);
                    } else {
                        throw new Exception("start_prb_ul size is not the same as UE size");
                    }
                }
                rrmConfig.setStartPrbUl(startPrbUl);
            }
        }

        {
            JsonNode end_prb_ul = rrmConfigNode.path("end_prb_ul");
            if (!end_prb_ul.isMissingNode()) {
                RRMConfig.EndPrbUl endPrbUl = new RRMConfig.EndPrbUl();
                if (end_prb_ul.isArray()) {
                    if (ueList.size() == end_prb_ul.size()) {
                        List<BerInteger> collect = Stream.of(end_prb_ul)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        endPrbUl.setSeqOf(collect);
                    } else {
                        throw new Exception("end_prb_ul size is not the same as UE size");
                    }
                }
                rrmConfig.setEndPrbUl(endPrbUl);
            }
        }

        {
            JsonNode p0_ue_pusch = rrmConfigNode.path("p0_ue_pusch");
            if (!p0_ue_pusch.isMissingNode()) {
                RRMConfig.P0UePusch p0UePusch = new RRMConfig.P0UePusch();
                if (p0_ue_pusch.isArray()) {
                    if (ueList.size() == p0_ue_pusch.size()) {
                        List<BerInteger> collect = Stream.of(p0_ue_pusch)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        p0UePusch.setSeqOf(collect);
                    } else {
                        throw new Exception("p0_ue_pusch size is not the same as UE size");
                    }
                }
                rrmConfig.setP0UePusch(p0UePusch);
            }
        }

        {
            JsonNode sub_frame_bitmask_ul = rrmConfigNode.path("sub_frame_bitmask_ul");
            if (!sub_frame_bitmask_ul.isMissingNode()) {
                RRMConfig.SubframeBitmaskUl subframeBitmaskUl = new RRMConfig.SubframeBitmaskUl();
                if (sub_frame_bitmask_ul.isArray()) {
                    List<BerBitString> collect = Stream.of(sub_frame_bitmask_ul)
                            .map(val -> new BerBitString(DatatypeConverter.parseHexBinary(val.asText()), 10))
                            .collect(Collectors.toList());

                    subframeBitmaskUl.setSeqOf(collect);
                } else {
                    throw new Exception("sub_frame_bitmask_ul size is not the same as UE size");
                }
                rrmConfig.setSubframeBitmaskUl(subframeBitmaskUl);
            }
        }

        rrmConfig.setCrnti(crnti);
    }

    @JsonProperty("QCI")
    public SchedMeasReportPerCell.QciVals getQci() {
        return qci;
    }

    @JsonProperty("QCI")
    public void setQci(SchedMeasReportPerCell.QciVals qci) {
        this.qci = qci;
    }

    @JsonProperty("MeasurementConfiguration")
    public L2MeasConfig getMeasConfig() {
        return measConfig;
    }

    @JsonProperty("MeasurementConfiguration")
    public void setMeasConfig(L2MeasConfig measConfig) {
        this.measConfig = measConfig;
    }

    @Override
    public String toString() {
        return "RnibCell{" +
                "ecgi=" + ecgi +
                ", conf=" + conf +
                ", prbUsage=" + prbUsage +
                ", qci=" + qci +
                ", rrmConfig=" + rrmConfig +
                ", measConfig=" + measConfig +
                ", version='" + version + '\'' +
                '}';
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

    @JsonPropertyOrder({
            "primary",
            "secondary",
            "timesincelastupdate"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrbUsageContainer {
        PRBUsage primary;
        PRBUsage secondary;
        WallClockTimestamp timesincelastupdate;

        @JsonCreator
        public PrbUsageContainer(@JsonProperty("primary") PRBUsage primary, @JsonProperty("secondary") PRBUsage secondary) {
            this.primary = primary;
            this.secondary = secondary;
            this.timesincelastupdate = new WallClockTimestamp();
        }

        public PRBUsage getPrimary() {
            return primary;
        }

        public void setPrimary(PRBUsage primary) {
            this.primary = primary;
        }

        public PRBUsage getSecondary() {
            return secondary;
        }

        public void setSecondary(PRBUsage secondary) {
            this.secondary = secondary;
        }

        public long getTimesincelastupdate() {
            return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
        }

        public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
            this.timesincelastupdate = timesincelastupdate;
        }

        @Override
        public String toString() {
            return "PrbUsageContainer{" +
                    "primary=" + primary +
                    ", secondary=" + secondary +
                    ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp()) +
                    '}';
        }
    }
}

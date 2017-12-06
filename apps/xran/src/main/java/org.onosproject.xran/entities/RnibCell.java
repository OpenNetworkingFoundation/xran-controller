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

package org.onosproject.xran.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import org.onosproject.net.DeviceId;
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
 * R-NIB Cell and its properties.
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

    /**
     * Encode ECGI and obtain its URI.
     *
     * @param ecgi ECGI
     * @return URI
     */
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

    /**
     * Obtain ECGI from the device ID.
     *
     * @param deviceId ID of the device
     * @return ECGI
     * @throws IOException I0 Exception for ByteArrayInputStream
     */
    public static ECGI decodeDeviceId(DeviceId deviceId) throws IOException {
        String uri = deviceId.toString();
        String hexEcgi = uri.substring(uri.lastIndexOf("xran:") + 5);

        ECGI ecgi = new ECGI();
        byte[] bytearray = DatatypeConverter.parseHexBinary(hexEcgi);
        InputStream inputStream = new ByteArrayInputStream(bytearray);

        ecgi.decode(inputStream);
        return ecgi;
    }

    /**
     * Get version ID.
     *
     * @return version ID
     */
    public int getVersion() {
        return Integer.parseInt(version);
    }

    /**
     * Set version ID.
     *
     * @param version version ID
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get RRMConfig.
     *
     * @return RRMConfig
     */
    @JsonProperty("RRMConfiguration")
    public RRMConfig getRrmConfig() {
        return rrmConfig;
    }

    /**
     * Set RRMConfig properties.
     *
     * @param rrmConfig RRMConfig
     */
    @JsonProperty("RRMConfiguration")
    public void setRrmConfig(RRMConfig rrmConfig) {
        this.rrmConfig = rrmConfig;
    }

    /**
     * Get PRB Usage.
     * @return prb usage
     */
    @JsonProperty("PRB-Usage")
    public PrbUsageContainer getPrbUsage() {
        return prbUsage;
    }

    /**
     * Set PRB Usage.
     *
     * @param prbUsage prb Usage
     */
    @JsonProperty("PRB-Usage")
    public void setPrbUsage(PrbUsageContainer prbUsage) {
        this.prbUsage = prbUsage;
    }

    /**
     * Get ECGI.
     *
     * @return ECGI
     */
    @JsonProperty("ECGI")
    public ECGI getEcgi() {
        return ecgi;
    }

    /**
     * Set ECGI.
     *
     * @param ecgi ECGI
     */
    @JsonProperty("ECGI")
    public void setEcgi(ECGI ecgi) {
        this.ecgi = ecgi;
    }

    /**
     * Get cell config report.
     *
     * @return CellConfig Report
     */
    @JsonProperty("Configuration")
    public CellConfigReport getConf() {
        return conf;
    }

    /**
     * Set cell config report.
     *
     * @param conf Cell config report
     */
    @JsonProperty("Configuration")
    public void setConf(CellConfigReport conf) {
        this.conf = conf;
    }

    /**
     * Modify the RRM Config parameters of cell.
     *
     * @param rrmConfigNode RRMConfig parameters to modify obtained from REST call
     * @param ueList List of all UEs
     * @throws Exception p_a size not equal to UE size
     */
    public void modifyRrmConfig(JsonNode rrmConfigNode, List<RnibUe> ueList) throws Exception {
        RRMConfig.Crnti crnti = new RRMConfig.Crnti();
        ueList.forEach(ue -> crnti.addCRNTI(ue.getCrnti()));

            JsonNode pA = rrmConfigNode.path("p_a");
            if (!pA.isMissingNode()) {
                RRMConfig.Pa pa = new RRMConfig.Pa();
                if (pA.isArray()) {
                    if (ueList.size() == pA.size()) {
                        List<XICICPA> collect = Stream.of(pA)
                                .map(val -> new XICICPA(val.asInt()))
                                .collect(Collectors.toList());
                        pa.setXICICPA(collect);
                    } else {
                        throw new Exception("p_a size is not the same as UE size");
                    }
                }
                rrmConfig.setPa(pa);
            }

            JsonNode startPrbDl1 = rrmConfigNode.path("start_prb_dl");
            if (!startPrbDl1.isMissingNode()) {
                RRMConfig.StartPrbDl startPrbDl = new RRMConfig.StartPrbDl();
                if (startPrbDl1.isArray()) {
                    if (ueList.size() == startPrbDl1.size()) {
                        List<BerInteger> collect = Stream.of(startPrbDl1)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        startPrbDl.setSeqOf(collect);
                    } else {
                        throw new Exception("start_prb_dl size is not the same as UE size");
                    }
                }
                rrmConfig.setStartPrbDl(startPrbDl);
            }

            JsonNode endPrbDl1 = rrmConfigNode.path("end_prb_dl");
            if (!endPrbDl1.isMissingNode()) {
                RRMConfig.EndPrbDl endPrbDl = new RRMConfig.EndPrbDl();
                if (endPrbDl1.isArray()) {
                    if (ueList.size() == endPrbDl1.size()) {
                        List<BerInteger> collect = Stream.of(endPrbDl1)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        endPrbDl.setSeqOf(collect);
                    } else {
                        throw new Exception("end_prb_dl size is not the same as UE size");
                    }
                }
                rrmConfig.setEndPrbDl(endPrbDl);
            }

            JsonNode frameBitmaskDl = rrmConfigNode.path("sub_frame_bitmask_dl");
            if (!frameBitmaskDl.isMissingNode()) {
                RRMConfig.SubframeBitmaskDl subframeBitmaskDl = new RRMConfig.SubframeBitmaskDl();
                if (frameBitmaskDl.isArray()) {
                    List<BerBitString> collect = Stream.of(frameBitmaskDl)
                            .map(val -> new BerBitString(DatatypeConverter.parseHexBinary(val.asText()), 10))
                            .collect(Collectors.toList());

                    subframeBitmaskDl.setSeqOf(collect);
                } else {
                    throw new Exception("sub_frame_bitmask_dl size is not the same as UE size");
                }
                rrmConfig.setSubframeBitmaskDl(subframeBitmaskDl);
            }

            JsonNode startPrbUl1 = rrmConfigNode.path("start_prb_ul");
            if (!startPrbUl1.isMissingNode()) {
                RRMConfig.StartPrbUl startPrbUl = new RRMConfig.StartPrbUl();
                if (startPrbUl1.isArray()) {
                    if (ueList.size() == startPrbUl1.size()) {
                        List<BerInteger> collect = Stream.of(startPrbUl1)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        startPrbUl.setSeqOf(collect);
                    } else {
                        throw new Exception("start_prb_ul size is not the same as UE size");
                    }
                }
                rrmConfig.setStartPrbUl(startPrbUl);
            }

            JsonNode endPrbUl1 = rrmConfigNode.path("end_prb_ul");
            if (!endPrbUl1.isMissingNode()) {
                RRMConfig.EndPrbUl endPrbUl = new RRMConfig.EndPrbUl();
                if (endPrbUl1.isArray()) {
                    if (ueList.size() == endPrbUl1.size()) {
                        List<BerInteger> collect = Stream.of(endPrbUl1)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        endPrbUl.setSeqOf(collect);
                    } else {
                        throw new Exception("end_prb_ul size is not the same as UE size");
                    }
                }
                rrmConfig.setEndPrbUl(endPrbUl);
            }

            JsonNode uePusch = rrmConfigNode.path("p0_ue_pusch");
            if (!uePusch.isMissingNode()) {
                RRMConfig.P0UePusch p0UePusch = new RRMConfig.P0UePusch();
                if (uePusch.isArray()) {
                    if (ueList.size() == uePusch.size()) {
                        List<BerInteger> collect = Stream.of(uePusch)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        p0UePusch.setSeqOf(collect);
                    } else {
                        throw new Exception("p0_ue_pusch size is not the same as UE size");
                    }
                }
                rrmConfig.setP0UePusch(p0UePusch);
            }

            JsonNode frameBitmaskUl = rrmConfigNode.path("sub_frame_bitmask_ul");
            if (!frameBitmaskUl.isMissingNode()) {
                RRMConfig.SubframeBitmaskUl subframeBitmaskUl = new RRMConfig.SubframeBitmaskUl();
                if (frameBitmaskUl.isArray()) {
                    List<BerBitString> collect = Stream.of(frameBitmaskUl)
                            .map(val -> new BerBitString(DatatypeConverter.parseHexBinary(val.asText()), 10))
                            .collect(Collectors.toList());

                    subframeBitmaskUl.setSeqOf(collect);
                } else {
                    throw new Exception("sub_frame_bitmask_ul size is not the same as UE size");
                }
                rrmConfig.setSubframeBitmaskUl(subframeBitmaskUl);
            }

        rrmConfig.setCrnti(crnti);
    }

    /**
     * Get QCI values.
     *
     * @return QCI values
     */
    @JsonProperty("QCI")
    public SchedMeasReportPerCell.QciVals getQci() {
        return qci;
    }

    /**
     * Set QCI values.
     *
     * @param qci QCI
     */
    @JsonProperty("QCI")
    public void setQci(SchedMeasReportPerCell.QciVals qci) {
        this.qci = qci;
    }

    /**
     * Get L2 measurement config.
     *
     * @return L2MeasConfig
     */
    @JsonProperty("MeasurementConfiguration")
    public L2MeasConfig getMeasConfig() {
        return measConfig;
    }

    /**
     * Set L2 measurement config.
     *
     * @param measConfig l2MeasConfig
     */
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals()
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RnibCell rnibCell = (RnibCell) o;

        return ecgi.equals(rnibCell.ecgi);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ecgi.hashCode();
    }

    /**
     * Container class for PRBUsage.
     */
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
        public PrbUsageContainer(@JsonProperty("primary") PRBUsage primary,
                                 @JsonProperty("secondary") PRBUsage secondary) {
            this.primary = primary;
            this.secondary = secondary;
            this.timesincelastupdate = new WallClockTimestamp();
        }

        /**
         * Get primary PRBUsage.
         *
         * @return PRBUsage
         */
        public PRBUsage getPrimary() {
            return primary;
        }

        /**
         * Set secondary PRBUsage.
         *
         * @param primary PRBUsage
         */
        public void setPrimary(PRBUsage primary) {
            this.primary = primary;
        }

        /**
         * Get secondary PRBUsage.
         *
         * @return PRBUsage
         */
        public PRBUsage getSecondary() {
            return secondary;
        }

        /**
         * Set secondary PRBUsage.
         *
         * @param secondary PRBUsage
         */
        public void setSecondary(PRBUsage secondary) {
            this.secondary = secondary;
        }

        /**
         * Get time since last update.
         *
         * @return long Time
         */
        public long getTimeSinceLastUpdate() {
            return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
        }


        /**
         * Set time since last update.
         *
         * @param timesincelastupdate time since last update
         */
        public void setTimeSinceLastUpdate(WallClockTimestamp timesincelastupdate) {
            this.timesincelastupdate = timesincelastupdate;
        }

        @Override
        public String toString() {
            return "PrbUsageContainer{" +
                    "primary=" + primary +
                    ", secondary=" + secondary +
                    ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() -
                    timesincelastupdate.unixTimestamp()) +
                    '}';
        }
    }
}

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
import com.google.common.collect.Lists;
import org.onosproject.store.service.WallClockTimestamp;
import org.onosproject.xran.codecs.api.ERABParams;
import org.onosproject.xran.codecs.api.RadioRepPerServCell;
import org.onosproject.xran.codecs.api.TrafficSplitPercentage;
import org.onosproject.xran.codecs.api.XICICPA;
import org.onosproject.xran.codecs.api.SchedMeasRepPerServCell;
import org.onosproject.xran.codecs.api.PRBUsage;
import org.onosproject.xran.codecs.ber.types.BerBitString;
import org.onosproject.xran.codecs.ber.types.BerInteger;
import org.onosproject.xran.codecs.pdu.PDCPMeasReportPerUe;
import org.onosproject.xran.codecs.pdu.RRMConfig;
import org.onosproject.xran.identifiers.LinkId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;

/**
 * R-NIB Link and its properties.
 */
@JsonPropertyOrder({
        "Link-ID",
        "Type",
        "RRMConfiguration",
        "TrafficPercent",
        "BearerParameters",
        "Quality",
        "PDCP-Throughput",
        "PDCP-Packet-Delay",
        "Resource-Usage"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class RnibLink {
    @JsonIgnore
    private static final Logger log =
            LoggerFactory.getLogger(RnibLink.class);

    @JsonProperty("Link-ID")
    private LinkId linkId;
    @JsonProperty("RRMConfiguration")
    private RRMConfig rrmParameters;
    @JsonProperty("TrafficPercent")
    private TrafficSplitPercentage trafficPercent;
    @JsonProperty("BearerParameters")
    private ERABParams bearerParameters;
    @JsonProperty("Quality")
    private LinkQuality quality;
    @JsonProperty("PDCP-Throughput")
    private PdcpThroughput pdcpThroughput;
    @JsonProperty("PDCP-Packet-Delay")
    private PdcpPacketdelay pdcpPackDelay;
    @JsonProperty("Resource-Usage")
    private ResourceUsage resourceUsage;
    @JsonProperty("Type")
    private Type type;
    @JsonIgnore
    private Timer timer;

    public RnibLink(RnibCell cell, RnibUe ue) {
        trafficPercent = new TrafficSplitPercentage();
        trafficPercent.setEcgi(cell.getEcgi());
        trafficPercent.setTrafficPercentDl(new BerInteger(100));
        trafficPercent.setTrafficPercentUl(new BerInteger(100));

        timer = new Timer();

        type = Type.NON_SERVING;

        linkId = LinkId.valueOf(cell, ue);

        quality = new LinkQuality();

        rrmParameters = new RRMConfig();
        RRMConfig.Crnti crnti = new RRMConfig.Crnti();
        crnti.addCRNTI(linkId.getUe().getCrnti());
        rrmParameters.setCrnti(crnti);
        rrmParameters.setEcgi(linkId.getEcgi());
    }

    /**
     * Get timer.
     *
     * @return Timer
     */
    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer.cancel();
        this.timer.purge();
        this.timer = timer;
    }

    /**
     * Get Link ID.
     * @return LinkID
     */
    @JsonProperty("Link-ID")
    public LinkId getLinkId() {
        return linkId;
    }

    /**
     * Set the Link ID.
     * @param linkId Link ID
     */
    @JsonProperty("Link-ID")
    public void setLinkId(LinkId linkId) {
        this.linkId = linkId;
    }

    /**
     * Set the LINK ID with cell and ue.
     * @param cell Rnib CELL
     * @param ue Rnib UE
     */
    public void setLinkId(RnibCell cell, RnibUe ue) {
        this.linkId = LinkId.valueOf(cell, ue);
        trafficPercent.setEcgi(cell.getEcgi());
    }

    /**
     * Get the link type.
     *
     * @return Link-type
     */
    @JsonProperty("Type")
    public Type getType() {
        return type;
    }

    /**
     * Set the link type.
     * @param type Link-type
     */
    @JsonProperty("Type")
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Get traffic percent.
     * @return TrafficSplitPercentage
     */
    @JsonProperty("TrafficPercent")
    public TrafficSplitPercentage getTrafficPercent() {
        return trafficPercent;
    }

    /**
     * Set traffic percent.
     * @param trafficPercent TrafficSplitPercentage
     */
    @JsonProperty("TrafficPercent")
    public void setTrafficPercent(TrafficSplitPercentage trafficPercent) {
        this.trafficPercent = trafficPercent;
    }

    /**
     * Get the Bearer Parameters.
     * @return ERABParams
     */
    @JsonProperty("BearerParameters")
    public ERABParams getBearerParameters() {
        return bearerParameters;
    }

    /**
     * Set the Bearer Parameters.
     * @param bearerParameters ERABParams
     */
    @JsonProperty("BearerParameters")
    public void setBearerParameters(ERABParams bearerParameters) {
        this.bearerParameters = bearerParameters;
    }

    /**
     * Get Quality.
     * @return LinkQuality
     */
    @JsonProperty("Quality")
    public LinkQuality getQuality() {
        return quality;
    }

    /**
     * Set Quality.
     * @param quality LinkQuality
     */
    @JsonProperty("Quality")
    public void setQuality(LinkQuality quality) {
        this.quality = quality;
    }

    /**
     * Get RRM Configuration.
     * @return RRMConfig
     */
    @JsonProperty("RRMConfiguration")
    public RRMConfig getRrmParameters() {
        return rrmParameters;
    }

    /**
     * Set RRM Configuration.
     * @param rrmParameters RRMConfig
     */
    @JsonProperty("RRMConfiguration")
    public void setRrmParameters(RRMConfig rrmParameters) {
        this.rrmParameters = rrmParameters;
    }

    /**
     * Modify the RRM Config parameters of link.
     *
     * @param rrmConfigNode RRMConfig parameters to modify obtained from REST call
     */
    public void modifyRrmParameters(JsonNode rrmConfigNode) {

        JsonNode pA = rrmConfigNode.path("p_a");
        if (!pA.isMissingNode()) {
            RRMConfig.Pa pa = new RRMConfig.Pa();

            List<XICICPA> collect = Lists.newArrayList();
            collect.add(new XICICPA(pA.asInt()));
            pa.setXICICPA(collect);
            rrmParameters.setPa(pa);
        }

        JsonNode startPrbDl1 = rrmConfigNode.path("start_prb_dl");
        if (!startPrbDl1.isMissingNode()) {
            RRMConfig.StartPrbDl startPrbDl = new RRMConfig.StartPrbDl();

            List<BerInteger> collect = Lists.newArrayList();
            collect.add(new BerInteger(startPrbDl1.asInt()));
            startPrbDl.setSeqOf(collect);

            rrmParameters.setStartPrbDl(startPrbDl);
        }

        JsonNode endPrbDl1 = rrmConfigNode.path("end_prb_dl");
        if (!endPrbDl1.isMissingNode()) {
            RRMConfig.EndPrbDl endPrbDl = new RRMConfig.EndPrbDl();

            List<BerInteger> collect = Lists.newArrayList();
            collect.add(new BerInteger(endPrbDl1.asInt()));
            endPrbDl.setSeqOf(collect);

            rrmParameters.setEndPrbDl(endPrbDl);
        }

        JsonNode subFrameBitmaskDl = rrmConfigNode.path("sub_frame_bitmask_dl");
        if (!subFrameBitmaskDl.isMissingNode()) {
            RRMConfig.SubframeBitmaskDl subframeBitmaskDl = new RRMConfig.SubframeBitmaskDl();
            List<BerBitString> collect = Lists.newArrayList();

            byte[] hexString = DatatypeConverter.parseHexBinary(subFrameBitmaskDl.asText());
            collect.add(new BerBitString(hexString, 10));
            subframeBitmaskDl.setSeqOf(collect);
            rrmParameters.setSubframeBitmaskDl(subframeBitmaskDl);
        }

        JsonNode startPrbUl1 = rrmConfigNode.path("start_prb_ul");
        if (!startPrbUl1.isMissingNode()) {
            RRMConfig.StartPrbUl startPrbUl = new RRMConfig.StartPrbUl();

            List<BerInteger> collect = Lists.newArrayList();
            collect.add(new BerInteger(startPrbUl1.asInt()));
            startPrbUl.setSeqOf(collect);

            rrmParameters.setStartPrbUl(startPrbUl);
        }

        JsonNode endPrbUl1 = rrmConfigNode.path("end_prb_ul");
        if (!endPrbUl1.isMissingNode()) {
            RRMConfig.EndPrbUl endPrbUl = new RRMConfig.EndPrbUl();

            List<BerInteger> collect = Lists.newArrayList();
            collect.add(new BerInteger(endPrbUl1.asInt()));
            endPrbUl.setSeqOf(collect);

            rrmParameters.setEndPrbUl(endPrbUl);
        }


        JsonNode p0UePusch1 = rrmConfigNode.path("p0_ue_pusch");
        if (!p0UePusch1.isMissingNode()) {
            RRMConfig.P0UePusch p0UePusch = new RRMConfig.P0UePusch();

            List<BerInteger> collect = Lists.newArrayList();
            collect.add(new BerInteger(p0UePusch1.asInt()));
            p0UePusch.setSeqOf(collect);

            rrmParameters.setP0UePusch(p0UePusch);
        }

        JsonNode subFrameBitmaskUl = rrmConfigNode.path("sub_frame_bitmask_ul");
        if (!subFrameBitmaskUl.isMissingNode()) {
            RRMConfig.SubframeBitmaskUl subframeBitmaskUl = new RRMConfig.SubframeBitmaskUl();
            List<BerBitString> collect = Lists.newArrayList();

            byte[] hexString = DatatypeConverter.parseHexBinary(subFrameBitmaskUl.asText());
            collect.add(new BerBitString(hexString, 10));
            subframeBitmaskUl.setSeqOf(collect);
            rrmParameters.setSubframeBitmaskUl(subframeBitmaskUl);
        }
    }

    /**
     * Get PDCP Throughput.
     * @return PdcpThroughput
     */
    @JsonProperty("PDCP-Throughput")
    public PdcpThroughput getPdcpThroughput() {
        return pdcpThroughput;
    }

    /**
     * Set PDCP Throughput.
     * @param pdcpThroughput PdcpThroughput
     */
    @JsonProperty("PDCP-Throughput")
    public void setPdcpThroughput(PdcpThroughput pdcpThroughput) {
        this.pdcpThroughput = pdcpThroughput;
    }

    /**
     * Get PdcpPackDelay.
     * @return PdcpPacketdelay
     */
    @JsonProperty("PDCP-Packet-Delay")
    public PdcpPacketdelay getPdcpPackDelay() {
        return pdcpPackDelay;
    }

    /**
     * Set PdcpPackDelay.
     * @param pdcpPackDelay PdcpPacketdelay
     */
    @JsonProperty("PDCP-Packet-Delay")
    public void setPdcpPackDelay(PdcpPacketdelay pdcpPackDelay) {
        this.pdcpPackDelay = pdcpPackDelay;
    }

    /**
     * Get ResourceUsage.
     * @return ResourceUsage
     */
    @JsonProperty("Resource-Usage")
    public ResourceUsage getResourceUsage() {
        return resourceUsage;
    }

    /**
     * Set ResourceUsage.
     * @param resourceUsage ResourceUsage
     */
    @JsonProperty("Resource-Usage")
    public void setResourceUsage(ResourceUsage resourceUsage) {
        this.resourceUsage = resourceUsage;
    }

    @Override
    public String toString() {
        return "RnibLink{" +
                "linkId=" + linkId +
                ", rrmParameters=" + rrmParameters +
                ", trafficPercent=" + trafficPercent +
                ", bearerParameters=" + bearerParameters +
                ", quality=" + quality +
                ", pdcpThroughput=" + pdcpThroughput +
                ", pdcpPackDelay=" + pdcpPackDelay +
                ", resourceUsage=" + resourceUsage +
                ", type=" + type +
                ", timer=" + timer +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RnibLink link = (RnibLink) o;

        return linkId.equals(link.linkId);
    }

    @Override
    public int hashCode() {
        return linkId.hashCode();
    }

    /**
     * Enum of Link-Type.
     */
    public enum Type {
        SERVING_PRIMARY("serving/primary") {
            @Override
            public String toString() {
                return "serving/primary";
            }
        },
        SERVING_SECONDARY_CA("serving/secondary/ca") {
            @Override
            public String toString() {
                return "serving/secondary/ca";
            }
        },
        SERVING_SECONDARY_DC("serving/secondary/dc") {
            @Override
            public String toString() {
                return "serving/secondary/dc";
            }
        },
        NON_SERVING("non-serving") {
            @Override
            public String toString() {
                return "non-serving";
            }
        };

        private String name;

        Type(String name) {
            this.name = name;
        }

        /**
         * Get enum value of link-type.
         * @param name String representation of Enum Type
         * @return Type
         */
        public static Type getEnum(String name) {
            Optional<Type> any = Arrays.stream(Type.values()).filter(typeStr -> typeStr.name.equals(name)).findAny();
            if (any.isPresent()) {
                return any.get();
            }
            throw new IllegalArgumentException("No enum defined for string: " + name);
        }
    }

    /**
     * Quality of Link.
     */
    @JsonPropertyOrder({
            "rx",
            "cqi",
            "mcs"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinkQuality {
        Rx rx = null;
        Cqi cqi = null;
        Mcs mcs = null;

        /**
         * Get rx.
         * @return rx
         */
        public Rx getRx() {
            return rx;
        }

        /**
         * Set rx.
         * @param rx rx
         */
        public void setRx(Rx rx) {
            this.rx = rx;
        }

        /**
         * Get cqi.
         * @return cqi
         */
        public Cqi getCqi() {
            return cqi;
        }

        /**
         * Set cqi.
         * @param cqi cqi
         */
        public void setCqi(Cqi cqi) {
            this.cqi = cqi;
        }

        /**
         * Get mcs.
         * @return mcs
         */
        public Mcs getMcs() {
            return mcs;
        }

        /**
         * Set mcs.
         * @param mcs mcs
         */
        public void setMcs(Mcs mcs) {
            this.mcs = mcs;
        }

        /**
         * Class to represent rx.
         */
        @JsonPropertyOrder({
                "rsrp",
                "rsrq",
                "timesincelastupdate"
        })
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Rx {
            double rsrp;
            double rsrq;
            WallClockTimestamp timesincelastupdate;

            @JsonCreator
            public Rx(@JsonProperty("rsrp") double rsrp, @JsonProperty("rsrq") double rsrq) {
                this.rsrp = rsrp;
                this.rsrq = rsrq;
                this.timesincelastupdate = new WallClockTimestamp();
            }

            /**
             * Get rsrp.
             * @return double rsrp
             */
            public double getRsrp() {
                return rsrp;
            }

            /**
             * Set rsrp.
             * @param rsrp rsrp
             */
            public void setRsrp(double rsrp) {
                this.rsrp = rsrp;
            }

            /**
             * Get rsrq.
             * @return double rsrq
             */
            public double getRsrq() {
                return rsrq;
            }

            /**
             * Set rsrq.
             * @param rsrq rsrq
             */
            public void setRsrq(double rsrq) {
                this.rsrq = rsrq;
            }

            /**
             * Get time since last update.
             *
             * @return long Time
             */
            public long getTimesincelastupdate() {
                return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
            }

            /**
             * Set time since last update.
             *
             * @param timesincelastupdate time since last update
             */
            public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
                this.timesincelastupdate = timesincelastupdate;
            }

            @Override
            public String toString() {
                return "rx{" +
                        "rsrp=" + rsrp +
                        ", rsrq=" + rsrq +
                        ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() -
                        timesincelastupdate.unixTimestamp()) +
                        '}';
            }
        }

        @JsonPropertyOrder({
                "hist",
                "mode",
                "mean",
                "timesincelastupdate"
        })
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Cqi {
            RadioRepPerServCell.CqiHist hist;
            double mode;
            double mean;
            WallClockTimestamp timesincelastupdate;

            @JsonCreator
            public Cqi(@JsonProperty("hist") RadioRepPerServCell.CqiHist hist, @JsonProperty("mode") double mode,
                       @JsonProperty("mean") double mean) {
                this.hist = hist;
                this.mode = mode;
                this.mean = mean;
                this.timesincelastupdate = new WallClockTimestamp();
            }


            /**
             * Get CQIHist.
             * @return CqiHist
             */
            public RadioRepPerServCell.CqiHist getHist() {
                return hist;
            }

            /**
             * Get CQIHist.
             * @param hist CqiHist
             */
            public void setHist(RadioRepPerServCell.CqiHist hist) {
                this.hist = hist;
            }

            /**
             * Get mode.
             * @return double mode
             */
            public double getMode() {
                return mode;
            }

            /**
             * Set mode.
             * @param mode mode
             */
            public void setMode(double mode) {
                this.mode = mode;
            }

            /**
             * Get mean.
             * @return double mean
             */
            public double getMean() {
                return mean;
            }

            /**
             * Set mean.
             * @param mean mean
             */
            public void setMean(double mean) {
                this.mean = mean;
            }

            /**
             * Get time since last update.
             *
             * @return long Time
             */
            public long getTimesincelastupdate() {
                return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
            }

            /**
             * Set time since last update.
             *
             * @param timesincelastupdate time since last update
             */
            public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
                this.timesincelastupdate = timesincelastupdate;
            }

            @Override
            public String toString() {
                return "cqi{" +
                        "hist=" + hist +
                        ", mode=" + mode +
                        ", mean=" + mean +
                        ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() -
                        timesincelastupdate.unixTimestamp()) +
                        '}';
            }
        }

        @JsonPropertyOrder({
                "dl",
                "ul",
                "timesincelastupdate"
        })
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Mcs {
            SchedMeasRepPerServCell.McsDl dl;
            SchedMeasRepPerServCell.McsUl ul;
            WallClockTimestamp timesincelastupdate;

            @JsonCreator
            public Mcs(@JsonProperty("dl") SchedMeasRepPerServCell.McsDl dl,
                       @JsonProperty("ul") SchedMeasRepPerServCell.McsUl ul) {
                this.dl = dl;
                this.ul = ul;
                this.timesincelastupdate = new WallClockTimestamp();
            }

            /**
             * Get DL.
             * @return Dl
             */
            public SchedMeasRepPerServCell.McsDl getDl() {
                return dl;
            }

            /**
             * Set DL.
             * @param dl DL
             */
            public void setDl(SchedMeasRepPerServCell.McsDl dl) {
                this.dl = dl;
            }

            /**
             * Get UL.
             * @return Ul
             */
            public SchedMeasRepPerServCell.McsUl getUl() {
                return ul;
            }

            /**
             * Set UL.
             * @param ul Ul
             */
            public void setUl(SchedMeasRepPerServCell.McsUl ul) {
                this.ul = ul;
            }

            /**
             * Get time since last update.
             *
             * @return long Time
             */
            public long getTimesincelastupdate() {
                return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
            }

            /**
             * Set time since last update.
             *
             * @param timesincelastupdate time since last update
             */
            public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
                this.timesincelastupdate = timesincelastupdate;
            }

            @Override
            public String toString() {
                return "mcs{" +
                        "dl=" + dl +
                        ", ul=" + ul +
                        ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() -
                        timesincelastupdate.unixTimestamp()) +
                        '}';
            }
        }

    }

    @JsonPropertyOrder({
            "dl",
            "ul"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PdcpThroughput {
        WallClockTimestamp timesincelastupdate;
        private PDCPMeasReportPerUe.ThroughputDl dl;
        private PDCPMeasReportPerUe.ThroughputUl ul;

        @JsonCreator
        public PdcpThroughput(@JsonProperty("dl") PDCPMeasReportPerUe.ThroughputDl dl,
                              @JsonProperty("ul") PDCPMeasReportPerUe.ThroughputUl ul) {
            this.dl = dl;
            this.ul = ul;
            this.timesincelastupdate = new WallClockTimestamp();
        }

        /**
         * Get DL.
         * @return Dl
         */
        public PDCPMeasReportPerUe.ThroughputDl getDl() {
            return dl;
        }

        /**
         * Set DL.
         * @param dl DL
         */
        public void setDl(PDCPMeasReportPerUe.ThroughputDl dl) {
            this.dl = dl;
        }

        /**
         * Get UL.
         * @return Ul
         */
        public PDCPMeasReportPerUe.ThroughputUl getUl() {
            return ul;
        }

        /**
         * Set UL.
         * @param ul Ul
         */
        public void setUl(PDCPMeasReportPerUe.ThroughputUl ul) {
            this.ul = ul;
        }

        /**
         * Get time since last update.
         *
         * @return long Time
         */
        public long getTimesincelastupdate() {
            return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
        }

        /**
         * Set time since last update.
         *
         * @param timesincelastupdate time since last update
         */
        public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
            this.timesincelastupdate = timesincelastupdate;
        }

        @Override
        public String
        toString() {
            return "PdcpThroughput{" +
                    "dl=" + dl +
                    ", ul=" + ul +
                    ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() -
                    timesincelastupdate.unixTimestamp()) +
                    '}';
        }
    }

    @JsonPropertyOrder({
            "dl",
            "ul"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PdcpPacketdelay {
        PDCPMeasReportPerUe.PktDelayDl dl;
        PDCPMeasReportPerUe.PktDelayUl ul;
        WallClockTimestamp timesincelastupdate;

        @JsonCreator
        public PdcpPacketdelay(@JsonProperty("dl") PDCPMeasReportPerUe.PktDelayDl dl,
                               @JsonProperty("ul") PDCPMeasReportPerUe.PktDelayUl ul) {
            this.dl = dl;
            this.ul = ul;
            this.timesincelastupdate = new WallClockTimestamp();
        }

        /**
         * Get DL.
         * @return Dl
         */
        public PDCPMeasReportPerUe.PktDelayDl getDl() {
            return dl;
        }

        /**
         * Set DL.
         * @param dl DL
         */
        public void setDl(PDCPMeasReportPerUe.PktDelayDl dl) {
            this.dl = dl;
        }

        /**
         * Get UL.
         * @return Ul
         */
        public PDCPMeasReportPerUe.PktDelayUl getUl() {
            return ul;
        }

        /**
         * Set UL.
         * @param ul Ul
         */
        public void setUl(PDCPMeasReportPerUe.PktDelayUl ul) {
            this.ul = ul;
        }

        /**
         * Get time since last update.
         *
         * @return long Time
         */
        public long getTimesincelastupdate() {
            return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
        }

        /**
         * Set time since last update.
         *
         * @param timesincelastupdate time since last update
         */
        public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
            this.timesincelastupdate = timesincelastupdate;
        }

        @Override
        public String toString() {
            return "PdcpPacketdelay{" +
                    "dl=" + dl +
                    ", ul=" + ul +
                    ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() -
                    timesincelastupdate.unixTimestamp()) +
                    '}';
        }
    }

    @JsonPropertyOrder({
            "dl",
            "ul"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResourceUsage {
        PRBUsage.PrbUsageDl dl;
        PRBUsage.PrbUsageUl ul;
        WallClockTimestamp timesincelastupdate;

        @JsonCreator
        public ResourceUsage(@JsonProperty("dl") PRBUsage.PrbUsageDl dl,
                             @JsonProperty("ul") PRBUsage.PrbUsageUl ul) {
            this.dl = dl;
            this.ul = ul;
            this.timesincelastupdate = new WallClockTimestamp();
        }

        /**
         * Get DL.
         * @return Dl
         */
        public PRBUsage.PrbUsageDl getDl() {
            return dl;
        }

        /**
         * Set DL.
         * @param dl DL
         */
        public void setDl(PRBUsage.PrbUsageDl dl) {
            this.dl = dl;
        }

        /**
         * Get UL.
         * @return Ul
         */
        public PRBUsage.PrbUsageUl getUl() {
            return ul;
        }

        /**
         * Set UL.
         * @param ul Ul
         */
        public void setUl(PRBUsage.PrbUsageUl ul) {
            this.ul = ul;
        }

        /**
         * Get time since last update.
         *
         * @return long Time
         */
        public long getTimesincelastupdate() {
            return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
        }

        /**
         * Set time since last update.
         *
         * @param timesincelastupdate time since last update
         */
        public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
            this.timesincelastupdate = timesincelastupdate;
        }

        @Override
        public String toString() {
            return "ResourceUsage{" +
                    "dl=" + dl +
                    ", ul=" + ul +
                    ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() -
                    timesincelastupdate.unixTimestamp()) +
                    '}';
        }
    }
}

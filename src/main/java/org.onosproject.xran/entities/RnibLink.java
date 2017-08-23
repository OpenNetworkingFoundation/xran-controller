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
import org.onosproject.store.service.WallClockTimestamp;
import org.onosproject.xran.codecs.api.*;
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
 * Created by dimitris on 7/22/17.
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
    private PDCPThroughput pdcpThroughput;
    @JsonProperty("PDCP-Packet-Delay")
    private PDCPPacketDelay pdcpPackDelay;
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

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer.cancel();
        this.timer.purge();
        this.timer = timer;
    }

    @JsonProperty("Link-ID")
    public LinkId getLinkId() {
        return linkId;
    }

    @JsonProperty("Link-ID")
    public void setLinkId(LinkId linkId) {
        this.linkId = linkId;
    }

    public void setLinkId(RnibCell cell, RnibUe ue) {
        this.linkId = LinkId.valueOf(cell, ue);
        trafficPercent.setEcgi(cell.getEcgi());
    }

    @JsonProperty("Type")
    public Type getType() {
        return type;
    }

    @JsonProperty("Type")
    public void setType(Type type) {
        this.type = type;
    }

    @JsonProperty("TrafficPercent")
    public TrafficSplitPercentage getTrafficPercent() {
        return trafficPercent;
    }

    @JsonProperty("TrafficPercent")
    public void setTrafficPercent(TrafficSplitPercentage trafficPercent) {
        this.trafficPercent = trafficPercent;
    }

    @JsonProperty("BearerParameters")
    public ERABParams getBearerParameters() {
        return bearerParameters;
    }

    @JsonProperty("BearerParameters")
    public void setBearerParameters(ERABParams bearerParameters) {
        this.bearerParameters = bearerParameters;
    }

    @JsonProperty("Quality")
    public LinkQuality getQuality() {
        return quality;
    }

    @JsonProperty("Quality")
    public void setQuality(LinkQuality quality) {
        this.quality = quality;
    }

    @JsonProperty("RRMConfiguration")
    public RRMConfig getRrmParameters() {
        return rrmParameters;
    }

    @JsonProperty("RRMConfiguration")
    public void setRrmParameters(RRMConfig rrmParameters) {
        this.rrmParameters = rrmParameters;
    }

    public void modifyRrmParameters(JsonNode rrmConfigNode) {
        {
            JsonNode p_a = rrmConfigNode.path("p_a");
            if (!p_a.isMissingNode()) {
                RRMConfig.Pa pa = new RRMConfig.Pa();

                List<XICICPA> collect = Lists.newArrayList();
                collect.add(new XICICPA(p_a.asInt()));
                pa.setXICICPA(collect);
                rrmParameters.setPa(pa);
            }
        }

        {
            JsonNode start_prb_dl = rrmConfigNode.path("start_prb_dl");
            if (!start_prb_dl.isMissingNode()) {
                RRMConfig.StartPrbDl startPrbDl = new RRMConfig.StartPrbDl();

                List<BerInteger> collect = Lists.newArrayList();
                collect.add(new BerInteger(start_prb_dl.asInt()));
                startPrbDl.setSeqOf(collect);

                rrmParameters.setStartPrbDl(startPrbDl);
            }
        }

        {
            JsonNode end_prb_dl = rrmConfigNode.path("end_prb_dl");
            if (!end_prb_dl.isMissingNode()) {
                RRMConfig.EndPrbDl endPrbDl = new RRMConfig.EndPrbDl();

                List<BerInteger> collect = Lists.newArrayList();
                collect.add(new BerInteger(end_prb_dl.asInt()));
                endPrbDl.setSeqOf(collect);

                rrmParameters.setEndPrbDl(endPrbDl);
            }
        }

        {
            JsonNode sub_frame_bitmask_dl = rrmConfigNode.path("sub_frame_bitmask_dl");
            if (!sub_frame_bitmask_dl.isMissingNode()) {
                RRMConfig.SubframeBitmaskDl subframeBitmaskDl = new RRMConfig.SubframeBitmaskDl();
                List<BerBitString> collect = Lists.newArrayList();
                
                byte[] hexString = DatatypeConverter.parseHexBinary(sub_frame_bitmask_dl.asText());
                collect.add(new BerBitString(hexString, 10));
                subframeBitmaskDl.setSeqOf(collect);
                rrmParameters.setSubframeBitmaskDl(subframeBitmaskDl);
            }
        }

        {
            JsonNode start_prb_ul = rrmConfigNode.path("start_prb_ul");
            if (!start_prb_ul.isMissingNode()) {
                RRMConfig.StartPrbUl startPrbUl = new RRMConfig.StartPrbUl();

                List<BerInteger> collect = Lists.newArrayList();
                collect.add(new BerInteger(start_prb_ul.asInt()));
                startPrbUl.setSeqOf(collect);

                rrmParameters.setStartPrbUl(startPrbUl);
            }
        }

        {
            JsonNode end_prb_ul = rrmConfigNode.path("end_prb_ul");
            if (!end_prb_ul.isMissingNode()) {
                RRMConfig.EndPrbUl endPrbUl = new RRMConfig.EndPrbUl();

                List<BerInteger> collect = Lists.newArrayList();
                collect.add(new BerInteger(end_prb_ul.asInt()));
                endPrbUl.setSeqOf(collect);

                rrmParameters.setEndPrbUl(endPrbUl);
            }
        }

        {
            JsonNode p0_ue_pusch = rrmConfigNode.path("p0_ue_pusch");
            if (!p0_ue_pusch.isMissingNode()) {
                RRMConfig.P0UePusch p0UePusch = new RRMConfig.P0UePusch();

                List<BerInteger> collect = Lists.newArrayList();
                collect.add(new BerInteger(p0_ue_pusch.asInt()));
                p0UePusch.setSeqOf(collect);

                rrmParameters.setP0UePusch(p0UePusch);
            }
        }

        {
            JsonNode sub_frame_bitmask_ul = rrmConfigNode.path("sub_frame_bitmask_ul");
            if (!sub_frame_bitmask_ul.isMissingNode()) {
                RRMConfig.SubframeBitmaskUl subframeBitmaskUl = new RRMConfig.SubframeBitmaskUl();
                List<BerBitString> collect = Lists.newArrayList();

                byte[] hexString = DatatypeConverter.parseHexBinary(sub_frame_bitmask_ul.asText());
                collect.add(new BerBitString(hexString, 10));
                subframeBitmaskUl.setSeqOf(collect);
                rrmParameters.setSubframeBitmaskUl(subframeBitmaskUl);
            }
        }
    }

    @JsonProperty("PDCP-Throughput")
    public PDCPThroughput getPdcpThroughput() {
        return pdcpThroughput;
    }

    @JsonProperty("PDCP-Throughput")
    public void setPdcpThroughput(PDCPThroughput pdcpThroughput) {
        this.pdcpThroughput = pdcpThroughput;
    }

    @JsonProperty("PDCP-Packet-Delay")
    public PDCPPacketDelay getPdcpPackDelay() {
        return pdcpPackDelay;
    }

    @JsonProperty("PDCP-Packet-Delay")
    public void setPdcpPackDelay(PDCPPacketDelay pdcpPackDelay) {
        this.pdcpPackDelay = pdcpPackDelay;
    }

    @JsonProperty("Resource-Usage")
    public ResourceUsage getResourceUsage() {
        return resourceUsage;
    }

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RnibLink link = (RnibLink) o;

        return linkId.equals(link.linkId);
    }

    @Override
    public int hashCode() {
        return linkId.hashCode();
    }

    public enum Type {
        SERVING_PRIMARY("serving/primary") {
            @Override
            public String toString() {
                return "\"serving/primary\"";
            }
        },
        // TODO: Add CA/DC
        SERVING_SECONDARY_CA("serving/secondary/ca") {
            @Override
            public String toString() {
                return "\"serving/secondary/ca\"";
            }
        },
        SERVING_SECONDARY_DC("serving/secondary/dc") {
            @Override
            public String toString() {
                return "\"serving/secondary/dc\"";
            }
        },
        NON_SERVING("non-serving") {
            @Override
            public String toString() {
                return "\"non-serving\"";
            }
        };

        private String name;

        Type(String name) {
            this.name = name;
        }

        public static Type getEnum(String name) {
            Optional<Type> any = Arrays.stream(Type.values()).filter(typeStr -> typeStr.name.equals(name)).findAny();
            if (any.isPresent()) {
                return any.get();
            }
            throw new IllegalArgumentException("No enum defined for string: " + name);
        }
    }

    @JsonPropertyOrder({
            "RX",
            "CQI",
            "MCS"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinkQuality {
        RX RX = null;
        CQI CQI = null;
        MCS MCS = null;

        public LinkQuality.RX getRX() {
            return RX;
        }

        public void setRX(LinkQuality.RX RX) {
            this.RX = RX;
        }

        public LinkQuality.CQI getCQI() {
            return CQI;
        }

        public void setCQI(LinkQuality.CQI CQI) {
            this.CQI = CQI;
        }

        public LinkQuality.MCS getMCS() {
            return MCS;
        }

        public void setMCS(LinkQuality.MCS MCS) {
            this.MCS = MCS;
        }

        @JsonPropertyOrder({
                "RSRP",
                "RSRQ",
                "timesincelastupdate"
        })
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RX {
            double RSRP;
            double RSRQ;
            WallClockTimestamp timesincelastupdate;

            @JsonCreator
            public RX(@JsonProperty("RSRP") double RSRP, @JsonProperty("RSRQ") double RSRQ) {
                this.RSRP = RSRP;
                this.RSRQ = RSRQ;
                this.timesincelastupdate = new WallClockTimestamp();
            }

            public double getRSRP() {
                return RSRP;
            }

            public void setRSRP(double RSRP) {
                this.RSRP = RSRP;
            }

            public double getRSRQ() {
                return RSRQ;
            }

            public void setRSRQ(double RSRQ) {
                this.RSRQ = RSRQ;
            }

            public long getTimesincelastupdate() {
                return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
            }

            public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
                this.timesincelastupdate = timesincelastupdate;
            }

            @Override
            public String toString() {
                return "RX{" +
                        "RSRP=" + RSRP +
                        ", RSRQ=" + RSRQ +
                        ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp()) +
                        '}';
            }
        }

        @JsonPropertyOrder({
                "Hist",
                "Mode",
                "Mean",
                "timesincelastupdate"
        })
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class CQI {
            RadioRepPerServCell.CqiHist Hist;
            double Mode;
            double Mean;
            WallClockTimestamp timesincelastupdate;

            @JsonCreator
            public CQI(@JsonProperty("Hist") RadioRepPerServCell.CqiHist hist, @JsonProperty("Mode") double mode, @JsonProperty("Mean") double mean) {
                Hist = hist;
                Mode = mode;
                Mean = mean;
                this.timesincelastupdate = new WallClockTimestamp();
            }

            public RadioRepPerServCell.CqiHist getHist() {
                return Hist;
            }

            public void setHist(RadioRepPerServCell.CqiHist hist) {
                Hist = hist;
            }

            public double getMode() {
                return Mode;
            }

            public void setMode(double mode) {
                Mode = mode;
            }

            public double getMean() {
                return Mean;
            }

            public void setMean(double mean) {
                Mean = mean;
            }

            public long getTimesincelastupdate() {
                return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
            }

            public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
                this.timesincelastupdate = timesincelastupdate;
            }

            @Override
            public String toString() {
                return "CQI{" +
                        "Hist=" + Hist +
                        ", Mode=" + Mode +
                        ", Mean=" + Mean +
                        ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp()) +
                        '}';
            }
        }

        @JsonPropertyOrder({
                "dl",
                "ul",
                "timesincelastupdate"
        })
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MCS {
            SchedMeasRepPerServCell.McsDl dl;
            SchedMeasRepPerServCell.McsUl ul;
            WallClockTimestamp timesincelastupdate;

            @JsonCreator
            public MCS(@JsonProperty("dl") SchedMeasRepPerServCell.McsDl dl, @JsonProperty("ul") SchedMeasRepPerServCell.McsUl ul) {
                this.dl = dl;
                this.ul = ul;
                this.timesincelastupdate = new WallClockTimestamp();
            }

            public SchedMeasRepPerServCell.McsDl getDl() {
                return dl;
            }

            public void setDl(SchedMeasRepPerServCell.McsDl dl) {
                this.dl = dl;
            }

            public SchedMeasRepPerServCell.McsUl getUl() {
                return ul;
            }

            public void setUl(SchedMeasRepPerServCell.McsUl ul) {
                this.ul = ul;
            }

            public long getTimesincelastupdate() {
                return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
            }

            public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
                this.timesincelastupdate = timesincelastupdate;
            }

            @Override
            public String toString() {
                return "MCS{" +
                        "dl=" + dl +
                        ", ul=" + ul +
                        ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp()) +
                        '}';
            }
        }

    }

    @JsonPropertyOrder({
            "dl",
            "ul"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PDCPThroughput {
        WallClockTimestamp timesincelastupdate;
        private PDCPMeasReportPerUe.ThroughputDl dl;
        private PDCPMeasReportPerUe.ThroughputUl ul;

        @JsonCreator
        public PDCPThroughput(@JsonProperty("dl") PDCPMeasReportPerUe.ThroughputDl dl, @JsonProperty("ul") PDCPMeasReportPerUe.ThroughputUl ul) {
            this.dl = dl;
            this.ul = ul;
            this.timesincelastupdate = new WallClockTimestamp();
        }

        public PDCPMeasReportPerUe.ThroughputDl getDl() {
            return dl;
        }

        public void setDl(PDCPMeasReportPerUe.ThroughputDl dl) {
            this.dl = dl;
        }

        public PDCPMeasReportPerUe.ThroughputUl getUl() {
            return ul;
        }

        public void setUl(PDCPMeasReportPerUe.ThroughputUl ul) {
            this.ul = ul;
        }

        public long getTimesincelastupdate() {
            return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
        }

        public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
            this.timesincelastupdate = timesincelastupdate;
        }

        @Override
        public String
        toString() {
            return "PDCPThroughput{" +
                    "dl=" + dl +
                    ", ul=" + ul +
                    ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp()) +
                    '}';
        }
    }

    @JsonPropertyOrder({
            "dl",
            "ul"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PDCPPacketDelay {
        PDCPMeasReportPerUe.PktDelayDl dl;
        PDCPMeasReportPerUe.PktDelayUl ul;
        WallClockTimestamp timesincelastupdate;

        @JsonCreator
        public PDCPPacketDelay(@JsonProperty("dl") PDCPMeasReportPerUe.PktDelayDl dl, @JsonProperty("ul") PDCPMeasReportPerUe.PktDelayUl ul) {
            this.dl = dl;
            this.ul = ul;
            this.timesincelastupdate = new WallClockTimestamp();
        }

        public PDCPMeasReportPerUe.PktDelayDl getDl() {
            return dl;
        }

        public void setDl(PDCPMeasReportPerUe.PktDelayDl dl) {
            this.dl = dl;
        }

        public PDCPMeasReportPerUe.PktDelayUl getUl() {
            return ul;
        }

        public void setUl(PDCPMeasReportPerUe.PktDelayUl ul) {
            this.ul = ul;
        }

        public long getTimesincelastupdate() {
            return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
        }

        public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
            this.timesincelastupdate = timesincelastupdate;
        }

        @Override
        public String toString() {
            return "PDCPPacketDelay{" +
                    "dl=" + dl +
                    ", ul=" + ul +
                    ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp()) +
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
        public ResourceUsage(@JsonProperty("dl") PRBUsage.PrbUsageDl dl, @JsonProperty("ul") PRBUsage.PrbUsageUl ul) {
            this.dl = dl;
            this.ul = ul;
            this.timesincelastupdate = new WallClockTimestamp();
        }

        public PRBUsage.PrbUsageDl getDl() {
            return dl;
        }

        public void setDl(PRBUsage.PrbUsageDl dl) {
            this.dl = dl;
        }

        public PRBUsage.PrbUsageUl getUl() {
            return ul;
        }

        public void setUl(PRBUsage.PrbUsageUl ul) {
            this.ul = ul;
        }

        public long getTimesincelastupdate() {
            return new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp();
        }

        public void setTimesincelastupdate(WallClockTimestamp timesincelastupdate) {
            this.timesincelastupdate = timesincelastupdate;
        }

        @Override
        public String toString() {
            return "ResourceUsage{" +
                    "dl=" + dl +
                    ", ul=" + ul +
                    ", timesincelastupdate=" + (new WallClockTimestamp().unixTimestamp() - timesincelastupdate.unixTimestamp()) +
                    '}';
        }
    }
}

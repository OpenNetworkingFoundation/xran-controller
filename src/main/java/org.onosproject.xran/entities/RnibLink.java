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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.onosproject.xran.codecs.api.*;
import org.onosproject.xran.codecs.pdu.PDCPMeasReportPerUe;
import org.onosproject.xran.codecs.pdu.RRMConfig;
import org.onosproject.xran.identifiers.LinkId;
import org.onosproject.xran.codecs.ber.types.BerInteger;

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

        pdcpThroughput = new PDCPThroughput();
        quality = new LinkQuality();
        pdcpPackDelay = new PDCPPacketDelay();
        resourceUsage = new ResourceUsage();
        timer = new Timer();

        type = Type.NON_SERVING;

        linkId = LinkId.valueOf(cell, ue);

        rrmParameters = new RRMConfig();
        RRMConfig.Crnti crnti = new RRMConfig.Crnti();
        crnti.addCRNTI(linkId.getUe().getRanId());
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
            "dl",
            "ul"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class PDCPThroughput {
        private PDCPMeasReportPerUe.ThroughputDl dl;
        private PDCPMeasReportPerUe.ThroughputUl ul;

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

        @Override
        public String toString() {
            return "PDCPThroughput{" +
                    "dl=" + dl +
                    ", ul=" + ul +
                    '}';
        }
    }

    @JsonPropertyOrder({
            "dl",
            "ul"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class PDCPPacketDelay {
        PDCPMeasReportPerUe.PktDelayDl dl;
        PDCPMeasReportPerUe.PktDelayUl ul;

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

        @Override
        public String toString() {
            return "PDCPPacketDelay{" +
                    "dl=" + dl +
                    ", ul=" + ul +
                    '}';
        }
    }

    @JsonPropertyOrder({
            "dl",
            "ul"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class ResourceUsage {
        PRBUsage.PrbUsageDl dl;
        PRBUsage.PrbUsageUl ul;

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

        @Override
        public String toString() {
            return "ResourceUsage{" +
                    "dl=" + dl +
                    ", ul=" + ul +
                    '}';
        }
    }

    @JsonPropertyOrder({
            "rsrp",
            "rsrq",
            "cqiHist",
            "cqiMode",
            "cqiMean",
            "mcsDl",
            "mcsUl"
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class LinkQuality {
        double rsrp;
        double rsrq;
        RadioRepPerServCell.CqiHist cqiHist;
        double cqiMode;
        double cqiMean;
        SchedMeasRepPerServCell.McsDl mcsDl;
        SchedMeasRepPerServCell.McsUl mcsUl;

        public double getRsrp() {
            return rsrp;
        }

        public void setRsrp(double rsrp) {
            this.rsrp = rsrp;
        }

        public double getRsrq() {
            return rsrq;
        }

        public void setRsrq(double rsrq) {
            this.rsrq = rsrq;
        }

        public RadioRepPerServCell.CqiHist getCqiHist() {
            return cqiHist;
        }

        public void setCqiHist(RadioRepPerServCell.CqiHist cqiHist) {
            this.cqiHist = cqiHist;
        }

        public double getCqiMode() {
            return cqiMode;
        }

        public void setCqiMode(double cqiMode) {
            this.cqiMode = cqiMode;
        }

        public double getCqiMean() {
            return cqiMean;
        }

        public void setCqiMean(double cqiMean) {
            this.cqiMean = cqiMean;
        }

        public SchedMeasRepPerServCell.McsDl getMcsDl() {
            return mcsDl;
        }

        public void setMcsDl(SchedMeasRepPerServCell.McsDl mcsDl) {
            this.mcsDl = mcsDl;
        }

        public SchedMeasRepPerServCell.McsUl getMcsUl() {
            return mcsUl;
        }

        public void setMcsUl(SchedMeasRepPerServCell.McsUl mcsUl) {
            this.mcsUl = mcsUl;
        }

        @Override
        public String toString() {
            return "LinkQuality{" +
                    "rsrp=" + rsrp +
                    ", rsrq=" + rsrq +
                    ", cqiHist=" + cqiHist +
                    ", cqiMode=" + cqiMode +
                    ", cqiMean=" + cqiMean +
                    ", mcsDl=" + mcsDl +
                    ", mcsUl=" + mcsUl +
                    '}';
        }
    }
}

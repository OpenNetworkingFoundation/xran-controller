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

import org.onosproject.xran.codecs.api.*;
import org.onosproject.xran.codecs.pdu.PDCPMeasReportPerUe;
import org.onosproject.xran.codecs.pdu.RRMConfig;
import org.onosproject.xran.identifiers.LinkId;
import org.openmuc.jasn1.ber.types.BerInteger;

import java.util.Arrays;
import java.util.Optional;
import java.util.Timer;

/**
 * Created by dimitris on 7/22/17.
 */
public class RnibLink {
    private LinkId linkId;
    //    private String type;
    private RRMConfig rrmParameters;

    private TrafficSplitPercentage trafficPercent;
    private ERABParams bearerParameters;

    private LinkQuality quality;
    private PDCPThroughput pdcpThroughput;
    private PDCPPacketDelay pdcpPackDelay;
    private ResourceUsage resourceUsage;
    private Type type;
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
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer.cancel();
        this.timer.purge();
        this.timer = timer;
    }

    public LinkId getLinkId() {
        return linkId;
    }

    public void setLinkId(LinkId linkId) {
        this.linkId = linkId;
    }

    public void setLinkId(RnibCell cell, RnibUe ue) {
        this.linkId = LinkId.valueOf(cell, ue);
        trafficPercent.setEcgi(cell.getEcgi());
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public TrafficSplitPercentage getTrafficPercent() {
        return trafficPercent;
    }

    public void setTrafficPercent(TrafficSplitPercentage trafficPercent) {
        this.trafficPercent = trafficPercent;
    }

    public ERABParams getBearerParameters() {
        return bearerParameters;
    }

    public void setBearerParameters(ERABParams bearerParameters) {
        this.bearerParameters = bearerParameters;
    }

    public LinkQuality getQuality() {
        return quality;
    }

    public void setQuality(LinkQuality quality) {
        this.quality = quality;
    }

    public RRMConfig getRrmParameters() {
        return rrmParameters;
    }

    public void setRrmParameters(RRMConfig rrmParameters) {
        this.rrmParameters = rrmParameters;
    }

    public PDCPThroughput getPdcpThroughput() {
        return pdcpThroughput;
    }

    public void setPdcpThroughput(PDCPThroughput pdcpThroughput) {
        this.pdcpThroughput = pdcpThroughput;
    }

    public PDCPPacketDelay getPdcpPackDelay() {
        return pdcpPackDelay;
    }

    public void setPdcpPackDelay(PDCPPacketDelay pdcpPackDelay) {
        this.pdcpPackDelay = pdcpPackDelay;
    }

    public ResourceUsage getResourceUsage() {
        return resourceUsage;
    }

    public void setResourceUsage(ResourceUsage resourceUsage) {
        this.resourceUsage = resourceUsage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append(linkId != null ? "\"link-id\":" + linkId : "")
                .append(type != null ? ",\n\"type\":" + type : "")
                .append(rrmParameters != null ? ",\n\"rrm-params\":" + rrmParameters : "")
                .append(trafficPercent != null ? ",\n\"traffic-percent\":" + trafficPercent : "")
                .append(bearerParameters != null ? ",\n\"bearer-params\":" + bearerParameters : "")
                .append(quality != null ? ",\n\"quality\":" + quality : "")
                .append(pdcpThroughput != null ? ",\n\"pdcp-throughput\":" + pdcpThroughput : "")
                .append(pdcpPackDelay != null ? ",\n\"pdcp-packet-delay\":" + pdcpPackDelay : "")
                .append(resourceUsage != null ? ",\n\"resource-usage\":" + resourceUsage : "")
                .append("\n}\n");
        return sb.toString();
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
        SERVING_SECONDARY("serving/secondary") {
            @Override
            public String toString() {
                return "\"serving/secondary\"";
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
            StringBuilder sb = new StringBuilder();
            sb.append("{\n")
                    .append(dl != null ? "\"dl\":" + dl : "")
                    .append(ul != null ? ",\n\"ul\":" + ul : "")
                    .append("\n}\n");
            return sb.toString();
        }
    }

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
            StringBuilder sb = new StringBuilder();
            sb.append("{\n")
                    .append(dl != null ? "\"dl\":" + dl : "")
                    .append(ul != null ? ",\n\"ul\":" + ul : "")
                    .append("\n}\n");
            return sb.toString();
        }
    }

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
            StringBuilder sb = new StringBuilder();
            sb.append("{\n")
                    .append(dl != null ? "\"dl\":" + dl : "")
                    .append(ul != null ? ",\n\"ul\":" + ul : "")
                    .append("\n}\n");
            return sb.toString();
        }
    }

    public class LinkQuality {
        double rsrp;
        double rsrq;
        RadioRepPerServCell.CqiHist cqiHist;
        double cqiMode;
        double cqiMean;
        SchedMeasRepPerServCell.McsDl mcs_dl;
        SchedMeasRepPerServCell.McsUl mcs_ul;

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

        public SchedMeasRepPerServCell.McsDl getMcs_dl() {
            return mcs_dl;
        }

        public void setMcs_dl(SchedMeasRepPerServCell.McsDl mcs_dl) {
            this.mcs_dl = mcs_dl;
        }

        public SchedMeasRepPerServCell.McsUl getMcs_ul() {
            return mcs_ul;
        }

        public void setMcs_ul(SchedMeasRepPerServCell.McsUl mcs_ul) {
            this.mcs_ul = mcs_ul;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n")
                    .append("\"rsrp\":" + rsrp)
                    .append(",\n\"rsrq\":" + rsrq)
                    .append(",\n\"cqiMode\":" + cqiMode)
                    .append(",\n\"cqiMean\":" + cqiMean)
                    .append(mcs_dl != null ? ",\n\"mcs-dl\":" + mcs_dl : "")
                    .append(mcs_ul != null ? ",\n\"mcs-ul\":" + mcs_ul : "")
                    .append("\n}\n");
            return sb.toString();
        }
    }
}

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
import org.onosproject.xran.codecs.api.*;
import org.onosproject.xran.codecs.pdu.PDCPMeasReportPerUe;
import org.onosproject.xran.codecs.pdu.RRMConfig;
import org.onosproject.xran.identifiers.LinkId;
import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerInteger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        setDefaultRRMConf();
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

    public void modifyRrmParameters(JsonNode rrmConfigNode) {
        {
            JsonNode start_prb_dl = rrmConfigNode.get("start_prb_dl");
            if (start_prb_dl != null) {
                RRMConfig.StartPrbDl startPrbDl = new RRMConfig.StartPrbDl();
                if (start_prb_dl.isArray()) {
                    if (rrmParameters.getStartPrbDl().getSeqOf().size() == start_prb_dl.size()) {
                        List<BerInteger> collect = Stream.of(start_prb_dl)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        startPrbDl.setSeqOf(collect);
                    }
                }
                rrmParameters.setStartPrbDl(startPrbDl);
            }
        }

        {
            JsonNode end_prb_dl = rrmConfigNode.get("end_prb_dl");
            if (end_prb_dl != null) {
                RRMConfig.EndPrbDl endPrbDl = new RRMConfig.EndPrbDl();
                if (end_prb_dl.isArray()) {
                    if (rrmParameters.getEndPrbDl().getSeqOf().size() == end_prb_dl.size()) {
                        List<BerInteger> collect = Stream.of(end_prb_dl)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        endPrbDl.setSeqOf(collect);
                    }
                }
                rrmParameters.setEndPrbDl(endPrbDl);
            }
        }

        {
            JsonNode start_prb_ul = rrmConfigNode.get("start_prb_ul");
            if (start_prb_ul != null) {
                RRMConfig.StartPrbUl startPrbUl = new RRMConfig.StartPrbUl();
                if (start_prb_ul.isArray()) {
                    if (rrmParameters.getStartPrbUl().getSeqOf().size() == start_prb_ul.size()) {
                        List<BerInteger> collect = Stream.of(start_prb_ul)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        startPrbUl.setSeqOf(collect);
                    }
                }
                rrmParameters.setStartPrbUl(startPrbUl);
            }
        }

        {
            JsonNode end_prb_ul = rrmConfigNode.get("end_prb_ul");
            if (end_prb_ul != null) {
                RRMConfig.EndPrbUl endPrbUl = new RRMConfig.EndPrbUl();
                if (end_prb_ul.isArray()) {
                    if (rrmParameters.getEndPrbUl().getSeqOf().size() == end_prb_ul.size()) {
                        List<BerInteger> collect = Stream.of(end_prb_ul)
                                .map(val -> new BerInteger(val.asInt()))
                                .collect(Collectors.toList());
                        endPrbUl.setSeqOf(collect);
                    }
                }
                rrmParameters.setEndPrbUl(endPrbUl);
            }
        }

        // TODO
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

    private void setDefaultRRMConf() {
        rrmParameters = new RRMConfig();

        RRMConfig.Crnti crnti = new RRMConfig.Crnti();
        crnti.addCRNTI(linkId.getUe().getRanId());

        rrmParameters.setCrnti(crnti);

        rrmParameters.setEcgi(linkId.getEcgi());

        RRMConfig.StartPrbDl startPrbDl = new RRMConfig.StartPrbDl();
        startPrbDl.addBerInteger(new BerInteger(0));
        startPrbDl.addBerInteger(new BerInteger(50));

        rrmParameters.setStartPrbDl(startPrbDl);

        RRMConfig.StartPrbUl startPrbUl = new RRMConfig.StartPrbUl();
        startPrbUl.addBerInteger(new BerInteger(50));
        startPrbUl.addBerInteger(new BerInteger(100));

        rrmParameters.setStartPrbUl(startPrbUl);

        RRMConfig.EndPrbDl endPrbDl = new RRMConfig.EndPrbDl();
        endPrbDl.addBerInteger(new BerInteger(50));
        endPrbDl.addBerInteger(new BerInteger(100));

        rrmParameters.setEndPrbDl(endPrbDl);

        RRMConfig.EndPrbUl endPrbUl = new RRMConfig.EndPrbUl();
        endPrbUl.addBerInteger(new BerInteger(50));
        endPrbUl.addBerInteger(new BerInteger(100));

        rrmParameters.setEndPrbUl(endPrbUl);

        RRMConfig.SubframeBitmaskDl subframeBitmaskDl = new RRMConfig.SubframeBitmaskDl();
        BerBitString berBitString = new BerBitString(new byte[]{(byte) 0xAA, (byte) 0x80}, 10);
        BerBitString berBitString1 = new BerBitString(new byte[]{(byte) 0x55, (byte) 0x40}, 10);

        subframeBitmaskDl.addBerBitString(berBitString);
        subframeBitmaskDl.addBerBitString(berBitString1);

        rrmParameters.setSubframeBitmaskDl(subframeBitmaskDl);
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

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.onlab.packet.MacAddress;
import org.onosproject.net.HostId;
import org.onosproject.xran.codecs.api.CRNTI;
import org.onosproject.xran.codecs.api.ENBUES1APID;
import org.onosproject.xran.codecs.api.MMEUES1APID;
import org.onosproject.xran.codecs.pdu.RXSigMeasConfig;
import org.onosproject.xran.codecs.pdu.UECapabilityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Timer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.onosproject.net.HostId.hostId;

/**
 * R-NIB UE and its properties.
 */
@JsonPropertyOrder({
        "ID",
        "IMSI",
        "ENBUES1APID",
        "MMEUES1APID",
        "CRNTI",
        "State",
        "Capability",
        "MeasurementConfiguration"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class RnibUe {
    @JsonIgnore
    private static final String SCHEME = "xran";
    @JsonIgnore
    private static final Logger log =
            LoggerFactory.getLogger(RnibUe.class);

    @JsonProperty("ID")
    private Long id;
    @JsonProperty("IMSI")
    private String imsi;
    @JsonProperty("ENBUES1APID")
    private ENBUES1APID enbS1apId;
    @JsonProperty("MMEUES1APID")
    private MMEUES1APID mmeS1apId;
    @JsonProperty("CRNTI")
    private CRNTI crnti;
    @JsonProperty("State")
    private State state;
    @JsonProperty("Capability")
    private UECapabilityInfo capability;
    @JsonProperty("MeasurementConfiguration")
    private RXSigMeasConfig measConfig;
    @JsonIgnore
    private Timer timer;

    public RnibUe() {
        state = State.ACTIVE;
        timer = new Timer();
    }

    /**
     * Convert Host ID to UE ID.
     * @param hostId hostID
     * @return Long UE ID
     */
    public static Long hostIdtoUEId(HostId hostId) {
        String mac = hostId.mac().toString();
        mac = mac.replace(":", "");
        long l = Long.parseLong(mac, 16);
        return l;
    }

    /**
     * Get timer.
     * @return Timer
     */
    @JsonIgnore
    public Timer getTimer() {
        return timer;
    }

    /**
     * Set timer.
     * @param timer Timer
     */
    @JsonIgnore
    public void setTimer(Timer timer) {
        this.timer.cancel();
        this.timer.purge();
        this.timer = timer;
    }

    /**
     * Get MMEUES1APID.
     * @return MMEUES1APID
     */
    @JsonProperty("MMEUES1APID")
    public MMEUES1APID getMmeS1apId() {
        return mmeS1apId;
    }

    /**
     * Set MMEUES1APID.
     * @param mmeS1apId MMEUES1APID
     */
    @JsonProperty("MMEUES1APID")
    public void setMmeS1apId(MMEUES1APID mmeS1apId) {
        this.mmeS1apId = mmeS1apId;
    }

    /**
     * Get ENBUES1APID.
     * @return ENBUES1APID
     */
    @JsonProperty("ENBUES1APID")
    public ENBUES1APID getEnbS1apId() {
        return enbS1apId;
    }

    /**
     * Set ENBUES1APID.
     * @param enbS1apId ENBUES1APID
     */
    @JsonProperty("ENBUES1APID")
    public void setEnbS1apId(ENBUES1APID enbS1apId) {
        this.enbS1apId = enbS1apId;
    }

    /**
     * Get CRNTI.
     * @return CRNTI
     */
    @JsonProperty("CRNTI")
    public CRNTI getCrnti() {
        return crnti;
    }

    /**
     * Set CRNTI.
     * @param crnti CRNTI
     */
    @JsonProperty("CRNTI")
    public void setCrnti(CRNTI crnti) {
        this.crnti = crnti;
    }

    /**
     * Get IMSI.
     * @return IMSI
     */
    @JsonProperty("IMSI")
    public String getImsi() {
        return imsi;
    }

    /**
     * Set IMSI.
     * @param imsi IMSI
     */
    @JsonProperty("IMSI")
    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    /**
     * Get Host ID.
     * @return HostId
     */
    @JsonIgnore
    public HostId getHostId() {
        try {
            String text = Long.toHexString(this.id),
                    res = "";
            int charsLeft = 12 - text.length();
            if (charsLeft > 0) {
                res += Stream.generate(() -> "0").limit(charsLeft).collect(Collectors.joining(""));
            } else if (charsLeft < 0) {
                return null;
            }
            res += text;

            String insert = ":";
            int period = 2;

            StringBuilder builder = new StringBuilder(
                    res.length() + insert.length() * (res.length() / period) + 1);

            int index = 0;
            String prefix = "";
            while (index < res.length()) {
                // Don't putPrimaryLink the insert in the very first iteration.
                // This is easier than appending it *after* each substring
                builder.append(prefix);
                prefix = insert;
                builder.append(res.substring(index,
                        Math.min(index + period, res.length())));
                index += period;
            }

            return hostId(MacAddress.valueOf(builder.toString()));
        } catch (Exception e) {
            log.warn(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get RXMeasConfig Report.
     * @return RXSigMeasConfig
     */
    @JsonProperty("MeasurementConfiguration")
    public RXSigMeasConfig getMeasConfig() {
        return measConfig;
    }

    /**
     * Set RXMeasConfig Report.
     * @param measConfig RXSigMeasConfig
     */
    @JsonProperty("MeasurementConfiguration")
    public void setMeasConfig(RXSigMeasConfig measConfig) {
        this.measConfig = measConfig;
    }

    /**
     * Get UE Capability Info.
     * @return UECapabilityInfo
     */
    @JsonProperty("Capability")
    public UECapabilityInfo getCapability() {
        return capability;
    }

    /**
     * Set UE Capability Info.
     * @param capability UECapabilityInfo
     */
    @JsonProperty("Capability")
    public void setCapability(UECapabilityInfo capability) {
        this.capability = capability;
    }

    /**
     * Get State.
     * @return State
     */
    @JsonProperty("State")
    public State getState() {
        return state;
    }

    /**
     * Set State.
     * @param state State
     */
    @JsonProperty("State")
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Get UE ID.
     * @return Long UE ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set UE ID.
     * @param id Long UE ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "RnibUe{" +
                "id=" + id +
                ", imsi='" + imsi + '\'' +
                ", enbS1apId=" + enbS1apId +
                ", mmeS1apId=" + mmeS1apId +
                ", crnti=" + crnti +
                ", state=" + state +
                ", capability=" + capability +
                ", measConfig=" + measConfig +
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
        RnibUe rnibUe = (RnibUe) o;
        return Objects.equals(id, rnibUe.id);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    /**
     * Enum of State of UE.
     */
    public enum State {
        ACTIVE {
            @Override
            public String toString() {
                return "ACTIVE";
            }
        },
        IDLE {
            @Override
            public String toString() {
                return "IDLE";
            }
        }
    }
}

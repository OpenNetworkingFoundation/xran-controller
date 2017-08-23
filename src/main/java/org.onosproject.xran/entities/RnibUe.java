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
 * Created by dimitris on 7/22/17.
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

    public static Long hostIdtoUEId(HostId hostId) {
        String mac = hostId.mac().toString();
        mac = mac.replace(":", "");
        long l = Long.parseLong(mac, 16);
        return l;
    }

    @JsonIgnore
    public Timer getTimer() {
        return timer;
    }

    @JsonIgnore
    public void setTimer(Timer timer) {
        this.timer.cancel();
        this.timer.purge();
        this.timer = timer;
    }

    @JsonProperty("MMEUES1APID")
    public MMEUES1APID getMmeS1apId() {
        return mmeS1apId;
    }

    @JsonProperty("MMEUES1APID")
    public void setMmeS1apId(MMEUES1APID mmeS1apId) {
        this.mmeS1apId = mmeS1apId;
    }

    @JsonProperty("ENBUES1APID")
    public ENBUES1APID getEnbS1apId() {
        return enbS1apId;
    }

    @JsonProperty("ENBUES1APID")
    public void setEnbS1apId(ENBUES1APID enbS1apId) {
        this.enbS1apId = enbS1apId;
    }

    @JsonProperty("CRNTI")
    public CRNTI getCrnti() {
        return crnti;
    }

    @JsonProperty("CRNTI")
    public void setCrnti(CRNTI crnti) {
        this.crnti = crnti;
    }

    @JsonProperty("IMSI")
    public String getImsi() {
        return imsi;
    }

    @JsonProperty("IMSI")
    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

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

    @JsonProperty("MeasurementConfiguration")
    public RXSigMeasConfig getMeasConfig() {
        return measConfig;
    }

    @JsonProperty("MeasurementConfiguration")
    public void setMeasConfig(RXSigMeasConfig measConfig) {
        this.measConfig = measConfig;
    }

    @JsonProperty("Capability")
    public UECapabilityInfo getCapability() {
        return capability;
    }

    @JsonProperty("Capability")
    public void setCapability(UECapabilityInfo capability) {
        this.capability = capability;
    }

    @JsonProperty("State")
    public State getState() {
        return state;
    }

    @JsonProperty("State")
    public void setState(State state) {
        this.state = state;
    }

    public Long getId() {
        return id;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RnibUe rnibUe = (RnibUe) o;
        return Objects.equals(id, rnibUe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public enum State {
        ACTIVE {
            @Override
            public String toString() {
                return "\"ACTIVE\"";
            }
        },
        IDLE {
            @Override
            public String toString() {
                return "\"IDLE\"";
            }
        }
    }
}

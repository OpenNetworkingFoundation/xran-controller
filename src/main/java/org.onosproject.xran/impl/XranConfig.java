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

package org.onosproject.xran.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.Config;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.EUTRANCellIdentifier;
import org.onosproject.xran.codecs.api.PLMNIdentity;
import org.onosproject.xran.codecs.util.HexConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.onosproject.net.DeviceId.deviceId;

public class XranConfig extends Config<ApplicationId> {

    private static final String CELLS = "active_cells";

    private static final String PLMN_ID = "plmn_id";
    private static final String ECI_ID = "eci";

    private static final String IP_ADDR = "ip_addr";

    private static final String XRANC_PORT = "xranc_port";

    private static final String XRANC_CELLCONFIG_INTERVAL = "xranc_cellconfigrequest_interval_seconds";

    private static final String RX_SIGNAL_MEAS_REPORT_INTERVAL = "rx_signal_meas_report_interval_seconds";

    private static final String L2_MEAS_REPORT_INTERVAL = "l2_meas_report_interval_ms";

    private static final String ADMISSION_SUCCESS = "admission_success";

    private static final String BEARER_SUCCESS = "bearer_success";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public Map<String, ECGI> activeCellSet() {
        Map<String, ECGI> cells = new ConcurrentHashMap<>();

        JsonNode cellsNode = object.get(CELLS);
        if (cellsNode == null) {
            log.warn("no cells have been provided!");
            return cells;
        }

        cellsNode.forEach(cellNode -> {
            String plmn_id = cellNode.get(PLMN_ID).asText();
            String eci = cellNode.get(ECI_ID).asText();

            String ipAddress = cellNode.get(IP_ADDR).asText();

            ECGI ecgi = hexToECGI(plmn_id, eci);
            cells.put(ipAddress, ecgi);
        });

        return cells;
    }

    public boolean admissionFlag() {
        JsonNode flag = object.get(ADMISSION_SUCCESS);
        return flag != null && flag.asBoolean();
    }

    public boolean bearerFlag() {
        JsonNode flag = object.get(BEARER_SUCCESS);
        return flag != null && flag.asBoolean();
    }

    public int getXrancPort() {
        return object.get(XRANC_PORT).asInt();
    }

    public int getConfigRequestInterval() {
        return object.get(XRANC_CELLCONFIG_INTERVAL).asInt();
    }

    public int getRxSignalInterval() {
        return object.get(RX_SIGNAL_MEAS_REPORT_INTERVAL).asInt();
    }

    public int getL2MeasInterval() {
        return object.get(L2_MEAS_REPORT_INTERVAL).asInt();
    }


    private ECGI hexToECGI(String plmn_id, String eci) {
        byte[] bytes = HexConverter.fromShortHexString(plmn_id);
        byte[] bytearray = DatatypeConverter.parseHexBinary(eci);

        InputStream inputStream = new ByteArrayInputStream(bytearray);

        PLMNIdentity plmnIdentity = new PLMNIdentity(bytes);
        EUTRANCellIdentifier eutranCellIdentifier = new EUTRANCellIdentifier(bytearray, 28);

        ECGI ecgi = new ECGI();
        ecgi.setEUTRANcellIdentifier(eutranCellIdentifier);
        ecgi.setPLMNIdentity(plmnIdentity);
        try {
            ecgi.decode(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ecgi;
    }
}

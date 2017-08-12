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
import org.onosproject.net.DeviceId;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.PRBUsage;
import org.onosproject.xran.codecs.pdu.CellConfigReport;
import org.onosproject.xran.codecs.pdu.L2MeasConfig;
import org.onosproject.xran.codecs.pdu.RRMConfig;
import org.onosproject.xran.codecs.pdu.SchedMeasReportPerCell;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerInteger;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by dimitris on 7/22/17.
 */
public class RnibCell {
    private static final String SCHEME = "xran";

    private ECGI ecgi;
    private CellConfigReport conf;
    private PrbUsageContainer prbUsage;
    private SchedMeasReportPerCell.QciVals qci;
    private RRMConfig rrmConfig;
    private L2MeasConfig measConfig;

    public RnibCell() {
        prbUsage = new PrbUsageContainer();
        setDefaultRRMConf();

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

    public RRMConfig getRrmConfig() {
        return rrmConfig;
    }

    public void setRrmConfig(RRMConfig rrmConfig) {
        this.rrmConfig = rrmConfig;
    }

    public PrbUsageContainer getPrbUsage() {
        return prbUsage;
    }

    public void setPrbUsage(PrbUsageContainer prbUsage) {
        this.prbUsage = prbUsage;
    }

    private void setDefaultRRMConf() {
        rrmConfig = new RRMConfig();

        RRMConfig.Crnti crnti2 = new RRMConfig.Crnti();

        rrmConfig.setCrnti(crnti2);

        rrmConfig.setEcgi(ecgi);

        RRMConfig.StartPrbDl startPrbDl = new RRMConfig.StartPrbDl();
        startPrbDl.addBerInteger(new BerInteger(0));
        startPrbDl.addBerInteger(new BerInteger(50));

        rrmConfig.setStartPrbDl(startPrbDl);

        RRMConfig.StartPrbUl startPrbUl = new RRMConfig.StartPrbUl();
        startPrbUl.addBerInteger(new BerInteger(50));
        startPrbUl.addBerInteger(new BerInteger(100));

        rrmConfig.setStartPrbUl(startPrbUl);

        RRMConfig.EndPrbDl endPrbDl = new RRMConfig.EndPrbDl();
        endPrbDl.addBerInteger(new BerInteger(50));
        endPrbDl.addBerInteger(new BerInteger(100));

        rrmConfig.setEndPrbDl(endPrbDl);

        RRMConfig.EndPrbUl endPrbUl = new RRMConfig.EndPrbUl();
        endPrbUl.addBerInteger(new BerInteger(50));
        endPrbUl.addBerInteger(new BerInteger(100));

        rrmConfig.setEndPrbUl(endPrbUl);

        RRMConfig.SubframeBitmaskDl subframeBitmaskDl = new RRMConfig.SubframeBitmaskDl();
        BerBitString berBitString = new BerBitString(new byte[]{(byte) 0xAA, (byte) 0x80}, 10);
        BerBitString berBitString1 = new BerBitString(new byte[]{(byte) 0x55, (byte) 0x40}, 10);

        subframeBitmaskDl.addBerBitString(berBitString);
        subframeBitmaskDl.addBerBitString(berBitString1);

        rrmConfig.setSubframeBitmaskDl(subframeBitmaskDl);
    }

    public ECGI getEcgi() {
        return ecgi;
    }

    public void setEcgi(ECGI ecgi) {
        this.ecgi = ecgi;
    }

    public CellConfigReport getConf() {
        return conf;
    }

    /*public RRMConfig getRrmConfig() {
        return rrmConfig;
    }*/

    public void setConf(CellConfigReport conf) {
        this.conf = conf;
    }

    public void modifyRrmConfig(JsonNode rrmConfigNode, List<RnibUe> ueList) {
        RRMConfig.Crnti crnti = new RRMConfig.Crnti();
        ueList.forEach(ue -> crnti.addCRNTI(ue.getRanId()));

        RRMConfig.StartPrbDl startPrbDl = new RRMConfig.StartPrbDl();
        RRMConfig.EndPrbDl endPrbDl = new RRMConfig.EndPrbDl();
        int i = 0;
        if (rrmConfigNode.get("start_prb_dl").isArray()) {
            for (final JsonNode config : rrmConfigNode) {
                startPrbDl.getSeqOf().set(i, new BerInteger(config.asInt()));
                i++;
            }
        }
        i = 0;
        if (rrmConfigNode.get("end_prb_dl").isArray()) {
            for (final JsonNode config : rrmConfigNode) {
                endPrbDl.getSeqOf().set(i, new BerInteger(config.asInt()));
                i++;
            }
        }
        rrmConfig.setEndPrbDl(endPrbDl);
        rrmConfig.setStartPrbDl(startPrbDl);
        rrmConfig.setCrnti(crnti);
        rrmConfig.setEcgi(ecgi);


        // TODO
    }

    public SchedMeasReportPerCell.QciVals getQci() {
        return qci;
    }

    public void setQci(SchedMeasReportPerCell.QciVals qci) {
        this.qci = qci;
    }

    public void setPrimaryPrbUsage(PRBUsage primary) {
        this.prbUsage.primary = primary;
    }

    public void setSecondaryPrbUsage(PRBUsage secondary) {
        this.prbUsage.secondary = secondary;
    }

    public L2MeasConfig getMeasConfig() {
        return measConfig;
    }

    public void setMeasConfig(L2MeasConfig measConfig) {
        this.measConfig = measConfig;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append(ecgi != null ? "\"ecgi\":" + ecgi : "")
                .append(conf != null ? ",\n\"config-report\":" + conf : "")
                .append(prbUsage != null ? ",\n\"prb-usage\":" + prbUsage : "")
                .append(qci != null ? ",\n\"qci-vals\":" + qci : "")
                .append(rrmConfig != null ? ",\n\"rrm-config\":" + rrmConfig : "")
                .append(measConfig != null ? ",\n\"l2-meas-config\":" + measConfig : "")
                .append("\n}\n");
        return sb.toString();
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

    class PrbUsageContainer {
        PRBUsage primary;
        PRBUsage secondary;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n")
                    .append(primary != null ? "\"primary\":" + primary : "")
                    .append(secondary != null ? ",\n\"secondary\":" + secondary : "")
                    .append("\n}\n");
            return sb.toString();
        }
    }
}

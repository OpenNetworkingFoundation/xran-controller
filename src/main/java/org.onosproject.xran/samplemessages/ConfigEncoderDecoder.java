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

package org.onosproject.xran.samplemessages;

import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.EUTRANCellIdentifier;
import org.onosproject.xran.codecs.api.PLMNIdentity;
import org.onosproject.xran.codecs.pdu.*;
import org.openmuc.jasn1.ber.types.string.BerUTF8String;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * Created by dimitris on 7/25/17.
 */
public class ConfigEncoderDecoder {
    private static final Logger log =
            LoggerFactory.getLogger(ConfigEncoderDecoder.class);

    /*public String decode(String s) throws IOException {
        byte[] bytearray = DatatypeConverter.parseHexBinary(s);
        InputStream inputStream = new ByteArrayInputStream(bytearray);
        XrancPdu pdu_decoded = new XrancPdu();
        pdu_decoded.decode(inputStream);
        return pdu_decoded.toString();
    }*/
/*
    public String encodeConfigRequest()
            throws IOException {
        pdu = setPacketProperties();
        pdu.encode(os);
        return DatatypeConverter.printHexBinary(os.getArray());
    }*/

    public static XrancPdu constructPacket(ECGI ecgi) throws UnsupportedEncodingException {
        CellConfigRequest cellConfigRequest = new CellConfigRequest();
        cellConfigRequest.setEcgi(ecgi);

        BerUTF8String ver = new BerUTF8String("2a");

        XrancApiID apiID = new XrancApiID(0);
        XrancPduBody body = new XrancPduBody();
        body.setCellConfigRequest(cellConfigRequest);

        XrancPduHdr hdr = new XrancPduHdr();
        hdr.setVer(ver);
        hdr.setApiId(apiID);

        XrancPdu pdu = new XrancPdu();
        pdu.setBody(body);
        pdu.setHdr(hdr);

        return pdu;
    }

    public XrancPdu setPacketProperties() throws UnsupportedEncodingException {
        PLMNIdentity plmnIdentity = new PLMNIdentity(new byte[]{(byte) 0x22, (byte) 0x08, (byte) 0x41});
        EUTRANCellIdentifier eutranCellIdentifier = new EUTRANCellIdentifier(new byte[]{
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xF0
        }, 28);

        ECGI ecgi = new ECGI();

        ecgi.setPLMNIdentity(plmnIdentity);
        ecgi.setEUTRANcellIdentifier(eutranCellIdentifier);

        CellConfigRequest cellConfigRequest = new CellConfigRequest();
        cellConfigRequest.setEcgi(ecgi);

        BerUTF8String ver = new BerUTF8String("2a");

        XrancApiID apiID = new XrancApiID(0);
        XrancPduBody body = new XrancPduBody();
        body.setCellConfigRequest(cellConfigRequest);

        XrancPduHdr hdr = new XrancPduHdr();
        hdr.setVer(ver);
        hdr.setApiId(apiID);

        XrancPdu pdu = new XrancPdu();
        pdu.setBody(body);
        pdu.setHdr(hdr);

        return pdu;
    }

}

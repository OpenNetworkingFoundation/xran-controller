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

import org.onosproject.xran.codecs.api.CRNTI;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.pdu.*;
import org.openmuc.jasn1.ber.types.string.BerUTF8String;

import java.io.UnsupportedEncodingException;

public class HandoffRequest {

    public static XrancPdu constructPacket(CRNTI crnti, ECGI ecgis, ECGI ecgit) throws UnsupportedEncodingException {
        HORequest hoRequest = new HORequest();

        hoRequest.setCrnti(crnti);
        hoRequest.setEcgiS(ecgis);
        hoRequest.setEcgiT(ecgit);

        BerUTF8String ver = new BerUTF8String("2a");

        XrancApiID apiID = new XrancApiID(14);
        XrancPduBody body = new XrancPduBody();
        body.setHORequest(hoRequest);

        XrancPduHdr hdr = new XrancPduHdr();
        hdr.setVer(ver);
        hdr.setApiId(apiID);

        XrancPdu pdu = new XrancPdu();
        pdu.setBody(body);
        pdu.setHdr(hdr);

        return pdu;
    }
}

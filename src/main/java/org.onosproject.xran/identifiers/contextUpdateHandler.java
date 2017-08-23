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

package org.onosproject.xran.identifiers;

import org.onosproject.xran.codecs.pdu.HOComplete;
import org.onosproject.xran.codecs.pdu.UEAdmissionStatus;
import org.onosproject.xran.codecs.pdu.UEContextUpdate;

public class contextUpdateHandler {
    private UEContextUpdate contextUpdate;
    private UEAdmissionStatus admissionStatus;
    private HOComplete hoComplete;

    public UEContextUpdate getContextUpdate() {
        return contextUpdate;
    }

    public boolean setContextUpdate(UEContextUpdate contextUpdate) {
        this.contextUpdate = contextUpdate;

        return admissionStatus != null || hoComplete != null;

    }

    public UEAdmissionStatus getAdmissionStatus() {
        return admissionStatus;
    }

    public boolean setAdmissionStatus(UEAdmissionStatus admissionStatus) {
        this.admissionStatus = admissionStatus;

        return contextUpdate != null;
    }

    public HOComplete getHoComplete() {
        return hoComplete;
    }

    public boolean setHoComplete(HOComplete hoComplete) {
        this.hoComplete = hoComplete;

        return contextUpdate != null;
    }

    @Override
    public String toString() {
        return "contextUpdateHandler{" +
                "contextUpdate=" + (contextUpdate != null) +
                ", admissionStatus=" + (admissionStatus != null) +
                ", hoComplete=" + (hoComplete != null) +
                '}';
    }
}

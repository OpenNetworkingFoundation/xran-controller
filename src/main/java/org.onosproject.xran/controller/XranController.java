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

package org.onosproject.xran.controller;

import org.onosproject.xran.codecs.pdu.RRMConfig;
import org.onosproject.xran.entities.RnibLink;
import org.onosproject.xran.providers.XranDeviceListener;
import org.onosproject.xran.providers.XranHostListener;

import java.util.concurrent.SynchronousQueue;

/**
 * Created by dimitris on 7/27/17.
 */
public interface XranController {

    SynchronousQueue<String> sendHORequest(RnibLink newLink, RnibLink oldLink);

    void addListener(XranDeviceListener listener);

    void addListener(XranHostListener listener);

    void removeListener(XranDeviceListener listener);

    void removeListener(XranHostListener listener);

    SynchronousQueue<String> sendModifiedRRMConf(RRMConfig rrmConfig, boolean xICIC);

    SynchronousQueue<String> sendScellAdd(RnibLink link);

    boolean sendScellDelete(RnibLink link);
}

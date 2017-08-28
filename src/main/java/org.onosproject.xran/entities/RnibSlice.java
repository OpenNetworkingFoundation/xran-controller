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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.Set;

/**
 * Created by dimitris on 7/22/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RnibSlice {
    private long sliceId;
    private Set<RnibLink> links;
    private Map<String, String> ran2epc;
    private long validityPeriod;
    private Object desiredKpis;
    private Object deliveredKpis;
    private Object rrmSonConfiguration;

}

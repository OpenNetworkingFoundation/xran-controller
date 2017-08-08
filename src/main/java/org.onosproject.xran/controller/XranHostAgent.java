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

import io.netty.channel.ChannelHandlerContext;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibUe;

/**
 * Created by dimitris on 7/28/17.
 */
public interface XranHostAgent {
    boolean addConnectedHost(RnibUe ue, RnibCell cell, ChannelHandlerContext ctx);

    boolean removeConnectedHost(RnibUe ue);
}

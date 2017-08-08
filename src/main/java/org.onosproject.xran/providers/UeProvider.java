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

package org.onosproject.xran.providers;

import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.VlanId;
import org.onosproject.net.*;
import org.onosproject.net.host.DefaultHostDescription;
import org.onosproject.net.host.HostProvider;
import org.onosproject.net.host.HostProviderRegistry;
import org.onosproject.net.host.HostProviderService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.controller.XranController;
import org.onosproject.xran.entities.RnibUe;
import org.slf4j.Logger;

import java.util.Set;

import static org.onosproject.net.DeviceId.*;
import static org.onosproject.xran.entities.RnibCell.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by dimitris on 7/28/17.
 */

@Component(immediate = true)
public class UeProvider extends AbstractProvider implements HostProvider {

    private static final Logger log = getLogger(UeProvider.class);
    private final InternalHostListener listener = new InternalHostListener();
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private HostProviderRegistry providerRegistry;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private XranController controller;
    private HostProviderService providerService;

    public UeProvider() {
        super(new ProviderId("xran", "org.onosproject.providers.cell"));
    }

    @Activate
    public void activate() {
        providerService = providerRegistry.register(this);
        controller.addListener(listener);

        log.info("XRAN Host Provider Started");
    }

    @Deactivate
    public void deactivate() {
        controller.removeListener(listener);
        providerRegistry.unregister(this);

        providerService = null;
        log.info("XRAN Host Provider Stopped");
    }

    @Override
    public void triggerProbe(Host host) {

    }

    class InternalHostListener implements XranHostListener {

        @Override
        public void hostAdded(RnibUe ue, Set<ECGI> ecgiSet) {
            if (providerService == null) {
                return;
            }

            if (ue == null) {
                log.error("UE {} is not found", ue);
                return;
            }

            try {
                Set<HostLocation> hostLocations = Sets.newConcurrentHashSet();

                ecgiSet.forEach(ecgi -> hostLocations.add(new HostLocation(deviceId(uri(ecgi)), PortNumber.portNumber(0), 0)));

                SparseAnnotations annotations = DefaultAnnotations.builder()
                        .set(AnnotationKeys.NAME, "UE #" + ue.getMmeS1apId())
                        .build();

                DefaultHostDescription desc = new DefaultHostDescription(
                        ue.getHostId().mac(),
                        VlanId.vlanId(VlanId.UNTAGGED),
                        hostLocations,
                        Sets.newConcurrentHashSet(),
                        true,
                        annotations
                );

                providerService.hostDetected(ue.getHostId(), desc, false);
            } catch (Exception e) {
                log.warn(e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void hostRemoved(HostId id) {
            providerService.hostVanished(id);
        }
    }
}

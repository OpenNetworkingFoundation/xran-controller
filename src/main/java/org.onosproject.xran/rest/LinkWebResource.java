/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.xran.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.annotations.Patch;
import org.onosproject.xran.controller.XranController;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibLink;
import org.onosproject.xran.entities.RnibUe;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Link web resource.
 */
@Path("links")
public class LinkWebResource extends AbstractWebResource {

    private static final Logger log =
            LoggerFactory.getLogger(LinkWebResource.class);

    /**
     * test.
     *
     * @param eciHex test
     * @param ue     test
     * @return test
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLinksBetween(@DefaultValue("") @QueryParam("cell") String eciHex,
                                    @DefaultValue("-1") @QueryParam("ue") long ue) {
        List<RnibLink> list = Lists.newArrayList();
        if (!eciHex.isEmpty() && ue != -1) {
            RnibLink link = get(XranStore.class).getLinkBetweenCellIdUeId(eciHex, ue);
            if (link != null) {
                list.add(link);
            }
        } else if (!eciHex.isEmpty()) {
            list.addAll(get(XranStore.class).getLinksByCellId(eciHex));
        } else if (ue != -1) {
            list.addAll(get(XranStore.class).getLinksByUeId(ue));
        } else {
            list.addAll(get(XranStore.class).getLinks());
        }

        try {
            ObjectNode rootNode = mapper().createObjectNode();
            JsonNode jsonNode = mapper().readTree(list.toString());
            rootNode.put("links", jsonNode);
            return Response.ok(rootNode.toString()).build();
        } catch (IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
            return Response.serverError()
                    .entity(ExceptionUtils.getFullStackTrace(e))
                    .build();
        }
    }

    /**
     * test.
     *
     * @param src    test
     * @param dst    test
     * @param stream test
     * @return test
     */
    @Patch
    @Path("{src},{dst}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response patchLinks(@PathParam("src") String src, @PathParam("dst") long dst, InputStream stream) {
        RnibLink link = get(XranStore.class).getLinkBetweenCellIdUeId(src, dst);
        if (link != null) {
            try {
                ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);

                JsonNode type = jsonTree.path("type");
                if (!type.isMissingNode()) {
                    RnibLink.Type anEnum = RnibLink.Type.getEnum(type.asText());
                    return handleTypeChange(link, anEnum);
                }

                JsonNode trafficpercent = jsonTree.path("trafficpercent");
                if (!trafficpercent.isMissingNode()) {
                    return handleTrafficChange(link, trafficpercent);
                }

                JsonNode rrmConf = jsonTree.path("RRMConf");
                if (!rrmConf.isMissingNode()) {
                    return handleRRMChange(link, rrmConf);
                }

            } catch (Exception e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
                return Response.serverError().entity(ExceptionUtils.getFullStackTrace(e)).build();
            }
        }
        return Response.serverError().entity("link not found use POST request").build();
    }

    /**
     * test.
     *
     * @param src    test
     * @param dst    test
     * @param stream test
     * @return test
     */
    @POST
    @Path("{src},{dst}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postLinks(@PathParam("src") String src, @PathParam("dst") long dst, InputStream stream) {
        RnibCell cell = get(XranStore.class).getCell(src);
        RnibUe ue = get(XranStore.class).getUe(dst);

        if (cell == null) {
            return Response.serverError()
                    .entity("cell not found")
                    .build();
        }

        if (ue == null) {
            return Response.serverError()
                    .entity("ue not found")
                    .build();
        }

        if (get(XranStore.class).getLink(cell.getEcgi(), ue.getMmeS1apId()) != null) {
            return Response.serverError()
                    .entity("link exists use PATCH request")
                    .build();
        }

        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);

            JsonNode type = jsonTree.path("type");

            if (!type.isMissingNode()) {
                RnibLink link = new RnibLink(cell, ue);
                link.setType(RnibLink.Type.getEnum(type.asText()));
                get(XranStore.class).storeLink(link);

                // TODO: trigger the scell add
            }
        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
            return Response.serverError()
                    .entity(ExceptionUtils.getFullStackTrace(e))
                    .build();
        }

        return Response.serverError()
                .entity("unreachable code")
                .build();
    }

    private Response handleTypeChange(RnibLink link, RnibLink.Type newType) throws InterruptedException {
        final SynchronousQueue<String>[] queue = new SynchronousQueue[1];

        if (newType.equals(RnibLink.Type.SERVING_PRIMARY)) {
            List<RnibLink> linksByUeId = get(XranStore.class).getLinksByUeId(link.getLinkId().getMmeues1apid().longValue());

            Optional<RnibLink> primary = linksByUeId.stream()
                    .filter(l -> l.getType().equals(RnibLink.Type.SERVING_PRIMARY))
                    .findFirst();
            if (primary.isPresent()) {
                queue[0] = get(XranController.class).sendHORequest(link, primary.get());
                String poll = queue[0].poll(5, TimeUnit.SECONDS);

                if (poll != null) {
                    return Response.ok()
                            .entity(poll)
                            .build();
                } else {
                    return Response.serverError()
                            .entity("did not receive response in time")
                            .build();
                }
            } else {
                link.setType(RnibLink.Type.SERVING_PRIMARY);
                return Response.ok()
                        .entity("there was not another primary link")
                        .build();
            }
        } else if (newType.equals(RnibLink.Type.NON_SERVING)) {
            switch (link.getType()) {
                case NON_SERVING:
                    return Response.ok()
                            .entity("It's already a non serving link!" + link)
                            .build();
                case SERVING_PRIMARY:
                    return Response.serverError()
                            .entity("Cannot change a Primary link.")
                            .build();
                case SERVING_SECONDARY_CA:
                case SERVING_SECONDARY_DC:
                    if (get(XranController.class).sendScellDelete(link)) {
                        return Response.ok()
                                .entity("Successfully changed link type to " + link.getType())
                                .build();
                    } else {
                        return Response.serverError()
                                .entity("Could not change link type.")
                                .build();
                    }
            }
        } else if (newType.equals(RnibLink.Type.SERVING_SECONDARY_CA)) {
            switch (link.getType()) {
                case SERVING_PRIMARY:
                    return Response.serverError()
                            .entity("Cannot change a Primary link.")
                            .build();
                case SERVING_SECONDARY_DC:
                case NON_SERVING:
                    queue[0] = get(XranController.class).sendScellAdd(link);
                    String poll = queue[0].poll(5, TimeUnit.SECONDS);
                    if (poll != null) {
                        return Response.ok()
                                .entity("Successfully changed link type to " + link.getType())
                                .build();
                    } else {
                        return Response.serverError()
                                .entity("did not receive response in time")
                                .build();
                    }
                case SERVING_SECONDARY_CA:
                    return Response.ok()
                            .entity("It's already a service secondary ca link!")
                            .build();
            }
        }

        return Response.serverError()
                .entity("Unknown type")
                .build();
    }

    private Response handleTrafficChange(RnibLink link, JsonNode trafficpercent) {
        JsonNode jsonNode = trafficpercent.path("traffic-percent-dl");
        if (!jsonNode.isMissingNode()) {
            link.getTrafficPercent().setTrafficPercentDl(new BerInteger(jsonNode.asInt()));
        }

        jsonNode = trafficpercent.path("traffic-percent-ul");
        if (!jsonNode.isMissingNode()) {
            link.getTrafficPercent().setTrafficPercentUl(new BerInteger(jsonNode.asInt()));
        }

        return Response.ok("trafficpercent changed successfully").build();
    }

    private Response handleRRMChange(RnibLink link, JsonNode rrmConf) throws InterruptedException {
        final SynchronousQueue<String>[] queue = new SynchronousQueue[1];
        get(XranStore.class).modifyLinkRrmConf(link, rrmConf);
        queue[0] = get(XranController.class).sendModifiedRRMConf(link.getRrmParameters(),
                link.getLinkId().getCell().getVersion().equals("3"));
        String poll = queue[0].poll(5, TimeUnit.SECONDS);

        if (poll != null) {
            return Response.ok()
                    .entity(poll)
                    .build();
        } else {
            return Response.serverError()
                    .entity("did not receive response in time")
                    .build();
        }
    }
}

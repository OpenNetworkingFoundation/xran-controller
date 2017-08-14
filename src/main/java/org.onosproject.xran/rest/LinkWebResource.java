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
import org.onosproject.xran.entities.RnibLink;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

                JsonNode type = jsonTree.get("type");
                if (type != null) {
                    final SynchronousQueue<String>[] queue = new SynchronousQueue[1];
                    RnibLink.Type linkType = RnibLink.Type.getEnum(type.asText());
                    if (linkType.equals(RnibLink.Type.SERVING_PRIMARY)) {
                        List<RnibLink> linksByUeId = get(XranStore.class).getLinksByUeId(dst);
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
                        }
                    }
                }

                JsonNode trafficpercent = jsonTree.get("trafficpercent");
                if (trafficpercent != null) {
                    JsonNode jsonNode = trafficpercent.get("traffic-percent-dl");
                    if (jsonNode != null) {
                        link.getTrafficPercent().setTrafficPercentDl(new BerInteger(jsonNode.asInt()));
                    }
                    jsonNode = trafficpercent.get("traffic-percent-ul");
                    if (jsonNode != null) {
                        link.getTrafficPercent().setTrafficPercentUl(new BerInteger(jsonNode.asInt()));
                    }
                    return Response.ok("trafficpercent changed successfully").build();
                }

                JsonNode rrmConf = jsonTree.get("RRMConf");
                if (rrmConf != null) {
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

            } catch (Exception e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
                return Response.serverError().entity(ExceptionUtils.getFullStackTrace(e)).build();
            }
        }
        return Response.serverError().entity("link not found").build();
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
        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);

            JsonNode type = jsonTree.get("type");

            if (type != null) {
                boolean b;
                b = get(XranStore.class).createLinkBetweenCellIdUeId(src, dst, type.asText());
                return ok(b).build();
            }
        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
            return Response.serverError()
                    .entity(ExceptionUtils.getFullStackTrace(e))
                    .build();
        }

        return Response.serverError().entity("unreachable code").build();
    }

}

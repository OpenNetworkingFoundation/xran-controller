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

import com.fasterxml.jackson.annotation.JsonInclude;
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
import org.onosproject.xran.codecs.ber.types.BerInteger;
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

    public LinkWebResource() {

    }

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

        if (list.size() > 0) {
            try {
                JsonNode jsonNode = mapper().valueToTree(list);

                return ResponseHelper.getResponse(
                        mapper(),
                        ResponseHelper.statusCode.OK,
                        jsonNode
                );
            } catch (Exception e) {
                String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
                log.error(fullStackTrace);
                e.printStackTrace();

                return ResponseHelper.getResponse(
                        mapper(),
                        ResponseHelper.statusCode.INTERNAL_SERVER_ERROR,
                        "Exception",
                        fullStackTrace
                );
            }
        }

        return ResponseHelper.getResponse(
                mapper(),
                ResponseHelper.statusCode.NOT_FOUND,
                "Not Found",
                "Specified links not found"
        );
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
    @Produces(MediaType.APPLICATION_JSON)
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

                return ResponseHelper.getResponse(
                        mapper(),
                        ResponseHelper.statusCode.NOT_IMPLEMENTED,
                        "Not Implemented",
                        "The command you specified is not implemented or doesn't exist. We support " +
                                "type/RRMConf/traficpercent commands."
                );

            } catch (Exception e) {
                String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
                log.error(fullStackTrace);
                e.printStackTrace();

                return ResponseHelper.getResponse(
                        mapper(),
                        ResponseHelper.statusCode.INTERNAL_SERVER_ERROR,
                        "Exception",
                        fullStackTrace
                );
            }
        }

        return ResponseHelper.getResponse(
                mapper(),
                ResponseHelper.statusCode.NOT_FOUND,
                "Not Found",
                "Link not found use POST request"
        );
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response postLinks(@PathParam("src") String src, @PathParam("dst") long dst, InputStream stream) {
        RnibCell cell = get(XranStore.class).getCell(src);
        RnibUe ue = get(XranStore.class).getUe(dst);

        if (cell == null) {
            return ResponseHelper.getResponse(
                    mapper(),
                    ResponseHelper.statusCode.NOT_FOUND,
                    "Not Found",
                    "Cell " + src + " was not found"
            );
        }

        if (ue == null) {
            return ResponseHelper.getResponse(
                    mapper(),
                    ResponseHelper.statusCode.NOT_FOUND,
                    "Not Found",
                    "Ue with " + dst + " was not found"
            );
        }

        if (get(XranStore.class).getLink(cell.getEcgi(), ue.getMmeS1apId()) != null) {
            return ResponseHelper.getResponse(
                    mapper(),
                    ResponseHelper.statusCode.BAD_REQUEST,
                    "Bad Request",
                    "Link already exists use PATCH to modify"
            );
        }

        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);

            JsonNode type = jsonTree.path("type");

            RnibLink link = new RnibLink(cell, ue);
            // store it as non-serving when creating link
            get(XranStore.class).storeLink(link);
            if (!type.isMissingNode()) {
                return handleTypeChange(link, RnibLink.Type.getEnum(type.asText()));
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
            String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
            log.error(fullStackTrace);
            e.printStackTrace();

            return ResponseHelper.getResponse(
                    mapper(),
                    ResponseHelper.statusCode.INTERNAL_SERVER_ERROR,
                    "Exception",
                    fullStackTrace
            );
        }

        return ResponseHelper.getResponse(
                mapper(),
                ResponseHelper.statusCode.NOT_IMPLEMENTED,
                "Not Implemented",
                "The command you specified is not implemented or doesn't exist. We support " +
                        "type/RRMConf/traficpercent commands."
        );
    }

    private Response handleTypeChange(RnibLink link, RnibLink.Type newType) throws InterruptedException {
        final SynchronousQueue<String>[] queue = new SynchronousQueue[1];

        if (newType.equals(RnibLink.Type.SERVING_PRIMARY)) {
            switch (link.getType()) {
                case SERVING_PRIMARY: {
                    return ResponseHelper.getResponse(
                            mapper(),
                            ResponseHelper.statusCode.BAD_REQUEST,
                            "Bad Request",
                            "Link is already a primary link"
                    );
                }
                case SERVING_SECONDARY_CA:
                case SERVING_SECONDARY_DC:
                case NON_SERVING: {
                    List<RnibLink> linksByUeId = get(XranStore.class).getLinksByUeId(link.getLinkId().getMmeues1apid().longValue());

                    Optional<RnibLink> primary = linksByUeId.stream()
                            .filter(l -> l.getType().equals(RnibLink.Type.SERVING_PRIMARY))
                            .findFirst();
                    if (primary.isPresent()) {
                        queue[0] = get(XranController.class).sendHORequest(link, primary.get());
                        String poll = queue[0].poll(5, TimeUnit.SECONDS);

                        if (poll != null) {
                            return ResponseHelper.getResponse(
                                    mapper(),
                                    ResponseHelper.statusCode.OK,
                                    "Handoff Response",
                                    poll
                            );
                        } else {
                            return ResponseHelper.getResponse(
                                    mapper(),
                                    ResponseHelper.statusCode.REQUEST_TIMEOUT,
                                    "Handoff Timeout",
                                    "eNodeB did not send a HOComplete/HOFailure on time"
                            );
                        }
                    } else {
                        link.setType(RnibLink.Type.SERVING_PRIMARY);
                        return ResponseHelper.getResponse(
                                mapper(),
                                ResponseHelper.statusCode.OK,
                                "OK",
                                "Link set to primary"
                        );
                    }
                }
            }
        } else if (newType.equals(RnibLink.Type.NON_SERVING)) {
            switch (link.getType()) {
                case NON_SERVING:
                    return ResponseHelper.getResponse(
                            mapper(),
                            ResponseHelper.statusCode.BAD_REQUEST,
                            "Bad Request",
                            "Link is already a primary link"
                    );
                case SERVING_PRIMARY:
                    return ResponseHelper.getResponse(
                            mapper(),
                            ResponseHelper.statusCode.BAD_REQUEST,
                            "Bad Request",
                            "Cannot modify a primary link"
                    );
                case SERVING_SECONDARY_CA:
                case SERVING_SECONDARY_DC:
                    if (get(XranController.class).sendScellDelete(link)) {
                        return ResponseHelper.getResponse(
                                mapper(),
                                ResponseHelper.statusCode.OK,
                                "OK",
                                "Link set to non-serving"
                        );
                    } else {
                        return ResponseHelper.getResponse(
                                mapper(),
                                ResponseHelper.statusCode.NOT_FOUND,
                                "Not Found",
                                "Could not find cell config report to construct Scell Delete"
                        );
                    }
            }
        } else if (newType.equals(RnibLink.Type.SERVING_SECONDARY_CA)) {
            switch (link.getType()) {
                case SERVING_PRIMARY:
                    return ResponseHelper.getResponse(
                            mapper(),
                            ResponseHelper.statusCode.BAD_REQUEST,
                            "Bad Request",
                            "Cannot modify a primary link"
                    );
                case SERVING_SECONDARY_DC:
                case NON_SERVING:
                    queue[0] = get(XranController.class).sendScellAdd(link);
                    String poll = queue[0].poll(5, TimeUnit.SECONDS);
                    if (poll != null) {
                        return ResponseHelper.getResponse(
                                mapper(),
                                ResponseHelper.statusCode.OK,
                                "ScellAdd Response",
                                poll
                        );
                    } else {
                        return ResponseHelper.getResponse(
                                mapper(),
                                ResponseHelper.statusCode.REQUEST_TIMEOUT,
                                "ScellAdd Timeout",
                                "eNodeB did not send a ScellAddStatus on time"
                        );
                    }
                case SERVING_SECONDARY_CA:
                    return ResponseHelper.getResponse(
                            mapper(),
                            ResponseHelper.statusCode.BAD_REQUEST,
                            "Bad Request",
                            "Link is already a secondary CA link"
                    );
            }
        }

        return ResponseHelper.getResponse(
                mapper(),
                ResponseHelper.statusCode.NOT_IMPLEMENTED,
                "Not Implemented",
                "This request is not implemented"
        );
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

        return ResponseHelper.getResponse(
                mapper(),
                ResponseHelper.statusCode.OK,
                "OK",
                "Traffic Percent changed"
        );
    }

    private Response handleRRMChange(RnibLink link, JsonNode rrmConf) throws InterruptedException {
        final SynchronousQueue<String>[] queue = new SynchronousQueue[1];
        get(XranStore.class).modifyLinkRrmConf(link, rrmConf);
        queue[0] = get(XranController.class).sendModifiedRRMConf(link.getRrmParameters(),
                link.getLinkId().getCell().getVersion() <= 3);
        String poll = queue[0].poll(5, TimeUnit.SECONDS);

        if (poll != null) {
            return ResponseHelper.getResponse(
                    mapper(),
                    ResponseHelper.statusCode.OK,
                    "RRMConfig Response",
                    poll
            );
        } else {
            return ResponseHelper.getResponse(
                    mapper(),
                    ResponseHelper.statusCode.REQUEST_TIMEOUT,
                    "RRMConfig Timeout",
                    "eNodeB did not send a RRMConfingStatus on time"
            );
        }
    }
}

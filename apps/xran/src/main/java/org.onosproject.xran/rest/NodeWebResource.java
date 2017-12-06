/*
 * Copyright 2016-present Open Networking Foundation
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.xran.XranStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Node web resource.
 */
@Path("nodes")
public class NodeWebResource extends AbstractWebResource {

    private static final Logger log =
            LoggerFactory.getLogger(NodeWebResource.class);

    public NodeWebResource() {

    }

    /**
     * List all the nodes in the R-NIB.
     *
     * @param type Type of node (cell/ue)
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNodes(@DefaultValue("") @QueryParam("type") String type) {
        JsonNode jsonNode;
        try {
            List<Object> nodes;
            // List cell type of nodes or UE type of nodes.
            if (StringUtils.isBlank(type)) {
                nodes = get(XranStore.class).getNodes();
            } else if (type.equals("cell")) {
                nodes = get(XranStore.class).getcellnodes();
            } else if (type.equals("ue")) {
                nodes = get(XranStore.class).getuenodes();
            } else {
                return ResponseHelper.getResponse(
                        mapper(),
                        ResponseHelper.StatusCode.NOT_FOUND,
                        "Not Found",
                        "Type of node was not found"
                );
            }

            if (nodes.size() == 0) {
                return ResponseHelper.getResponse(
                        mapper(),
                        ResponseHelper.StatusCode.NOT_FOUND,
                        "Not Found",
                        "No nodes found"
                );
            }

            jsonNode = mapper().valueToTree(nodes);
        } catch (Exception e) {
            String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
            log.error(fullStackTrace);
            e.printStackTrace();

            return ResponseHelper.getResponse(
                    mapper(),
                    ResponseHelper.StatusCode.INTERNAL_SERVER_ERROR,
                    "Exception",
                    fullStackTrace
            );
        }

        return ResponseHelper.getResponse(
                mapper(),
                ResponseHelper.StatusCode.OK,
                jsonNode
        );
    }

    /**
     * List the node with a specific node id.
     *
     * @param nodeid ID of the node
     * @return Response
     */
    @GET
    @Path("{nodeid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNodeid(@PathParam("nodeid") String nodeid) {
        Object node = get(XranStore.class).getbynodeid(nodeid);

        if (node != null) {
            try {
                JsonNode jsonNode = mapper().valueToTree(node);

                return ResponseHelper.getResponse(
                        mapper(),
                        ResponseHelper.StatusCode.OK,
                        jsonNode
                );
            } catch (Exception e) {
                String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
                log.error(fullStackTrace);
                e.printStackTrace();

                return ResponseHelper.getResponse(
                        mapper(),
                        ResponseHelper.StatusCode.INTERNAL_SERVER_ERROR,
                        "Exception",
                        fullStackTrace
                );
            }
        }

        return ResponseHelper.getResponse(
                mapper(),
                ResponseHelper.StatusCode.NOT_FOUND,
                "Not Found",
                "Node " + nodeid + " was not found"
        );
    }

}

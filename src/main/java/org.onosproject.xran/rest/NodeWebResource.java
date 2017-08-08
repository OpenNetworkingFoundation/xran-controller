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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.entities.RnibUe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * Node web resource.
 */
@Path("nodes")
public class NodeWebResource extends AbstractWebResource {

    private static final Logger log =
            LoggerFactory.getLogger(NodeWebResource.class);

    /**
     * test.
     *
     * @param type test
     * @return test
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNodes(@DefaultValue("") @QueryParam("type") String type) {
        log.debug("GET NODES " + type);

        ObjectNode rootNode = mapper().createObjectNode();

        try {
            if (StringUtils.isBlank(type)) {
                List<Object> nodes = get(XranStore.class).getNodes();

                JsonNode jsonNode = mapper().readTree(nodes.get(0).toString());
                JsonNode jsonNode2 = mapper().readTree(nodes.get(1).toString());

                ObjectNode arrayNode = rootNode.putObject("nodes");
                arrayNode.put("cells", jsonNode);
                arrayNode.put("ues", jsonNode2);
            } else if (type.equals("cell")) {
                List<RnibCell> cellNodes = get(XranStore.class).getCellNodes();
                JsonNode jsonNode = mapper().readTree(cellNodes.toString());

                ObjectNode arrayNode = rootNode.putObject("nodes");
                arrayNode.put("cells", jsonNode);
            } else if (type.equals("ue")) {
                List<RnibUe> ueNodes = get(XranStore.class).getUeNodes();
                JsonNode jsonNode = mapper().readTree(ueNodes.toString());

                ObjectNode arrayNode = rootNode.putObject("nodes");
                arrayNode.put("ues", jsonNode);
            }
        } catch (IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
        }

        return ok(rootNode.toString()).build();
    }

    /**
     * test.
     *
     * @param nodeid test
     * @return test
     */
    @GET
    @Path("{nodeid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNodeid(@PathParam("nodeid") String nodeid) {
        log.debug("GET NODEID {}", nodeid);

        Object node = get(XranStore.class).getByNodeId(nodeid);

        ObjectNode rootNode = mapper().createObjectNode();

        if (node != null) {
            try {
                JsonNode jsonNode = mapper().readTree(node.toString());
                rootNode.put("node", jsonNode);
            } catch (IOException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
            }
        } else {
            rootNode.put("error", "not found");
        }

        return ok(rootNode.toString()).build();
    }

}

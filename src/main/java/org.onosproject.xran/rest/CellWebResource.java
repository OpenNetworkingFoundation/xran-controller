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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.annotations.Patch;
import org.onosproject.xran.codecs.api.ECGI;
import org.onosproject.xran.codecs.api.EUTRANCellIdentifier;
import org.onosproject.xran.controller.XranController;
import org.onosproject.xran.entities.RnibCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.SynchronousQueue;

/**
 * Cell web resource.
 */
@Path("cell")
public class CellWebResource extends AbstractWebResource {

    private static final Logger log =
            LoggerFactory.getLogger(CellWebResource.class);

    /**
     * test.
     *
     * @param eciHex test
     * @return test
     */
    @GET
    @Path("{cellid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCell(@PathParam("cellid") String eciHex) {
        log.debug("GET CELLID {}", eciHex);

        RnibCell cell = get(XranStore.class).getCell(eciHex);

        ObjectNode rootNode = mapper().createObjectNode();

        if (cell != null) {
            try {
                JsonNode jsonNode = mapper().readTree(cell.toString());
                rootNode.put("cell", jsonNode);
            } catch (IOException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
                e.printStackTrace();
            }
        } else {
            rootNode.put("error", "not found");
        }

        return ok(rootNode.toString()).build();
    }

    /**
     * test.
     *
     * @param eciHex test
     * @param stream test (body of request)
     * @return test
     */
    @Patch
    @Path("{cellid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response patchCell(@PathParam("cellid") String eciHex, InputStream stream) {
        log.debug("PATCH CELLID {}", eciHex);

        boolean b;

        RnibCell cell = get(XranStore.class).getCell(eciHex);
        // Check if a cell with that ECI exists. If it does, then modify its contents.

        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);

            JsonNode rrmConf = jsonTree.get("RRMConf");
            if (rrmConf != null) {
                final SynchronousQueue<String>[] queue = new SynchronousQueue[1];
                b = get(XranStore.class).modifyCellRrmConf(cell, rrmConf);
                if (b) {
                    queue[0] = get(XranController.class).sendModifiedRRMConf(cell);
                    return Response.ok().entity(queue[0].take()).build();
                }
            }
        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
            return Response.serverError().entity(ExceptionUtils.getFullStackTrace(e)).build();
        }
        return Response.noContent().build();
    }

}

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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.annotations.Patch;
import org.onosproject.xran.controller.XranController;
import org.onosproject.xran.entities.RnibCell;
import org.onosproject.xran.rest.ResponseHelper.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Cell web resource.
 */
@Path("cell")
public class CellWebResource extends AbstractWebResource {

    private static final Logger log =
            LoggerFactory.getLogger(CellWebResource.class);

    public CellWebResource() {
    }

    /**
     * Lists the cell with {cellid}.
     *
     * @param eciHex EutranCellIdentifier in binary
     * @return Response
     */
    @GET
    @Path("{cellid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCell(@PathParam("cellid") String eciHex) {
        RnibCell cell = get(XranStore.class).getCell(eciHex);

        if (cell != null) {
            try {
                JsonNode jsonNode = mapper().valueToTree(cell);

                return ResponseHelper.getResponse(
                        mapper(),
                        StatusCode.OK,
                        jsonNode
                );

            } catch (Exception e) {
                String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
                log.error(fullStackTrace);
                e.printStackTrace();

                return ResponseHelper.getResponse(
                        mapper(),
                        StatusCode.INTERNAL_SERVER_ERROR,
                        "Exception",
                        fullStackTrace
                );
            }
        }

        return ResponseHelper.getResponse(
                mapper(),
                StatusCode.NOT_FOUND,
                "Not Found",
                "Cell with " + eciHex + " was not found"
        );
    }

    /**
     * Modify the RRMConfig parameters of the cell.
     *
     * @param eciHex EutranCellIdentifier in binary
     * @param stream Parameters that you want to modify
     * @return Response
     */
    @Patch
    @Path("{cellid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response patchCell(@PathParam("cellid") String eciHex, InputStream stream) {
        RnibCell cell = get(XranStore.class).getCell(eciHex);

        if (cell != null) {
            try {
                ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);

                JsonNode rrmConf = jsonTree.path("RRMConf");
                if (!rrmConf.isMissingNode()) {
                    final SynchronousQueue<String>[] queue = new SynchronousQueue[1];
                    get(XranStore.class).modifycellrrmconf(cell, rrmConf);

                    queue[0] = get(XranController.class).sendmodifiedrrmconf(cell.getRrmConfig(),
                            cell.getVersion() <= 3);
                    String poll = queue[0].poll(get(XranController.class)
                            .getNorthboundTimeout(), TimeUnit.MILLISECONDS);

                    if (poll != null) {
                        return ResponseHelper.getResponse(
                                mapper(),
                                StatusCode.OK,
                                "Handoff Response",
                                poll
                        );
                    } else {
                        return ResponseHelper.getResponse(
                                mapper(),
                                StatusCode.REQUEST_TIMEOUT,
                                "Handoff Timeout",
                                "eNodeB did not send a HOComplete/HOFailure on time"
                        );
                    }
                }

                return ResponseHelper.getResponse(
                        mapper(),
                        StatusCode.BAD_REQUEST,
                        "Bad Request",
                        "The command you specified is not implemented or doesn't exist. We support " +
                                "RRMConf commands."
                );
            } catch (Exception e) {
                String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
                log.error(fullStackTrace);
                e.printStackTrace();

                return ResponseHelper.getResponse(
                        mapper(),
                        StatusCode.INTERNAL_SERVER_ERROR,
                        "Exception",
                        fullStackTrace
                );
            }
        }

        return ResponseHelper.getResponse(
                mapper(),
                StatusCode.NOT_FOUND,
                "Not Found",
                "Cell " + eciHex + " was not found"
        );
    }

}

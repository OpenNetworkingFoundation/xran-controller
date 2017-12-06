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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.xran.XranStore;
import org.onosproject.xran.entities.RnibSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Slice web resource.
 */
@Path("slice")
public class SliceWebResource extends AbstractWebResource {

    private static final Logger log =
            LoggerFactory.getLogger(SliceWebResource.class);

    public SliceWebResource() {

    }

    /**
     * List the slice with the given slice ID.
     *
     * @param sliceid ID of the slice
     * @return Response
     */
    @GET
    @Path("{sliceid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSlice(@PathParam("sliceid") long sliceid) {
        RnibSlice slice = get(XranStore.class).getSlice(sliceid);

        if (slice != null) {
            try {
                JsonNode jsonNode = mapper().valueToTree(slice);

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
                "Slice " + sliceid + " not found"
        );
    }

    /**
     * Create slice with the corresponding attributes.
     *
     * @param stream Attributes to create slice
     * @return Response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postSlice(InputStream stream) {
        try {
//            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);
//            get(XranStore.class).createSlice(jsonTree);

            // FIXME: change when implemented
            return ResponseHelper.getResponse(
                    mapper(),
                    ResponseHelper.StatusCode.NOT_IMPLEMENTED,
                    "Not Implemented",
                    "POST Slice not implemented"
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

}

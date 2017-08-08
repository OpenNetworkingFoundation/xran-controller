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
import java.io.IOException;
import java.io.InputStream;

/**
 * Slice web resource.
 */
@Path("slice")
public class SliceWebResource extends AbstractWebResource {

    private static final Logger log =
            LoggerFactory.getLogger(SliceWebResource.class);

    /**
     * test.
     *
     * @param sliceid test
     * @return test
     */
    @GET
    @Path("{sliceid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSlice(@PathParam("sliceid") long sliceid) {
        log.debug("GET SLICE {}", sliceid);

        RnibSlice slice = get(XranStore.class).getSlice(sliceid);

        ObjectNode rootNode = mapper().createObjectNode();

        if (slice != null) {
            try {
                JsonNode jsonNode = mapper().readTree(slice.toString());
                rootNode.put("slice", jsonNode);
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
     * @param stream test
     * @return test
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postSlice(InputStream stream) {
        log.debug("POST SLICE");

        boolean b = false;
        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);

            b = get(XranStore.class).createSlice(jsonTree);
        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
        }

        return ok(b).build();
    }

}

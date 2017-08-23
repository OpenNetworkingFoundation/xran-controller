/*
 * Copyright 2015-present Open Networking Foundation
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.core.Response;

public class ResponseHelper {

    public static Response getResponse(ObjectMapper mapper, statusCode status, String title, String detail) {
        ObjectNode rootNode = mapper.createObjectNode();

        switch (status) {
            case OK: {
                ArrayNode data = rootNode.putArray("data");
                ObjectNode addObject = data.addObject();
                addObject.put("status", status.status);
                addObject.put("title", title);
                addObject.put("detail", detail);
                return Response.status(status.status)
                        .entity(rootNode.toString())
                        .build();
            }
            case BAD_REQUEST:
            case NOT_IMPLEMENTED:
            case REQUEST_TIMEOUT:
            case INTERNAL_SERVER_ERROR:
            case NOT_FOUND: {
                ArrayNode errors = rootNode.putArray("errors");
                ObjectNode addObject = errors.addObject();
                addObject.put("status", status.status);
                addObject.put("title", title);
                addObject.put("detail", detail);
                return Response.status(status.status)
                        .entity(rootNode.toString())
                        .build();
            }
            default:
                return Response.noContent().build();
        }
    }

    public static Response getResponse(ObjectMapper mapper, statusCode status, JsonNode node) {
        ObjectNode rootNode = mapper.createObjectNode();

        switch (status) {
            case OK:
            case BAD_REQUEST:
            case NOT_IMPLEMENTED:
            case REQUEST_TIMEOUT:
            case INTERNAL_SERVER_ERROR:
            case NOT_FOUND: {
                ArrayNode data = rootNode.putArray("data");
                data.add(node);
                return Response.status(status.status)
                        .entity(rootNode.toString())
                        .build();
            }
            default:
                return Response.noContent().build();
        }
    }

    public enum statusCode {
        OK(200),
        BAD_REQUEST(400),
        NOT_FOUND(404),
        REQUEST_TIMEOUT(408),
        INTERNAL_SERVER_ERROR(500),
        NOT_IMPLEMENTED(501);

        public int status;

        statusCode(int status) {
            this.status = status;
        }
    }
}

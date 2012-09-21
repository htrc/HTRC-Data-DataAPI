/*
#
# Copyright 2012 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  StatusResource.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.Constants;

/**
 * @author Yiming Sun
 *
 */

@Path("/status")
public class StatusResource {
    private static Logger log = Logger.getLogger(StatusResource.class);

    @GET
    @Produces("application/json")
    public Response getResponseGet() {
        Response response = null;
        AsyncJobManager asyncJobManager = AsyncJobManager.getInstance();
        Map<String, String> statusMap = asyncJobManager.getStatus();
        StringBuilder jsonBuilder = new StringBuilder("{");
        Set<String> keySet = statusMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        if (iterator.hasNext()) {
            String key = iterator.next();
            jsonBuilder.append("\"").append(key).append("\"").append(" : ").append("\"").append(statusMap.get(key)).append("\"");
        }
        while (iterator.hasNext()) {
            String key = iterator.next();
            jsonBuilder.append(",").append("\"").append(key).append("\"").append(" : ").append("\"").append(statusMap.get(key)).append("\"");
        }
        jsonBuilder.append("}");
        
        response = Response.ok(jsonBuilder.toString()).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON).build();
        return response;
    }
}


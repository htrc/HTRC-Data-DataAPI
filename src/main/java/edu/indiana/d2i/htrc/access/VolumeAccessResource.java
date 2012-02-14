/*
#
# Copyright 2007 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or areed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  VolumeAccessResource.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.text.ParseException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.IDTypeEnum;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser;
import edu.indiana.d2i.htrc.access.read.HectorVolumeRetriever;
import edu.indiana.d2i.htrc.access.response.VolumeZipStreamingOutput;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory.ZipTypeEnum;

/**
 * @author Yiming Sun
 *
 */

@Path("/volumes")
public class VolumeAccessResource {
    
//    @Context ServletContext sc;
    
    private static Logger log = Logger.getLogger(VolumeAccessResource.class);
    
    @GET
    public Response getResource(@QueryParam("volumeIDs") String volumeIDs, 
                               @QueryParam("concat") boolean concatenate,
                               @QueryParam("version") int version) {
        
        if (log.isDebugEnabled()) {
            log.debug("volumeIDs = " + volumeIDs);
            log.debug("concatenate = " + concatenate);
            log.debug("version = " + version);
        }
                
        
        Response response = null;
        
        Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID);
        try {
            if (volumeIDs != null) {
                List<? extends HTRCItemIdentifier> volumeIDList = parser.parse(volumeIDs);
                VolumeRetriever volumeRetriever = new HectorVolumeRetriever(volumeIDList, HectorResourceSingleton.getInstance());
                ZipTypeEnum zipMakerType = concatenate ? ZipTypeEnum.COMBINE_PAGE : ZipTypeEnum.SEPARATE_PAGE;
                
                ZipMaker zipMaker = ZipMakerFactory.newInstance(zipMakerType);
                
                StreamingOutput streamingOutput = new VolumeZipStreamingOutput(volumeRetriever, zipMaker);
                
                response = Response.ok(streamingOutput).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_ZIP).header(Constants.HTTP_HEADER_CONTENT_DISPOSITION, Constants.FILENAME_VOLUMES_ZIP).build();
            } else {
                log.error("Required parameter volumeIDs is null");
                response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Missing required parameter volumeIDs</p>").build();
            }
            
        } catch (ParseException e) {
            log.error("ParseException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Malformed Volume ID List. Offending token: " + e.getMessage() + "</p>").build();
        }
        
        return response;
    }
}


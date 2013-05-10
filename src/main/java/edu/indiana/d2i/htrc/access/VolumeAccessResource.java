/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  VolumeAccessResource.java
# Description: This class handles requests for volume resources
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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.async.ThrottledVolumeRetrieverImpl;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesParserFactory;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesParserFactory.IDTypeEnum;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesParserFactory.Parser;
import edu.indiana.d2i.htrc.access.policy.PolicyCheckerRegistryImpl;
import edu.indiana.d2i.htrc.access.response.VolumeZipStreamingOutput;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory.ZipTypeEnum;
import edu.indiana.d2i.htrc.audit.Auditor;
import edu.indiana.d2i.htrc.audit.AuditorFactory;

/**
 * This class handles requests for volume resources
 * 
 * @author Yiming Sun
 *
 */

@Path("/volumes")
public class VolumeAccessResource {
    
    private static Logger log = Logger.getLogger(VolumeAccessResource.class);

    /**
     * Wrapper method to allow requests made via HTTP Get
     * 
     * @param volumeIDs a single String container raw IDs of requested pages
     * @param concatenate parameter to specify whether the pages of each request volume to be concatenated into a single file or as separate text files.
     * @param retrieveMETS parameter to specify if METS metadata should also be returned.
     * @param version parameter to specify a specific version of data API to use. Just a place holder for now.
     * @param httpHeaders an HttpHeaders object
     * @param httpServletRequest an HttpServletRequest object
     * @return a Response object
     */
    @GET
    public Response getResourceGet(@QueryParam("volumeIDs") String volumeIDs,
                                   @QueryParam("concat") boolean concatenate,
                                   @QueryParam("mets") boolean retrieveMETS,
                                   @QueryParam("version") int version,
                                   @Context HttpHeaders httpHeaders,
                                   @Context HttpServletRequest httpServletRequest) {
        
        return getResourcePost(volumeIDs, concatenate, retrieveMETS, version, httpHeaders, httpServletRequest);
    }
        
    /**
     * Method to handle HTTP Post requests for volume resources
     * 
     * @param volumeIDs a single String containing raw IDs of the requested volumes
     * @param concatenate parameter to specify whether the pages of each request volume to be concatenated into a single file or as spearate text files.
     * @param retrieveMETS parameter to specify if METS metadata should also be returned.
     * @param version parameter to specify a specific version of data API to use. Just a place holder for now.
     * @param httpHeaders an HttpHeaders object
     * @param httpServletRequest an HttpServletRequest object
     * @return a Response object
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response getResourcePost(@FormParam("volumeIDs") String volumeIDs, 
                                @FormParam("concat") boolean concatenate,
                                @FormParam("mets") boolean retrieveMETS,
                                @FormParam("version") int version,
                                @Context HttpHeaders httpHeaders,
                                @Context HttpServletRequest httpServletRequest) {
        
        if (log.isDebugEnabled()) {
            log.debug("volumeIDs = " + volumeIDs);
            log.debug("concatenate = " + concatenate);
            log.debug("mets = " + retrieveMETS);
            log.debug("version = " + version);
        }
                
        
        Response response = null;
        ContextExtractor contextExtractor = new ContextExtractor(httpServletRequest, httpHeaders);
        Auditor auditor = AuditorFactory.getAuditor(contextExtractor.getContextMap());
        
        Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, PolicyCheckerRegistryImpl.getInstance());
        parser.setRetrieveMETS(retrieveMETS);
        
        try {
            if (volumeIDs != null) {
                List<? extends RequestedItemCoordinates> volumeIDList = parser.parse(volumeIDs);
                
                for (RequestedItemCoordinates volumeIdentifier : volumeIDList) {
                    String volumeID = volumeIdentifier.getVolumeID();
                    auditor.audit("REQUESTED", volumeID);
                }
                

                ThrottledVolumeRetrieverImpl volumeRetriever = ThrottledVolumeRetrieverImpl.newInstance(auditor);
                volumeRetriever.setRetrievalIDs(volumeIDList);

                ZipTypeEnum zipMakerType = concatenate ? ZipTypeEnum.COMBINE_PAGE : ZipTypeEnum.SEPARATE_PAGE;
                ZipMaker zipMaker = ZipMakerFactory.newInstance(zipMakerType, auditor);
                StreamingOutput streamingOutput = new VolumeZipStreamingOutput(volumeRetriever, zipMaker, auditor);
                response = Response.ok(streamingOutput).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_ZIP).header(Constants.HTTP_HEADER_CONTENT_DISPOSITION, Constants.FILENAME_VOLUMES_ZIP).build();

            } else {
                log.error("Required parameter volumeIDs is null");
                response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN).entity("Missing required parameter volumeIDs").build();
                auditor.error("Missing Parameter", "Parameter volumeIDs required", "");

            }
            
        } catch (ParseException e) {
            log.error("ParseException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN).entity("Malformed Volume ID list. Offending token: " + e.getMessage()).build();
            auditor.error("ParseException", "Malformed Volume ID List", e.getMessage());
            
        } catch (PolicyViolationException e) {
            log.error("PolicyViolationException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN).entity("Request too greedy. " + e.getMessage()).build();
            auditor.error("PolicyViolationException", "Request Too Greedy", e.getMessage());
        }
        
        return response;
    }
    
}


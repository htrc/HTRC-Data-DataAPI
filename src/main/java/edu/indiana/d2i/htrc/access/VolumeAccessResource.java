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

import edu.indiana.d2i.htrc.access.async.AsyncJob;
import edu.indiana.d2i.htrc.access.async.AsyncJobManager;
import edu.indiana.d2i.htrc.access.async.AsyncVolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.IDTypeEnum;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser;
import edu.indiana.d2i.htrc.access.policy.PolicyCheckerRegistryImpl;
import edu.indiana.d2i.htrc.access.response.VolumeZipStreamingOutput;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory.ZipTypeEnum;
import edu.indiana.d2i.htrc.audit.Auditor;
import edu.indiana.d2i.htrc.audit.AuditorFactory;

/**
 * @author Yiming Sun
 *
 */

@Path("/volumes")
public class VolumeAccessResource {
    
//    @Context ServletContext sc;
    
    private static Logger log = Logger.getLogger(VolumeAccessResource.class);
    
    @GET
    public Response getResourceGet(@QueryParam("volumeIDs") String volumeIDs,
                                   @QueryParam("concat") boolean concatenate,
                                   @QueryParam("version") int version,
                                   @Context HttpHeaders httpHeaders,
                                   @Context HttpServletRequest httpServletRequest) {
        
        return getResourcePost(volumeIDs, concatenate, version, httpHeaders, httpServletRequest);
    }
        
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response getResourcePost(@FormParam("volumeIDs") String volumeIDs, 
                                @FormParam("concat") boolean concatenate,
                                @FormParam("version") int version,
                                @Context HttpHeaders httpHeaders,
                                @Context HttpServletRequest httpServletRequest) {
        
        if (log.isDebugEnabled()) {
            log.debug("volumeIDs = " + volumeIDs);
            log.debug("concatenate = " + concatenate);
            log.debug("version = " + version);
        }
                
        
        Response response = null;
        ContextExtractor contextExtractor = new ContextExtractor(httpServletRequest, httpHeaders);
        Auditor auditor = AuditorFactory.getAuditor(contextExtractor.getContextMap());
        
        Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID, PolicyCheckerRegistryImpl.getInstance());
        
        try {
            if (volumeIDs != null) {
                List<? extends HTRCItemIdentifier> volumeIDList = parser.parse(volumeIDs);

                AsyncVolumeRetriever asyncVolumeRetriever = new AsyncVolumeRetriever();

                for (HTRCItemIdentifier volumeIdentifier : volumeIDList) {
                    String volumeID = volumeIdentifier.getVolumeID();
                    auditor.audit("REQUESTED", volumeID);
                    asyncVolumeRetriever.addOutstandingVolumeID(volumeID);
                }

                AsyncJobManager asyncJobManager = AsyncJobManager.getInstance();

                ZipTypeEnum zipMakerType = concatenate ? ZipTypeEnum.COMBINE_PAGE : ZipTypeEnum.SEPARATE_PAGE;
                ZipMaker zipMaker = ZipMakerFactory.newInstance(zipMakerType, auditor);
                StreamingOutput streamingOutput = new VolumeZipStreamingOutput(asyncVolumeRetriever, zipMaker, auditor);
                response = Response.ok(streamingOutput).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_ZIP).header(Constants.HTTP_HEADER_CONTENT_DISPOSITION, Constants.FILENAME_VOLUMES_ZIP).build();


                // add the jobs to the queue as the last step to ensure all other data objects are
                // properly created before any job is finished
                for (HTRCItemIdentifier volumeIdentifier : volumeIDList) {
                    AsyncJob asyncJob = new AsyncJob(volumeIdentifier, asyncVolumeRetriever);
                    asyncJobManager.addJob(asyncJob);
                }
                
            } else {
                log.error("Required parameter volumeIDs is null");
                response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Missing required parameter volumeIDs</p>").build();
                auditor.error("Missing Parameter", "Parameter volumeIDs required", "");

            }
            
        } catch (ParseException e) {
            log.error("ParseException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Malformed Volume ID list. Offending token: " + e.getMessage() + "</p>").build();
            auditor.error("ParseException", "Malformed Volume ID List", e.getMessage());
            
        } catch (PolicyViolationException e) {
            log.error("PolicyViolationException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Request too greedy. " + e.getMessage() + "</p>").build();
            auditor.error("PolicyViolationException", "Request Too Greedy", e.getMessage());
        }
        
        return response;
    }
    
}


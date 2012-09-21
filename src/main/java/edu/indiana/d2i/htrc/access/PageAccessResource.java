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
# File:  PageAccessResource.java
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
@Path("/pages")
public class PageAccessResource {
    
    private static Logger log = Logger.getLogger(PageAccessResource.class);
    
    @GET
    public Response getResourceGet(@QueryParam("pageIDs") String pageIDs,
                                   @QueryParam("concat") boolean concatenate,
                                   @QueryParam("version") int version,
                                   @Context HttpHeaders httpHeaders,
                                   @Context HttpServletRequest httpServletRequest) {
        return getResourcePost(pageIDs, concatenate, version, httpHeaders, httpServletRequest);
    }
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response getResourcePost(@FormParam("pageIDs") String pageIDs,
                                    @FormParam("concat") boolean concatenate,
                                    @FormParam("version") int version,
                                    @Context HttpHeaders httpHeaders,
                                    @Context HttpServletRequest httpServletRequest) {
        
        
        if (log.isDebugEnabled()) {
            log.debug("pageIDs = " + pageIDs);
            log.debug("concatenate = " + concatenate);
            log.debug("version = " + version);
        }
        
        Response response = null;
        ContextExtractor contextExtractor = new ContextExtractor(httpServletRequest, httpHeaders);
        Auditor auditor = AuditorFactory.getAuditor(contextExtractor.getContextMap());
        
        Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID, PolicyCheckerRegistryImpl.getInstance());
        
        try {
            
            if (pageIDs != null) {
                List<? extends HTRCItemIdentifier> pageIDList = parser.parse(pageIDs);
                
                AsyncJobManager asyncJobManager = AsyncJobManager.getInstance();
                
                AsyncVolumeRetriever asyncVolumeRetriever = AsyncVolumeRetriever.newInstance(asyncJobManager);
                
                for (HTRCItemIdentifier pageIdentifier : pageIDList) {
                    String volumeID = pageIdentifier.getVolumeID();
                    auditor.audit("REQUESTED", volumeID, pageIdentifier.getPageSequences().toArray(new String[0]));
                }
                
                asyncVolumeRetriever.setRetrievalIDs(pageIDList);

                ZipTypeEnum zipMakerType = concatenate ? ZipTypeEnum.WORD_SEQUENCE : ZipTypeEnum.SEPARATE_PAGE;
                ZipMaker zipMaker = ZipMakerFactory.newInstance(zipMakerType, auditor);
                StreamingOutput streamingOutput = new VolumeZipStreamingOutput(asyncVolumeRetriever, zipMaker, auditor);
                response = Response.ok(streamingOutput).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_ZIP).header(Constants.HTTP_HEADER_CONTENT_DISPOSITION, Constants.FILENAME_PAGES_ZIP).build();

                // adding jobs to the queue as the last step to ensure all other
                // objects are properly created
                asyncVolumeRetriever.submitJobs();
            
            } else {
                log.error("Required parameter pageIDs is null");
                response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Missing required parameter pageIDs</p>").build();
                auditor.error("Missing Parameter", "Parameter pageIDs required", "");
            }
        } catch (ParseException e) {
            log.error("ParseException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Malformed Page ID list. Offending token: " + e.getMessage() + "</p>").build();
            auditor.error("ParseException", "Malformed Page ID List", e.getMessage());
        } catch (PolicyViolationException e) {
            log.error("PolicyViolationException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Request too greedy. " + e.getMessage() + "</p>").build();
            auditor.error("PolicyViolationException", "Request Too Greedy", e.getMessage());
        }
        
        
        return response;
    }

}


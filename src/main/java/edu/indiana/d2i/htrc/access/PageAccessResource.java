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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.audit.AuditorFactory;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.IDTypeEnum;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser;
import edu.indiana.d2i.htrc.access.policy.PolicyCheckerRegistryImpl;
import edu.indiana.d2i.htrc.access.read.HectorVolumeRetriever;
import edu.indiana.d2i.htrc.access.response.VolumeZipStreamingOutput;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory.ZipTypeEnum;

/**
 * @author Yiming Sun
 *
 */
@Path("/pages")
public class PageAccessResource {
    
    private static Logger log = Logger.getLogger(PageAccessResource.class);
    
    @GET
    public Response getResource(@QueryParam("pageIDs") String pageIDs,
                                @QueryParam("concat") boolean concatenate,
                                @QueryParam("version") int version,
                                @Context HttpHeaders httpHeaders,
                                @Context HttpServletRequest httpServletRequest) {
        
        
        if (log.isDebugEnabled()) {
            log.debug("pageIDs = " + pageIDs);
            log.debug("concatenate = " + concatenate);
            log.debug("version = " + version);
        }
        
        Response response = null;
        ContextExtractor contextExtractor = new ContextExtractor(httpServletRequest, httpHeaders);
        Auditor auditor = AuditorFactory.getAuditor(contextExtractor);
        
        Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID, PolicyCheckerRegistryImpl.getInstance());
        
        try {
            
            if (pageIDs != null) {
                List<? extends HTRCItemIdentifier> pageIDList = parser.parse(pageIDs);
                
                for (HTRCItemIdentifier pageIdentifier : pageIDList) {
                    auditor.audit("REQUESTED", pageIdentifier.getVolumeID(), pageIdentifier.getPageSequences().toArray(new String[0]));
                }
                
                VolumeRetriever volumeRetriever = new HectorVolumeRetriever(pageIDList, HectorResourceSingleton.getInstance(), ParameterContainerSingleton.getInstance(), PolicyCheckerRegistryImpl.getInstance());
                ZipTypeEnum zipMakerType = concatenate ? ZipTypeEnum.WORD_BAG : ZipTypeEnum.SEPARATE_PAGE;
                
                ZipMaker zipMaker = ZipMakerFactory.newInstance(zipMakerType, auditor);
                
                StreamingOutput streamingOutput = new VolumeZipStreamingOutput(volumeRetriever, zipMaker, auditor);
                
                response = Response.ok(streamingOutput).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_ZIP).header(Constants.HTTP_HEADER_CONTENT_DISPOSITION, Constants.FILENAME_PAGES_ZIP).build();
            } else {
                log.error("Required parameter pageIDs is null");
                response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Missing required parameter pageIDs</p>").build();
                auditor.error("Missing Parameter", "Parameter pageIDs required", "");
            }
        } catch (ParseException e) {
            log.error("ParseException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Malformed Page ID List. Offending token: " + e.getMessage() + "</p>").build();
            auditor.error("ParseException", "Malformed Page ID List", e.getMessage());
        } catch (PolicyViolationException e) {
            log.error("PolicyViolationException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Request too greedy. " + e.getMessage() + "</p>").build();
            auditor.error("PolicyViolation", "Request too greedy", e.getMessage());
        }
        
        
        return response;
    }

}


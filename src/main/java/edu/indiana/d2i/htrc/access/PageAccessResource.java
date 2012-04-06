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
import java.util.Map;

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

import edu.indiana.d2i.htrc.access.exception.DataAPIException;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.IDTypeEnum;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser;
import edu.indiana.d2i.htrc.access.policy.PolicyCheckerRegistryImpl;
import edu.indiana.d2i.htrc.access.read.HectorResource;
import edu.indiana.d2i.htrc.access.read.HectorVolumeRetriever;
import edu.indiana.d2i.htrc.access.response.VolumeZipStreamingOutput;
import edu.indiana.d2i.htrc.access.validity.PageValidityChecker;
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
        Auditor auditor = AuditorFactory.getAuditor(contextExtractor.getContextMap());
        
        Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID, PolicyCheckerRegistryImpl.getInstance());
        
        try {
            
            if (pageIDs != null) {
                List<? extends HTRCItemIdentifier> pageIDList = parser.parse(pageIDs);
                
                for (HTRCItemIdentifier pageIdentifier : pageIDList) {
                    auditor.audit("REQUESTED", pageIdentifier.getVolumeID(), pageIdentifier.getPageSequences().toArray(new String[0]));
                }
                
                RequestValidityChecker validityChecker = new PageValidityChecker(HectorResource.getSingletonInstance(), ParameterContainerSingleton.getInstance(), PolicyCheckerRegistryImpl.getInstance());
                Map<String, ? extends VolumeInfo> volumeInfoMap = validityChecker.validateRequest(pageIDList);
                
                
                VolumeRetriever volumeRetriever = new HectorVolumeRetriever(pageIDList, HectorResource.getSingletonInstance(), volumeInfoMap);
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
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Malformed Page ID list. Offending token: " + e.getMessage() + "</p>").build();
            auditor.error("ParseException", "Malformed Page ID List", e.getMessage());
        } catch (PolicyViolationException e) {
            log.error("PolicyViolationException", e);
            response = Response.status(Status.BAD_REQUEST).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Request too greedy. " + e.getMessage() + "</p>").build();
            auditor.error("PolicyViolationException", "Request Too Greedy", e.getMessage());
        } catch (KeyNotFoundException e) {
            log.error("KeyNotFoundException", e);
            response = Response.status(Status.NOT_FOUND).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Key not found. " + e.getMessage() + "</p>").build();
            auditor.error("KeyNotFoundException", "Key Not Found", e.getMessage());
//        } catch (HTimedOutException e) {
//            log.error("HTimedOutException", e);
//            response = Response.status(Status.INTERNAL_SERVER_ERROR).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Server too busy.</p>").build();
//            auditor.error("HTimedOutException", "Cassandra Timed Out", e.getMessage());
        } catch (RepositoryException e) {
            log.error("RepositoryException", e);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Server too busy.</p>").build();
            auditor.error("RepositoryException", "Cassandra Timed Out", e.getMessage());
        } catch (DataAPIException e) {
            log.error("DataAPIException", e);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Internal server error.</p>").build();
            auditor.error("DataAPIException", "Unspecified Error", e.getMessage());
        }
        
        
        return response;
    }

}


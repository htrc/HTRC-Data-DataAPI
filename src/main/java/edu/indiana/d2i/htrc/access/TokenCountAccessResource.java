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
# File:  TokenCountAccessResource.java
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
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import edu.indiana.d2i.htrc.access.response.TokenCountZipStreamingOutput;
import edu.indiana.d2i.htrc.access.tokencount.Count;
import edu.indiana.d2i.htrc.access.tokencount.EmptyTokenRemovalFilter;
import edu.indiana.d2i.htrc.access.tokencount.SimpleTokenFilterChain;
import edu.indiana.d2i.htrc.access.tokencount.SimpleTokenizer;
import edu.indiana.d2i.htrc.access.tokencount.TokenCountComparatorFactory;
import edu.indiana.d2i.htrc.access.tokencount.TokenCountComparatorFactory.TokenCountComparatorTypeEnum;
import edu.indiana.d2i.htrc.access.tokencount.TokenCountZipper;
import edu.indiana.d2i.htrc.access.tokencount.TokenCountZipperFactory;
import edu.indiana.d2i.htrc.access.tokencount.TokenCountZipperFactory.TokenCountZipTypeEnum;
import edu.indiana.d2i.htrc.access.tokencount.TokenFilterChain;
import edu.indiana.d2i.htrc.access.tokencount.Tokenizer;
import edu.indiana.d2i.htrc.audit.Auditor;
import edu.indiana.d2i.htrc.audit.AuditorFactory;

/**
 * @author Yiming Sun
 *
 */
@Path("/tokencount")
public class TokenCountAccessResource {
    private static final Logger log = Logger.getLogger(TokenCountAccessResource.class);
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response getResourcePost(@FormParam("volumeIDs") String volumeIDs,
                               @FormParam("level") String countLevel,
                               @FormParam("sortBy") String sortBy,
                               @FormParam("sortOrder") String sortOrder,
                               @FormParam("version") int version,
                               @Context HttpHeaders httpHeaders,
                               @Context HttpServletRequest httpServletRequest) {
        
        Response response = null;
        ContextExtractor contextExtractor = new ContextExtractor(httpServletRequest, httpHeaders);
        Auditor auditor = AuditorFactory.getAuditor(contextExtractor.getContextMap());
        
        Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, PolicyCheckerRegistryImpl.getInstance());
        parser.setRetrieveMETS(false); // should never use METS for token count, so it is good to set it explicitly to false here
        
        try {
            if (volumeIDs != null) {
                List<? extends RequestedItemCoordinates> volumeIDList = parser.parse(volumeIDs);
                
                for (RequestedItemCoordinates itemCoordinates : volumeIDList) {
                    String volumeID = itemCoordinates.getVolumeID();
                    auditor.audit("REQUESTED", volumeID);
                }
            
                ThrottledVolumeRetrieverImpl volumeRetriever = ThrottledVolumeRetrieverImpl.newInstance(auditor);
                volumeRetriever.setRetrievalIDs(volumeIDList);
                
                TokenCountZipper tokenCountZipper = null;
                Comparator<Entry<String, Count>> comparator = null;
                Tokenizer tokenizer = new SimpleTokenizer(SystemResourcesContainerSingleton.getInstance().getTokenCountExecutorService(), ParameterContainerSingleton.getInstance());
                
                if (countLevel != null && "page".equalsIgnoreCase(countLevel)) {
                    tokenCountZipper = TokenCountZipperFactory.newInstance(TokenCountZipTypeEnum.PAGE_LEVEL, auditor);
                } else {
                    tokenCountZipper = TokenCountZipperFactory.newInstance(TokenCountZipTypeEnum.VOLUME_LEVEL, auditor);
                }
                
                if ("token".equalsIgnoreCase(sortBy)) {
                    if (sortOrder != null && sortOrder.toLowerCase().startsWith("desc")) {
                        comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_LEX_DESC);
                    } else {
                        comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_LEX_ASC);
                    }
                } else if ("count".equalsIgnoreCase(sortBy)) {
                    if (sortOrder != null && sortOrder.toLowerCase().startsWith("desc")) {
                        comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_COUNT_DESC);
                    } else {
                        comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_COUNT_ASC);
                    }
                } else {
                    comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.DEFAULT);
                }
                
                TokenFilterChain tokenFilterChain = new SimpleTokenFilterChain();
                tokenFilterChain.addFilter(new EmptyTokenRemovalFilter());
                
                StreamingOutput streamingOutput = new TokenCountZipStreamingOutput(volumeRetriever, tokenCountZipper, tokenizer, tokenFilterChain, comparator);
                response = Response.ok(streamingOutput).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_ZIP).header(Constants.HTTP_HEADER_CONTENT_DISPOSITION, Constants.FILENAME_TOKENCOUNT_ZIP).build();
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


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
# File:  SeparatePageVolumeZipStreamingOutput.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.response;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.Constants;
import edu.indiana.d2i.htrc.access.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.ZipMaker;

/**
 * @author Yiming Sun
 *
 */
public class VolumeZipStreamingOutput implements StreamingOutput {

    private static Logger log = Logger.getLogger(VolumeZipStreamingOutput.class);
    
    private VolumeRetriever volumeRetriever = null;
    private ZipMaker zipMaker = null;
    
    public VolumeZipStreamingOutput(VolumeRetriever volumeRetriever, ZipMaker zipMaker) {
        this.volumeRetriever = volumeRetriever;
        this.zipMaker = zipMaker;
    }
    
    
    /**
     * @see javax.ws.rs.core.StreamingOutput#write(java.io.OutputStream)
     */
    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        try {
            zipMaker.makeZipFile(output, volumeRetriever);
        } catch (KeyNotFoundException e) {
            log.error("KeyNotFoundException", e);
            Response response = Response.status(Status.NOT_FOUND).header(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML).entity("<p>Key Not Found. " + e.getMessage() + "</p>").build();
            WebApplicationException exception = new WebApplicationException(response);
            
            throw exception;
        }
    }

}


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
# File:  VolumeZipStreamingOutput.java
# Description:  This class is an implementation of the StreamOutput class that can output the data as a zip output stream using the given ZipMaker object
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
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.ZipMaker;
import edu.indiana.d2i.htrc.access.exception.DataAPIException;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.audit.Auditor;

/**
 * This class is an implementation of the StreamOutput class that can output the data as a zip output stream using the given ZipMaker object
 * 
 * @author Yiming Sun
 *
 */
public class VolumeZipStreamingOutput implements StreamingOutput {

    private static Logger log = Logger.getLogger(VolumeZipStreamingOutput.class);
    
    private VolumeRetriever volumeRetriever = null;
    private ZipMaker zipMaker = null;
    private Auditor auditor = null;
   
    /**
     * Constructor
     * @param volumeRetriever a VolumeRetriever object holding the volume content to be output to the stream
     * @param zipMaker a ZipMaker object
     * @param auditor an Auditor object
     */
    public VolumeZipStreamingOutput(VolumeRetriever volumeRetriever, ZipMaker zipMaker, Auditor auditor) {
        this.volumeRetriever = volumeRetriever;
        this.zipMaker = zipMaker;
        this.auditor = auditor;
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
            auditor.error("KeyNotFoundException", "Key Not Found", e.getMessage());
        } catch (PolicyViolationException e) {
            log.error("PolicyViolationException", e);
            auditor.error("PolicyViolationException", "Request Too Greedy", e.getMessage());
        } catch (RepositoryException e) {
            log.error("RepositoryException", e);
            auditor.error("RepositoryException", "Cassandra Timed Out", e.getMessage());
        } catch (DataAPIException e) {
            log.error("DataAPIException", e);
            auditor.error("DataAPIException", "Unspecified Error", e.getMessage());
        }
    }

}


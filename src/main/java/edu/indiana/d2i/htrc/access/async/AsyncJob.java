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
# Project: data-api-async-experimental
# File:  AsyncJob.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

import java.util.List;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.exception.DataAPIException;

/**
 * @author Yiming Sun
 *
 */
public class AsyncJob implements Callback {
    
    private static Logger log = Logger.getLogger(AsyncJob.class);

    protected final HTRCItemIdentifier identifier;
    protected final AsyncVolumeRetriever asyncVolumeRetriever;
    
    public AsyncJob(HTRCItemIdentifier identifier, AsyncVolumeRetriever asyncVolumeRetriever) {
        this.identifier = identifier;
        this.asyncVolumeRetriever = asyncVolumeRetriever;
    }
    
    public HTRCItemIdentifier getIdentifier() {
        return this.identifier;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.async.Callback#finished(edu.indiana.d2i.htrc.access.async.ContentHolder)
     */
    @Override
    public void finished(List<ExceptionAwareVolumeReader> exceptionAwareVolumeReaders) {
        String volumeID = identifier.getVolumeID();
        if (log.isDebugEnabled()) log.debug("Job finished for " + volumeID);
        asyncVolumeRetriever.addResult(volumeID, exceptionAwareVolumeReaders);
    }

    /**
     * @see edu.indiana.d2i.htrc.access.async.Callback#failed(edu.indiana.d2i.htrc.access.async.ContentHolder, edu.indiana.d2i.htrc.access.HTRCItemIdentifier, java.lang.Exception)
     */
    @Override
    public void failed(List<ExceptionAwareVolumeReader> exceptionAwareVolumeReaders, DataAPIException dataAPIException) {
        String volumeID = identifier.getVolumeID();
        if (log.isDebugEnabled()) log.debug("Job failed for " + volumeID);
        asyncVolumeRetriever.addResult(volumeID, exceptionAwareVolumeReaders);
    }

    /**
     * @see edu.indiana.d2i.htrc.access.async.Callback#started(int)
     */
    @Override
    public void updateJobCount(int entryCount) {
        asyncVolumeRetriever.updateOutstandingJobCount(entryCount);
    }

}


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
# File:  AsyncWorker.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.Constants;
import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.VolumeInfo;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.read.HectorResource;

/**
 * @author Yiming Sun
 *
 */
public class AsyncWorker implements Runnable {
    
    private static Logger log = Logger.getLogger(AsyncWorker.class);

    protected final AsyncJobManager asyncJobManager;
    protected final HectorResource hectorResource;
    protected volatile boolean done;
    protected String id;
    
    AsyncWorker(AsyncJobManager asyncJobManager, HectorResource hectorResource, String id) {
        this.asyncJobManager = asyncJobManager;
        this.hectorResource = hectorResource;
        this.done = false;
        this.id = id;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (!done) {
            AsyncJob job = asyncJobManager.getJob();
            if (job != null) {
                processJob(job);
            }
        }
        log.info("AsyncWorker " + id + " terminated");
    }

    void shutdown() {
        done = true;
    }
    
    protected void processJob(AsyncJob asyncJob) {
        HTRCItemIdentifier identifier = asyncJob.getIdentifier();
        String volumeID = identifier.getVolumeID();
        ExceptionAwareVolumeReaderImpl exceptionAwareVolumeReaderImpl = new ExceptionAwareVolumeReaderImpl(identifier);
        try {
            VolumeInfo volumeInfo = hectorResource.getVolumeInfo(volumeID);
            List<String> pageSequences = identifier.getPageSequences();
            if (pageSequences == null) {
                pageSequences = generatePageSequenceList(volumeInfo.getPageCount());
            } else {
                checkPageSequenceRange(volumeID, pageSequences, volumeInfo.getPageCount());
            }
            
            List<PageReader> pageReaderList = hectorResource.retrievePageContents(volumeID, pageSequences);
            exceptionAwareVolumeReaderImpl.setPages(pageReaderList);
            
            asyncJob.finished(exceptionAwareVolumeReaderImpl);
            
        } catch (RepositoryException re) {
            exceptionAwareVolumeReaderImpl.setRepositoryException(re);
            asyncJob.failed(exceptionAwareVolumeReaderImpl, re);
        } catch (KeyNotFoundException knfe) {
            exceptionAwareVolumeReaderImpl.setKeyNotFoundException(knfe);
            asyncJob.failed(exceptionAwareVolumeReaderImpl, knfe);
        }
        
    }
    
    
    private List<String> generatePageSequenceList(int pageCount) {
        List<String> pageSequences = new ArrayList<String>();
        for (int i = 1; i < pageCount; i++) {
            String pageSequence = HTRCItemIdentifierFactory.Parser.generatePageSequenceString(i);
            pageSequences.add(pageSequence);
        }
        return Collections.<String>unmodifiableList(pageSequences);
    }
    
    private void checkPageSequenceRange(String volumeID, List<String> pageSequences, int maxPageSequence) throws KeyNotFoundException {
        for (String pageSequence : pageSequences) {
            int value = Integer.parseInt(pageSequence);
            if (value > maxPageSequence) {
                throw new KeyNotFoundException(volumeID + Constants.PAGE_SEQ_START_MARK + pageSequence + Constants.PAGE_SEQ_END_MARK);
            }
        }
    }
}


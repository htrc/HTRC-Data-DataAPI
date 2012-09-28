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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.Constants;
import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.VolumeInfo;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.async.ExceptionAwareVolumeReader.ExceptionType;
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
    protected final String id;
    protected final int maxPagesPerResult;
    protected final int maxAsyncFetchEntryCount;
    
    AsyncWorker(AsyncJobManager asyncJobManager, HectorResource hectorResource, String id, int maxPagesPerResult, int maxAsyncFetchEntryCount) {
        this.asyncJobManager = asyncJobManager;
        this.hectorResource = hectorResource;
        this.done = false;
        this.id = id;
        this.maxPagesPerResult = maxPagesPerResult;
        this.maxAsyncFetchEntryCount = maxAsyncFetchEntryCount;
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
    
    private int dividePageSequencesIntoBatches(List<String> pageSequences, List<List<String>> pageRetrievalBatches) {
        int resultCount = 0;
        
        // determine in how many batches should the pages be retrieved
        int pageSequencesCount = pageSequences.size();
        resultCount = pageSequencesCount / maxPagesPerResult;
        int remainder = pageSequencesCount % maxPagesPerResult;
        
        // if the total number of pages is not divisible by the batch size
        // there will be one more batch
        if (remainder != 0) {
            resultCount++;
        }

        Iterator<String> pageSequencesIterator = pageSequences.iterator();
        
//        List<List<String>> pageRetrievalBatches = new LinkedList<List<String>>();
        
        
        int alignedBatchCount = (remainder != 0) ? (resultCount - 1) : resultCount;
        
        for (int i = 0; i < alignedBatchCount; i++) {
            List<String> retrievalBatch = new LinkedList<String>();
            pageRetrievalBatches.add(retrievalBatch);
            
            for (int j = 0; j < maxPagesPerResult; j++) {
                retrievalBatch.add(pageSequencesIterator.next());
            }
        }
        
        // last batch if remainder is not 0
        if (remainder != 0) {
            List<String> lastBatch = new LinkedList<String>();
            pageRetrievalBatches.add(lastBatch);
            for (int i = 0; i < remainder; i++) {
                lastBatch.add(pageSequencesIterator.next());
            }
        }

        return resultCount;
    }
    
    
    protected void processJob(AsyncJob asyncJob) {
        HTRCItemIdentifier identifier = asyncJob.getIdentifier();
        String volumeID = identifier.getVolumeID();
        LinkedList<ExceptionAwareVolumeReader> exceptionAwareVolumeReaderImplList = new LinkedList<ExceptionAwareVolumeReader>(); 
//        ExceptionAwareVolumeReaderImpl exceptionAwareVolumeReaderImpl = new ExceptionAwareVolumeReaderImpl(identifier);
//        exceptionAwareVolumeReaderImplList.add(exceptionAwareVolumeReaderImpl);
        int resultCount = 0;
//        ExceptionAwareVolumeReaderImpl exceptionAwareVolumeReaderImpl = new ExceptionAwareVolumeReaderImpl(identifier);
        try {
            
            VolumeInfo volumeInfo = hectorResource.getVolumeInfo(volumeID);
            List<String> pageSequences = identifier.getPageSequences();
            if (pageSequences == null) {
                pageSequences = generatePageSequenceList(volumeInfo.getPageCount());
            } else {
                checkPageSequenceRange(volumeID, pageSequences, volumeInfo.getPageCount());
            }
            
            List<List<String>> pageRetrievalBatches = new LinkedList<List<String>>();
            
            resultCount = dividePageSequencesIntoBatches(pageSequences, pageRetrievalBatches);
            if (log.isDebugEnabled()) log.debug("Number of entries for " + volumeID + ": " + resultCount);
            
            asyncJob.updateJobCount(resultCount);

            int asyncFetchCount = (resultCount > maxAsyncFetchEntryCount) ? maxAsyncFetchEntryCount : resultCount;
            if (log.isDebugEnabled()) log.debug("Number of async fetch entries for " + volumeID + ": " + asyncFetchCount);
            
            // fetch up to maxAsyncFetchEntryCount batches in this AsyncWorker
            for (int i = 0; i < asyncFetchCount; i++) {
                List<String> batch = pageRetrievalBatches.remove(0);

                ExceptionAwareVolumeReaderImpl exceptionAwareVolumeReaderImpl = new ExceptionAwareVolumeReaderImpl(identifier);

                try {
                    List<PageReader> pageReaderList = hectorResource.retrievePageContents(volumeID, batch);
                    exceptionAwareVolumeReaderImpl.setPages(pageReaderList);
                } catch (RepositoryException re) {
                    exceptionAwareVolumeReaderImpl.addException(re, ExceptionType.EXCEPTION_REPOSITORY);
                    asyncJob.updateJobCount(-resultCount);
                    asyncJob.failed(exceptionAwareVolumeReaderImplList, re);
                } catch (KeyNotFoundException knfe) {
                    exceptionAwareVolumeReaderImpl.addException(knfe, ExceptionType.EXCEPTION_KEY_NOT_FOUND);
                    asyncJob.updateJobCount(-resultCount);
                    asyncJob.failed(exceptionAwareVolumeReaderImplList, knfe);
                }

                exceptionAwareVolumeReaderImplList.add(exceptionAwareVolumeReaderImpl);

            }
            
            // the remaining page batches will not be fetched here, but done on-demand
            int syncFetchCount = resultCount - asyncFetchCount;
            for (int i = 0; i < syncFetchCount; i++) {
                List<String> unfetchedBatch = pageRetrievalBatches.remove(0);
                SynchronousFetchVolumeReaderImpl syncFetchVolumeReaderImpl = new SynchronousFetchVolumeReaderImpl(identifier, unfetchedBatch, hectorResource);
                exceptionAwareVolumeReaderImplList.add(syncFetchVolumeReaderImpl);
            }
            if (log.isDebugEnabled()) log.debug("Number of sync fetch entries added for " + volumeID + ": " + syncFetchCount);
            
            asyncJob.updateJobCount(-resultCount);
            asyncJob.finished(exceptionAwareVolumeReaderImplList);
            
        } catch (RepositoryException re) {
            ExceptionAwareVolumeReaderImpl exceptionAwareVolumeReaderImpl = new ExceptionAwareVolumeReaderImpl(identifier);
            exceptionAwareVolumeReaderImpl.addException(re, ExceptionType.EXCEPTION_REPOSITORY);
            exceptionAwareVolumeReaderImplList.add(exceptionAwareVolumeReaderImpl);
            asyncJob.updateJobCount(-resultCount);
            asyncJob.failed(exceptionAwareVolumeReaderImplList, re);
            
        } catch (KeyNotFoundException knfe) {
            ExceptionAwareVolumeReaderImpl exceptionAwareVolumeReaderImpl = new ExceptionAwareVolumeReaderImpl(identifier);
            exceptionAwareVolumeReaderImpl.addException(knfe, ExceptionType.EXCEPTION_KEY_NOT_FOUND);
            exceptionAwareVolumeReaderImplList.add(exceptionAwareVolumeReaderImpl);
            asyncJob.updateJobCount(-resultCount);
            asyncJob.failed(exceptionAwareVolumeReaderImplList, knfe);
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


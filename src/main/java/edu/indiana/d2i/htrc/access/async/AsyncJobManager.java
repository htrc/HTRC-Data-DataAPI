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
# File:  AsyncJobManager.java
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
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.exception.DataAPIException;
import edu.indiana.d2i.htrc.access.read.HectorResource;

/**
 * @author Yiming Sun
 *
 */
public class AsyncJobManager {
    
    protected static class TerminationJob extends AsyncJob {

        /**
         * @param identifier
         * @param asyncVolumeRetriever
         */
        public TerminationJob() {
            super(null, null);
        }
        
        public HTRCItemIdentifier getIdentifier() {
            return null;
        }
        
        public void finished(ExceptionAwareVolumeReader exceptionAwareVolumeReader) {
            
        }
        
        public void failed(ExceptionAwareVolumeReader exceptionAwareVolumeReader, DataAPIException dataAPIException) {
            
        }
    }
    
    private static Logger log = Logger.getLogger(AsyncJobManager.class);
    
    public static final String PN_ASYNC_WORKER_COUNT = "async.worker.count";

    protected static AsyncJobManager instance;
    protected static volatile boolean initialized = false;

    protected final ParameterContainer parameterContainer;
    protected final BlockingQueue<AsyncJob> jobQueue;
    protected final List<AsyncWorker> workerList;
    protected final List<Thread> workerThreadList;
    
    public static synchronized AsyncJobManager getInstance() {
        AsyncJobManager asyncJobManager = null;
        if (initialized) {
            asyncJobManager = instance;
        }
        return asyncJobManager;
    }
    
    public static void init(ParameterContainer parameterContainer, HectorResource hectorResource) {
        if (!initialized) {
            instance = new AsyncJobManager(parameterContainer, hectorResource);
            initialized = true;
        }
    }

    protected AsyncJobManager(ParameterContainer parameterContainer, HectorResource hectorResource) {
        this.parameterContainer = parameterContainer;
        int workerCount = Integer.parseInt(parameterContainer.getParameter(PN_ASYNC_WORKER_COUNT));
        this.jobQueue = new LinkedBlockingQueue<AsyncJob>();
        workerList = new ArrayList<AsyncWorker>(workerCount);
        workerThreadList = new ArrayList<Thread>(workerCount);
        for (int i = 0; i < workerCount; i++) {
            AsyncWorker worker = new AsyncWorker(this.jobQueue, hectorResource, "AsyncWorker-" + i);
            Thread thread = new Thread(worker, "AsyncWorker-" + i);
            thread.start();
            workerList.add(worker);
            workerThreadList.add(thread);
            if (log.isDebugEnabled()) log.debug(thread.getName() + " thread started");
        }
        
    }
    public void shutdown() {
        if (log.isDebugEnabled()) log.debug("shutdown received");
        TerminationJob terminationJob = new TerminationJob();
        for (AsyncWorker worker : workerList) {
           worker.shutdown();
           jobQueue.add(terminationJob);
        }
    }
    
    public void addJob(AsyncJob asyncJob) {
        jobQueue.add(asyncJob);
    }
}


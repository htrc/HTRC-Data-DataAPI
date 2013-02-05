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
# File:  AsyncFetchManager.java
# Description:  This singleton class manages the asynchronous fetching of data from Cassandra
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.read.HectorResource;

/**
 * This singleton class manages the asynchronous fetching of data from Cassandra
 * 
 * @author Yiming Sun
 *
 */
public class AsyncFetchManager {
    
    public static final String PN_ASYNC_WORKER_COUNT = "async.worker.count";
    protected static int POOL_SIZE = 1;
    protected static HectorResource hectorResource = null;
    protected final ExecutorService executorService;
    protected static AsyncFetchManager instance = null;
    
    /**
     * Method to initialize the singleton class
     * 
     * @param parameterContainer an initialized ParameterContainer object
     * @param hectorResource an initialized HectorResource object
     */
    public static void init(ParameterContainer parameterContainer, HectorResource hectorResource) {
        AsyncFetchManager.hectorResource = hectorResource;
        POOL_SIZE = Integer.parseInt(parameterContainer.getParameter(PN_ASYNC_WORKER_COUNT));
    }
    
    /**
     * Method to return the singleton instance object of this class
     * @return the singleton instance object of this class
     */
    public static synchronized AsyncFetchManager getInstance() {
        if (instance == null) {
            instance = new AsyncFetchManager();
        }
        return instance;
    }
    
    /**
     * Constructor. Used internally for the singleton instantiation
     */
    protected AsyncFetchManager() {
        this.executorService = Executors.newFixedThreadPool(POOL_SIZE);
        
    }
    
    /**
     * Method to submit an HTRCItemIdentifier for async fetch
     * @param itemIdentifier an HTRCItemIdentifier to be fetched asynchronously
     * @return a Future of VolumeReader object
     */
    public Future<VolumeReader> submit(HTRCItemIdentifier itemIdentifier) {
        CallableVolumeFetcher callableVolumeFetcher = new CallableVolumeFetcher(itemIdentifier, hectorResource);
        Future<VolumeReader> future = executorService.submit(callableVolumeFetcher);
        return future;
    }
    
    /**
     * Method to dispose of resources such as the ExecutorService object
     */
    public void shutdown() {
        this.executorService.shutdown();
    }

}


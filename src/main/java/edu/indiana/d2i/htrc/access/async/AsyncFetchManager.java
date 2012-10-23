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
# Project: data-api
# File:  AsyncFetchManager.java
# Description:  
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

import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.id.VolumePageIdentifier;
import edu.indiana.d2i.htrc.access.read.HectorResource;

/**
 * @author Yiming Sun
 *
 */
public class AsyncFetchManager {
    
    public static final String PN_ASYNC_WORKER_COUNT = "async.worker.count";
    protected static int POOL_SIZE = 1;
    protected static HectorResource hectorResource = null;
    protected final ExecutorService executorService;
    protected static AsyncFetchManager instance = null;
    
    
    public static void init(ParameterContainer parameterContainer, HectorResource hectorResource) {
        AsyncFetchManager.hectorResource = hectorResource;
        POOL_SIZE = Integer.parseInt(parameterContainer.getParameter(PN_ASYNC_WORKER_COUNT));
    }
    
    public static synchronized AsyncFetchManager getInstance() {
        if (instance == null) {
            instance = new AsyncFetchManager();
        }
        return instance;
    }
    
    protected AsyncFetchManager() {
        this.executorService = Executors.newFixedThreadPool(POOL_SIZE);
        
    }
    
    public Future<VolumeReader> submit(VolumePageIdentifier volumePageIdentifier) {
        CallableVolumeFetcher callableVolumeFetcher = new CallableVolumeFetcher(volumePageIdentifier, hectorResource);
        Future<VolumeReader> future = executorService.submit(callableVolumeFetcher);
        return future;
    }
    
    public void shutdown() {
        this.executorService.shutdown();
    }

}


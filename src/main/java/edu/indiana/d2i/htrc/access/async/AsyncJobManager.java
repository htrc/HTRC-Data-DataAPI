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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.read.HectorResource;

/**
 * @author Yiming Sun
 *
 */
public class AsyncJobManager {
    
//    protected static class TerminationJob extends AsyncJob {
//
//        /**
//         * @param identifier
//         * @param asyncVolumeRetriever
//         */
//        public TerminationJob() {
//            super(null, null);
//        }
//        
//        @Override
//        public HTRCItemIdentifier getIdentifier() {
//            return null;
//        }
//        
//        @Override
//        public void finished(List<ExceptionAwareVolumeReader> exceptionAwareVolumeReaders) {
//            
//        }
//        
//        @Override
//        public void failed(List<ExceptionAwareVolumeReader> exceptionAwareVolumeReaders, DataAPIException dataAPIException) {
//            
//        }
//    }
    
    protected static class TerminationBlockingQueue<T> implements BlockingQueue<T> {

        /**
         * @see java.util.Queue#remove()
         */
        @Override
        public T remove() {
            return null;
        }

        /**
         * @see java.util.Queue#poll()
         */
        @Override
        public T poll() {
            return null;
        }

        /**
         * @see java.util.Queue#element()
         */
        @Override
        public T element() {
            return null;
        }

        /**
         * @see java.util.Queue#peek()
         */
        @Override
        public T peek() {
            return null;
        }

        /**
         * @see java.util.Collection#size()
         */
        @Override
        public int size() {
            return 0;
        }

        /**
         * @see java.util.Collection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return true;
        }

        /**
         * @see java.util.Collection#iterator()
         */
        @Override
        public Iterator<T> iterator() {
            return null;
        }

        /**
         * @see java.util.Collection#toArray()
         */
        @Override
        public Object[] toArray() {
            return null;
        }

        /**
         * @see java.util.Collection#toArray(T[])
         */
        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        /**
         * @see java.util.Collection#containsAll(java.util.Collection)
         */
        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        /**
         * @see java.util.Collection#addAll(java.util.Collection)
         */
        @Override
        public boolean addAll(Collection<? extends T> c) {
            return false;
        }

        /**
         * @see java.util.Collection#removeAll(java.util.Collection)
         */
        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        /**
         * @see java.util.Collection#retainAll(java.util.Collection)
         */
        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        /**
         * @see java.util.Collection#clear()
         */
        @Override
        public void clear() {
        }

        /**
         * @see java.util.concurrent.BlockingQueue#add(java.lang.Object)
         */
        @Override
        public boolean add(T e) {
            return false;
        }

        /**
         * @see java.util.concurrent.BlockingQueue#offer(java.lang.Object)
         */
        @Override
        public boolean offer(T e) {
            return false;
        }

        /**
         * @see java.util.concurrent.BlockingQueue#put(java.lang.Object)
         */
        @Override
        public void put(T e) throws InterruptedException {
        }

        /**
         * @see java.util.concurrent.BlockingQueue#offer(java.lang.Object, long, java.util.concurrent.TimeUnit)
         */
        @Override
        public boolean offer(T e, long timeout, TimeUnit unit)
                throws InterruptedException {
            return false;
        }

        /**
         * @see java.util.concurrent.BlockingQueue#take()
         */
        @Override
        public T take() throws InterruptedException {
            return null;
        }

        /**
         * @see java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit)
         */
        @Override
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            return null;
        }

        /**
         * @see java.util.concurrent.BlockingQueue#remainingCapacity()
         */
        @Override
        public int remainingCapacity() {
            return 0;
        }

        /**
         * @see java.util.concurrent.BlockingQueue#remove(java.lang.Object)
         */
        @Override
        public boolean remove(Object o) {
            return false;
        }

        /**
         * @see java.util.concurrent.BlockingQueue#contains(java.lang.Object)
         */
        @Override
        public boolean contains(Object o) {
            return false;
        }

        /**
         * @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection)
         */
        @Override
        public int drainTo(Collection<? super T> c) {
            return 0;
        }

        /**
         * @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection, int)
         */
        @Override
        public int drainTo(Collection<? super T> c, int maxElements) {
            return 0;
        }
        
    }
    
    private static Logger log = Logger.getLogger(AsyncJobManager.class);
    
    public static final String PN_ASYNC_WORKER_COUNT = "async.worker.count";
    public static final String PN_MAX_PAGES_PER_RESULT_ENTRY = "max.pages.per.result.entry";
    public static final String PN_MAX_ASYNC_FETCH_ENTRY_COUNT = "max.async.fetch.entry.count";

    protected static AsyncJobManager instance;
    protected static volatile boolean initialized = false;

    protected final ParameterContainer parameterContainer;

    
    protected final BlockingQueue<WeakReference<BlockingQueue<AsyncJob>>> queueOfJobQueues;
    protected final WeakHashMap<BlockingQueue<AsyncJob>, WeakReference<BlockingQueue<AsyncJob>>> weakJobQueueMap;

    protected final List<AsyncWorker> workerList;
    protected final List<Thread> workerThreadList;
    protected volatile boolean shutdown;
//    protected final TerminationJob TERMINATION_JOB = new TerminationJob();
    protected final TerminationBlockingQueue<AsyncJob> TERMINATION_QUEUE = new TerminationBlockingQueue<AsyncJob>();
    protected final WeakReference<BlockingQueue<AsyncJob>> TERMINATION_QUEUE_WEAK_REFERENCE = new WeakReference<BlockingQueue<AsyncJob>>(TERMINATION_QUEUE);
    
    
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
        this.shutdown = false;
        this.weakJobQueueMap = new WeakHashMap<BlockingQueue<AsyncJob>, WeakReference<BlockingQueue<AsyncJob>>>();
        int workerCount = Integer.parseInt(parameterContainer.getParameter(PN_ASYNC_WORKER_COUNT));
        this.queueOfJobQueues = new LinkedBlockingQueue<WeakReference<BlockingQueue<AsyncJob>>>();
        int maxPagesPerResult = Integer.parseInt(parameterContainer.getParameter(PN_MAX_PAGES_PER_RESULT_ENTRY));
        int maxAsyncFetchEntryCount = Integer.parseInt(parameterContainer.getParameter(PN_ASYNC_WORKER_COUNT));
        
        workerList = new ArrayList<AsyncWorker>(workerCount);
        workerThreadList = new ArrayList<Thread>(workerCount);
        
        for (int i = 0; i < workerCount; i++) {
            AsyncWorker worker = new AsyncWorker(this, hectorResource, "AsyncWorker-" + i, maxPagesPerResult, maxAsyncFetchEntryCount);
            Thread thread = new Thread(worker, "AsyncWorker-" + i);
            thread.start();
            workerList.add(worker);
            workerThreadList.add(thread);
            if (log.isDebugEnabled()) log.debug(thread.getName() + " thread started");
        }
        
    }

    
    public void shutdown() {
        if (log.isDebugEnabled()) log.debug("shutdown received");
        this.shutdown = true;
        for (AsyncWorker worker : workerList) {
           worker.shutdown();
           queueOfJobQueues.offer(TERMINATION_QUEUE_WEAK_REFERENCE);
        }
    }
    
    public void submitJobs(BlockingQueue<AsyncJob> jobQueue) {
        synchronized (weakJobQueueMap) {
            WeakReference<BlockingQueue<AsyncJob>> jobQueueWeakReference = new WeakReference<BlockingQueue<AsyncJob>>(jobQueue);
            weakJobQueueMap.put(jobQueue, jobQueueWeakReference);
            boolean result = queueOfJobQueues.offer(jobQueueWeakReference);
            if (!result) {
                log.fatal("Failed to offer jobQueue to queueOfJobQueues");
                assert(result);
            }
        }
    }
    
    private void requeueJobQueue(WeakReference<BlockingQueue<AsyncJob>> jobQueueWeakReference) {
        boolean result = queueOfJobQueues.offer(jobQueueWeakReference);
        if (!result) {
            log.fatal("Failed to requeue jobQueue");
            assert(result);
        }
    }
    
    public AsyncJob getJob() {
        AsyncJob asyncJob = null;

        if (!shutdown) {
            try {
                WeakReference<BlockingQueue<AsyncJob>> jobQueueWeakReference = queueOfJobQueues.take();
                if (jobQueueWeakReference != null) {
                    
                    BlockingQueue<AsyncJob> jobQueue = jobQueueWeakReference.get();
                    if (jobQueue != null) {
                        if (!jobQueue.isEmpty()) {
                            try {
                                asyncJob = jobQueue.take();
                                requeueJobQueue(jobQueueWeakReference);
                            } catch (InterruptedException e) {
                                log.warn("jobQueue.take() interrupted", e);
                            }
                        } else {
                            log.info("Exhausted job queue not requeued");
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.warn("queueOfJobQueues.take() interrupted", e);
            }
        }        
        return asyncJob;
    }
    
    public void skip(BlockingQueue<AsyncJob> jobQueue) {
        synchronized(weakJobQueueMap) {
            WeakReference<BlockingQueue<AsyncJob>> weakReference = weakJobQueueMap.get(jobQueue);
            if (weakReference != null) {
                queueOfJobQueues.remove(weakReference);
            }
        }
    }
    
    public void unskip(BlockingQueue<AsyncJob> jobQueue) {
        synchronized(weakJobQueueMap) {
            WeakReference<BlockingQueue<AsyncJob>> weakReference = weakJobQueueMap.get(jobQueue);
            if (weakReference != null) {
                boolean result = queueOfJobQueues.offer(weakReference);
                if (!result) {
                    log.fatal("Failed to unskip jobQueue");
                    assert(result);
                }
            }
        }
    }
    
    Map<String, String> getStatus() {
        Map<String, String> statusMap = new HashMap<String, String>();
        
        statusMap.put("queueEntryCount", Integer.toString(queueOfJobQueues.size()));
        statusMap.put("weakMapEntryCount", Integer.toString(weakJobQueueMap.size()));
        
        return statusMap;
    }
    
}


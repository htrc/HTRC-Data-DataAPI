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
# File:  ThrottledVolumeRetriever.java
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.VolumeInfo;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.async.ExceptionContainer.ExceptionType;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.id.VolumePageIdentifier;
import edu.indiana.d2i.htrc.access.read.HectorResource;

/**
 * @author Yiming Sun
 *
 */
public class ThrottledVolumeRetrieverImpl implements VolumeRetriever {

    private static Logger log = Logger.getLogger(ThrottledVolumeRetrieverImpl.class);
    
    public static final String PN_MAX_PAGES_PER_RETRIEVAL = "max.pages.per.retrieval";
    public static final String PN_MAX_ASYNC_FETCH_ENTRY_COUNT = "max.async.fetch.entry.count";
    public static final String PN_MAX_EXCEPTIONS_TO_REPORT = "max.exceptions.to.report";
    public static final String PN_MIN_ENTRY_COUNT_TRIGGER_DISPATCH = "min.entry.count.trigger.dispatch";
    
    
    protected static HectorResource hectorResource = null;
    protected static AsyncFetchManager asyncFetchManager = null;
    protected static int MAX_PAGES_PER_RETRIEVAL = 0;
    protected static int MAX_ASYNC_FETCH_ENTRY_COUNT = 0;
    protected static int MAX_EXCEPTIONS_TO_REPORT = 0;
    protected static int MIN_ENTRY_COUNT_TRIGGER_DISPATCH = 0;
    
    
    protected List<? extends HTRCItemIdentifier> identifierList = null;
    protected List<VolumePageIdentifier> workingList = null;
    protected List<Future<VolumeReader>> resultList = null;
    protected List<ExceptionContainer> exceptionList = null;

    // This map is needed to hold on to the VolumePageIdentifier object until it has been
    // processed. This is because the CallableVolumeFetcher uses a WeakReference to hold on
    // to the VolumePageIdentifier so if a client drops, it won't have to fetch on behalf
    // of the dropped client; without this map, the only reference to the VolumePageIdentifier
    // would be the WeakReference and it can get GC'ed before the CallableVolumeFetch is even
    // executed.
    protected Map<Future<VolumeReader>, VolumePageIdentifier> resultToIDMap = null;
    
    
    
    public static void init(ParameterContainer parameterContainer, HectorResource hectorResource, AsyncFetchManager asyncFetchManager) {
        MAX_ASYNC_FETCH_ENTRY_COUNT = Integer.parseInt(parameterContainer.getParameter(PN_MAX_ASYNC_FETCH_ENTRY_COUNT));
        MAX_EXCEPTIONS_TO_REPORT = Integer.parseInt(parameterContainer.getParameter(PN_MAX_EXCEPTIONS_TO_REPORT));
        MAX_PAGES_PER_RETRIEVAL = Integer.parseInt(parameterContainer.getParameter(PN_MAX_PAGES_PER_RETRIEVAL));
        MIN_ENTRY_COUNT_TRIGGER_DISPATCH = Integer.parseInt(parameterContainer.getParameter(PN_MIN_ENTRY_COUNT_TRIGGER_DISPATCH));
        
        ThrottledVolumeRetrieverImpl.hectorResource = hectorResource;
        ThrottledVolumeRetrieverImpl.asyncFetchManager = asyncFetchManager;
    }
    
    public static ThrottledVolumeRetrieverImpl newInstance() {
        ThrottledVolumeRetrieverImpl instance = new ThrottledVolumeRetrieverImpl();
        return instance;
    }
    
    protected ThrottledVolumeRetrieverImpl() {
        this.workingList = new LinkedList<VolumePageIdentifier>();
        this.resultList = new LinkedList<Future<VolumeReader>>();
        this.exceptionList = new LinkedList<ExceptionContainer>();
        this.resultToIDMap = new HashMap<Future<VolumeReader>, VolumePageIdentifier>();
    }
    
    public void setRetrievalIDs(List<? extends HTRCItemIdentifier> identifiers) {
        this.identifierList = identifiers;
        dispatchWork();
    }
    
    protected int dispatchWork() {
        int availableSlots = MAX_ASYNC_FETCH_ENTRY_COUNT - resultList.size();
        int jobDispatched = 0;
        
        if (log.isDebugEnabled()) log.debug("availableSlots: " + availableSlots);
        
        boolean done = false;
        while (availableSlots > 0 && !done) {
            if (!workingList.isEmpty()) {
                VolumePageIdentifier volumePageIdentifier = workingList.remove(0);
                Future<VolumeReader> future = asyncFetchManager.submit(volumePageIdentifier);
                resultList.add(future);
                resultToIDMap.put(future, volumePageIdentifier);
                availableSlots--;
                jobDispatched++;
                if (log.isDebugEnabled()) log.debug("workingList not empty, availableSlots: " + availableSlots + " jobDispatched: " + jobDispatched);
            } else if (!identifierList.isEmpty()){
                if (log.isDebugEnabled()) log.debug("workingList empty, breakdown identifierList");
                HTRCItemIdentifier identifier = identifierList.remove(0);
                String volumeID = identifier.getVolumeID();
                List<String> pageSequences = identifier.getPageSequences();
                if (pageSequences == null) {
                    try {
                        VolumeInfo volumeInfo = hectorResource.getVolumeInfo(volumeID);
                        int pageCount = volumeInfo.getPageCount();
                        pageSequences = generatePageSequenceList(pageCount);
                    } catch (RepositoryException re) {
                        log.error("RepositoryException while getVolumeInfo", re);
                        enlistException(re);
                    } catch (KeyNotFoundException knfe) {
                        log.error("KeyNotFoundException while getVolumeInfo", knfe);
                        enlistException(knfe);
                    }
                }
                
                if (pageSequences != null) {
                    List<VolumePageIdentifier> workingIDList = breakdownPageSequences(volumeID, pageSequences);
                    workingList.addAll(workingIDList);
                } 
            } else {
                if (log.isDebugEnabled()) log.debug("no more work to dispatch");
                done = true;
            }
        }
        
        return jobDispatched;
    }
    
    protected List<VolumePageIdentifier> breakdownPageSequences(String volumeID, List<String> pageSequences) {
        List<VolumePageIdentifier> identifiers = new LinkedList<VolumePageIdentifier>();

        int size = pageSequences.size();
        int fullBatchCount = (size / MAX_PAGES_PER_RETRIEVAL);

        for (int i = 0; i < fullBatchCount; i++) {
            VolumePageIdentifier volumePageIdentifier = new VolumePageIdentifier(volumeID);
            for (int j = 0; j < MAX_PAGES_PER_RETRIEVAL; j++) {
                volumePageIdentifier.addPageSequence(pageSequences.remove(0));
            }
            identifiers.add(volumePageIdentifier);
        }
        
        int remainingCount = size % MAX_PAGES_PER_RETRIEVAL;
        
        if (remainingCount > 0) {
            VolumePageIdentifier volumePageIdentifier = new VolumePageIdentifier(volumeID);
            for (int i = 0; i < remainingCount; i++) {
                volumePageIdentifier.addPageSequence(pageSequences.remove(0));
            }
            
            identifiers.add(volumePageIdentifier);
        }
        
        return identifiers;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#hasMoreVolumes()
     */
    @Override
    public boolean hasMoreVolumes() {
        boolean moreVolumes = !resultList.isEmpty() || !workingList.isEmpty() || !identifierList.isEmpty() || !exceptionList.isEmpty();
        return moreVolumes;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#nextVolume()
     */
    @Override
    public VolumeReader nextVolume() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        VolumeReader volumeReader = null;
        boolean done = false;
        
        while (!done) {
            if (!resultList.isEmpty()) {
                if (log.isDebugEnabled()) log.debug("trying to return entry from resultList");
                Future<VolumeReader> future = resultList.remove(0);
                
                try {
                    volumeReader = future.get();
                    done = true;
                } catch (InterruptedException ie) {
                    log.error("Async Fetch Interrupted: ", ie);
                } catch (ExecutionException ee) {
                    log.error("future.get() caused exception", ee);
                    Throwable throwable = ee.getCause();
                    if (throwable instanceof Exception) {
                        enlistException((Exception)throwable);
                    }
                } finally {
                    resultToIDMap.remove(future);
                }
                
                if (resultList.size() <= MIN_ENTRY_COUNT_TRIGGER_DISPATCH) {
                    if (log.isDebugEnabled()) log.debug("trigger threshold reached");
                    dispatchWork();
                }
                
            } else if (!workingList.isEmpty() || !identifierList.isEmpty()) {
                if (log.isDebugEnabled()) log.debug("resultList empty, dispatch more work");
                dispatchWork();
            } else if (!exceptionList.isEmpty()) {
                if (log.isDebugEnabled()) log.debug("only exceptions are left");
                ExceptionContainer exceptionContainer = exceptionList.remove(0);
                ExceptionType exceptionType = exceptionContainer.getExceptionType();
                switch (exceptionType) {
                case EXCEPTION_KEY_NOT_FOUND:
                    throw (KeyNotFoundException)exceptionContainer.getException();
                case EXCEPTION_REPOSITORY:
                    throw (RepositoryException)exceptionContainer.getException();
                case EXCEPTION_POLICY_VIOLATION:
                    throw (PolicyViolationException)exceptionContainer.getException();
                }
                // no need to set "done = true" here because the thrown exception will bail out the method call
                
            } else {
                if (log.isDebugEnabled()) log.debug("nothing left. all done");
                done = true;
            }
        }        
        return volumeReader;
    }
    
    protected List<String> generatePageSequenceList(int pageCount) {
        List<String> pageSequences = new ArrayList<String>();
        for (int i = 1; i < pageCount; i++) {
            String pageSequence = HTRCItemIdentifierFactory.Parser.generatePageSequenceString(i);
            pageSequences.add(pageSequence);
        }
        return pageSequences;
    }

    protected void enlistException(Exception exception) {
        if (exceptionList.size() < MAX_EXCEPTIONS_TO_REPORT) {
            if (exception instanceof KeyNotFoundException) {
                ExceptionContainer exceptionContainer = new ExceptionContainer(exception, ExceptionType.EXCEPTION_KEY_NOT_FOUND);
                exceptionList.add(exceptionContainer);
            } else if (exception instanceof RepositoryException) {
                ExceptionContainer exceptionContainer = new ExceptionContainer(exception, ExceptionType.EXCEPTION_REPOSITORY);
                exceptionList.add(exceptionContainer);
            } else if (exception instanceof PolicyViolationException) {
                ExceptionContainer exceptionContainer = new ExceptionContainer(exception, ExceptionType.EXCEPTION_POLICY_VIOLATION);
                exceptionList.add(exceptionContainer);
            }
        }
    }

}


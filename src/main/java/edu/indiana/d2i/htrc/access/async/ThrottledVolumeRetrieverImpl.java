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
# File:  ThrottledVolumeRetriever.java
# Description:  This class is an implementation of the VolumeRetriever interface, and it throttles the retrieval of volumes to achieve evenly distributed workloads
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
import edu.indiana.d2i.htrc.access.id.IdentifierImpl;
import edu.indiana.d2i.htrc.access.id.IdentifierParserFactory;
import edu.indiana.d2i.htrc.access.read.HectorResource;
import edu.indiana.d2i.htrc.audit.Auditor;

/**
 * This class is an implementation of the VolumeRetriever interface, and it throttles the retrieval of volumes to achieve evenly distributed workloads
 * 
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
    protected List<IdentifierImpl> workingList = null;
    protected List<Future<VolumeReader>> resultList = null;
    protected List<ExceptionContainer> exceptionList = null;
    protected final Auditor auditor;

    // This map is needed to hold on to the VolumePageIdentifier object until it has been
    // processed. This is because the CallableVolumeFetcher uses a WeakReference to hold on
    // to the VolumePageIdentifier so if a client drops, it won't have to fetch on behalf
    // of the dropped client; without this map, the only reference to the VolumePageIdentifier
    // would be the WeakReference and it can get GC'ed before the CallableVolumeFetch is even
    // executed.
    protected Map<Future<VolumeReader>, IdentifierImpl> resultToIDMap = null;
    
    
    /**
     * Method to initialize this class
     * @param parameterContainer an ParameterContainer object
     * @param hectorResource an HectorResource object
     * @param asyncFetchManager an AsyncFetchManager object
     */
    public static void init(ParameterContainer parameterContainer, HectorResource hectorResource, AsyncFetchManager asyncFetchManager) {
        MAX_ASYNC_FETCH_ENTRY_COUNT = Integer.parseInt(parameterContainer.getParameter(PN_MAX_ASYNC_FETCH_ENTRY_COUNT));
        MAX_EXCEPTIONS_TO_REPORT = Integer.parseInt(parameterContainer.getParameter(PN_MAX_EXCEPTIONS_TO_REPORT));
        MAX_PAGES_PER_RETRIEVAL = Integer.parseInt(parameterContainer.getParameter(PN_MAX_PAGES_PER_RETRIEVAL));
        MIN_ENTRY_COUNT_TRIGGER_DISPATCH = Integer.parseInt(parameterContainer.getParameter(PN_MIN_ENTRY_COUNT_TRIGGER_DISPATCH));
        
        ThrottledVolumeRetrieverImpl.hectorResource = hectorResource;
        ThrottledVolumeRetrieverImpl.asyncFetchManager = asyncFetchManager;
    }
    
    /**
     * Factory method to create an new instance of this class
     * @param auditor an Auditor object
     * @return a new instance of ThrottledVolumeRetrieverImpl object
     */
    public static ThrottledVolumeRetrieverImpl newInstance(Auditor auditor) {
        ThrottledVolumeRetrieverImpl instance = new ThrottledVolumeRetrieverImpl(auditor);
        return instance;
    }
    
    /**
     * Constructor. Used internally by the factory method
     * @param auditor an Auditor object
     */
    protected ThrottledVolumeRetrieverImpl(Auditor auditor) {
        this.auditor = auditor;
        this.workingList = new LinkedList<IdentifierImpl>();
        this.resultList = new LinkedList<Future<VolumeReader>>();
        this.exceptionList = new LinkedList<ExceptionContainer>();
        this.resultToIDMap = new HashMap<Future<VolumeReader>, IdentifierImpl>();
    }
    
    /**
     * Method for setting a List of HTRCItemIdentifier objects for retrieval
     * @param identifiers a List of HTRCItemIdentifier objects for retrieval
     */
    public void setRetrievalIDs(List<? extends HTRCItemIdentifier> identifiers) {
        this.identifierList = identifiers;
        dispatchWork();
    }
    
    /**
     * Method that breaks down the workload into a number of jobs and dispatches them to the asynchronous fetch mechanism
     * 
     * @return the number of jobs dispatched
     */
    protected int dispatchWork() {
        int availableSlots = MAX_ASYNC_FETCH_ENTRY_COUNT - resultList.size();
        int jobDispatched = 0;
        
        if (log.isDebugEnabled()) log.debug("availableSlots: " + availableSlots);
        
        boolean done = false;
        while (availableSlots > 0 && !done) {
            if (!workingList.isEmpty()) {
                IdentifierImpl identifierImpl = workingList.remove(0);
                Future<VolumeReader> future = asyncFetchManager.submit(identifierImpl);
                resultList.add(future);
                resultToIDMap.put(future, identifierImpl);
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
                    List<IdentifierImpl> workingIDList = breakdownPageSequences(volumeID, pageSequences);
                    workingList.addAll(workingIDList);
                }
                
                List<String> metadataNames = identifier.getMetadataNames();
                if (metadataNames != null) {
                    List<IdentifierImpl> metadataList = breakdownMetadataNames(volumeID, metadataNames);
                    workingList.addAll(metadataList);
                }
            } else {
                if (log.isDebugEnabled()) log.debug("no more work to dispatch");
                done = true;
            }
        }
        
        return jobDispatched;
    }
    
    /**
     * Method that breaks down the total number of pages to be retrieved for a given volumeID into a number of smaller batches
     * @param volumeID volumeID of the volume or pages to be retrieved
     * @param pageSequences a List of page sequence numbers to be retrieved
     * @return a List of IdentifierImpl objects containing the broken-down workloads
     */
    protected List<IdentifierImpl> breakdownPageSequences(String volumeID, List<String> pageSequences) {
        List<IdentifierImpl> identifiers = new LinkedList<IdentifierImpl>();

        int size = pageSequences.size();
        int fullBatchCount = (size / MAX_PAGES_PER_RETRIEVAL);

        for (int i = 0; i < fullBatchCount; i++) {
            IdentifierImpl identifierImpl = new IdentifierImpl(volumeID);
            for (int j = 0; j < MAX_PAGES_PER_RETRIEVAL; j++) {
                identifierImpl.addPageSequence(pageSequences.remove(0));
            }
            identifiers.add(identifierImpl);
        }
        
        int remainingCount = size % MAX_PAGES_PER_RETRIEVAL;
        
        if (remainingCount > 0) {
            IdentifierImpl identifierImpl = new IdentifierImpl(volumeID);
            for (int i = 0; i < remainingCount; i++) {
                identifierImpl.addPageSequence(pageSequences.remove(0));
            }
            
            identifiers.add(identifierImpl);
        }
        
        return identifiers;
    }
    
    /**
     * Method that breaks down the total number of metadata entries to be retrieved for a given volumeID into a number of smaller batches
     * @param volumeID volumeID of the volume whose metadata to be retrieved
     * @param metadataNames a List of metadata entry names to be retrieved
     * @return a List of IdentifierImpl objects containing the broken-down workloads
     */
    protected List<IdentifierImpl> breakdownMetadataNames(String volumeID, List<String> metadataNames) {
        List<IdentifierImpl> identifiers = new LinkedList<IdentifierImpl>();
        for (String metadataName : metadataNames) {
            IdentifierImpl identifierImpl = new IdentifierImpl(volumeID);
            identifierImpl.addMetadataName(metadataName);
            identifiers.add(identifierImpl);
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
                        enlistException((Exception)throwable, auditor);
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
    
    /**
     * Method to generate page sequence number strings based on the page count of the volume
     * @param pageCount number of pages in the volume
     * @return a List of String objects containing the page sequence numbers
     */
    protected List<String> generatePageSequenceList(int pageCount) {
        List<String> pageSequences = new ArrayList<String>();
        for (int i = 1; i < pageCount; i++) {
            String pageSequence = IdentifierParserFactory.Parser.generatePageSequenceString(i);
            pageSequences.add(pageSequence);
        }
        return pageSequences;
    }

    /**
     * Method that holds Exception objects into ExceptionContainer objects and adds them to a List, so that the asynchronous fetch process can carry on and the Exceptions can later be sent to the client 
     * @param exception an Exception object to be held and later sent to the client
     * @param auditor an Auditor object
     */
    protected void enlistException(Exception exception, Auditor auditor) {
        if (exceptionList.size() < MAX_EXCEPTIONS_TO_REPORT) {
            if (exception instanceof KeyNotFoundException) {
                ExceptionContainer exceptionContainer = new ExceptionContainer(exception, ExceptionType.EXCEPTION_KEY_NOT_FOUND);
                exceptionList.add(exceptionContainer);
                auditor.error("KeyNotFoundException", "Key Not Found", exception.getMessage());
            } else if (exception instanceof RepositoryException) {
                ExceptionContainer exceptionContainer = new ExceptionContainer(exception, ExceptionType.EXCEPTION_REPOSITORY);
                exceptionList.add(exceptionContainer);
                auditor.error("RepositoryException", "Cassandra Timed Out", exception.getMessage());
            } else if (exception instanceof PolicyViolationException) {
                ExceptionContainer exceptionContainer = new ExceptionContainer(exception, ExceptionType.EXCEPTION_POLICY_VIOLATION);
                exceptionList.add(exceptionContainer);
                auditor.error("PolicyViolationException", "Request Too Greedy", exception.getMessage());
            }
        }
    }

}


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
# File:  AsyncVolumeRetriever.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.async.ExceptionAwareVolumeReader.DataType;
import edu.indiana.d2i.htrc.access.exception.DataAPIException;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;

/**
 * @author Yiming Sun
 *
 */
public class AsyncVolumeRetriever implements VolumeRetriever {
    
    protected final Object lock;
    protected HashSet<String> outstandingVolumeIDsSet;
    protected List<VolumeReader> volumeReadersList;
    protected DataAPIException dataAPIException;
    protected DataType exceptionType;
//    protected final int DEFAULT_LIST_SIZE = 500;
    
    public AsyncVolumeRetriever() {
        this.lock = new Object();
        this.outstandingVolumeIDsSet = new HashSet<String>();
        this.volumeReadersList = new LinkedList<VolumeReader>();
        this.dataAPIException = null;
        this.exceptionType = null;
    }
    
    public void addOutstandingVolumeID(String volumeID) {
        synchronized (lock) {
            this.outstandingVolumeIDsSet.add(volumeID);
        }
    }
    
    public void addResult(String volumeID, ExceptionAwareVolumeReader exceptionAwareVolumeReader) {
        DataType dataType = exceptionAwareVolumeReader.getDataType();

        synchronized (lock) {
            this.outstandingVolumeIDsSet.remove(volumeID);
            switch (dataType) {
            case CONTENT:
                this.volumeReadersList.add(exceptionAwareVolumeReader);
                break;
            case EXCEPTION_KEY_NOT_FOUND:
            case EXCEPTION_POLICY_VIOLATION:
            case EXCEPTION_REPOSITORY:
                // since exception can only be thrown once from VolumeRetriever, we are only storing at most 1 exception
                // here, which is the earliest exception coming back.
                if (this.dataAPIException == null) {
                    this.exceptionType = dataType;
                    this.dataAPIException = exceptionAwareVolumeReader.getException();
                }
                break;
            }

            lock.notify();
        }
    }
    
   
    

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#hasMoreVolumes()
     */
    @Override
    public boolean hasMoreVolumes() {
        boolean result = false;
        synchronized (lock) {
            if (!outstandingVolumeIDsSet.isEmpty()) {
                result = true;
            } else if (!volumeReadersList.isEmpty()) {
                result = true;
            } else if (dataAPIException != null) {
                result = true;
            }
        }
        return result;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#nextVolume()
     */
    @Override
    public VolumeReader nextVolume() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        // unlike the synchronous volume retriever where we can just throw the first exception we encounter and
        // the following volumes will not be retrieved.  for the async, we must return all good results first before
        // throwing the exception, wihch means, even if we have an exception at hand, it cannot be thrown until
        // all outstanding volumes have come back.
        VolumeReader volumeReader = null;
        boolean block = true;
        synchronized(lock) {
           while (block) {
               if (!volumeReadersList.isEmpty()) {
                   volumeReader = volumeReadersList.remove(0);
                   block = false;
               } else if (outstandingVolumeIDsSet.isEmpty() && dataAPIException != null) {
                   try {
                       block = false;
                       switch (exceptionType) {
                       case EXCEPTION_KEY_NOT_FOUND:
                           throw (KeyNotFoundException)dataAPIException;
                       case EXCEPTION_POLICY_VIOLATION:
                           throw (PolicyViolationException)dataAPIException;
                       case EXCEPTION_REPOSITORY:
                           throw (RepositoryException)dataAPIException;
                       }
                   } finally {
                       dataAPIException = null;
                       exceptionType = null;
                   }
               } else {
                   if (!outstandingVolumeIDsSet.isEmpty()) {
                       try {
                           lock.wait();
                       } catch (InterruptedException e) {
                           
                       }
                   } else {
                       block = false;
                   }
                       
               }
           }
        }
        return volumeReader;
    }

}


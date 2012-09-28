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
# File:  SynchronousFetchVolumeReaderImpl.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.access.read.HectorResource;

/**
 * @author Yiming Sun
 *
 */
public class SynchronousFetchVolumeReaderImpl extends ExceptionAwareVolumeReaderImpl {
    private static Logger log = Logger.getLogger(SynchronousFetchVolumeReaderImpl.class);
    protected final HectorResource hectorResource;
    protected List<String> unfetchedPageSequences;
    protected List<ExceptionContainer> exceptions;
    public SynchronousFetchVolumeReaderImpl(HTRCItemIdentifier identifier, List<String> unfetchedPageSequences, HectorResource hectorResource) {
        super(identifier);
        this.unfetchedPageSequences = unfetchedPageSequences;
        this.hectorResource = hectorResource;
        this.exceptions = new LinkedList<ExceptionContainer>();
    }
    
    @Override
    public DataType getDataType() {
        return DataType.SYNC_FETCH;
    }
    
    public void fetch() {
        try {
            pages = hectorResource.retrievePageContents(volumeID, unfetchedPageSequences);
            if (SynchronousFetchVolumeReaderImpl.log.isDebugEnabled()) {
                SynchronousFetchVolumeReaderImpl.log.debug("fetched pages for " + volumeID + " pages: " + unfetchedPageSequences.get(0) + " ~ " + unfetchedPageSequences.get(unfetchedPageSequences.size() - 1));
                SynchronousFetchVolumeReaderImpl.log.debug("fetched " + pages.size() + " pages");
            }
            unfetchedPageSequences = null;
        } catch (KeyNotFoundException knfe) {
            SynchronousFetchVolumeReaderImpl.log.error("KeyNotFoundException in sync fetch", knfe);
            ExceptionContainer exceptionContainer = new ExceptionContainer(knfe, ExceptionType.EXCEPTION_KEY_NOT_FOUND);
            exceptions.add(exceptionContainer);
        } catch (RepositoryException re) {
            SynchronousFetchVolumeReaderImpl.log.error("RepositoryException in sync fetch", re);
            ExceptionContainer exceptionContainer = new ExceptionContainer(re, ExceptionType.EXCEPTION_REPOSITORY);
            exceptions.add(exceptionContainer);
        }
    }
    
    
    
}


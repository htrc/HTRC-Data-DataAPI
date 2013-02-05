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
# File:  CallableFetch.java
# Description:  This class implements the Callable interface and performs asynchronous fetch of data
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.ContentReader;
import edu.indiana.d2i.htrc.access.read.HectorResource;
import edu.indiana.d2i.htrc.access.read.VolumeReaderImpl;


/**
 * This class implements the Callable interface and performs asynchronous fetch of data
 * 
 * @author Yiming Sun
 *
 */
public class CallableVolumeFetcher implements Callable<VolumeReader> {

    private static final Logger log = Logger.getLogger(CallableVolumeFetcher.class);
    
    private final WeakReference<HTRCItemIdentifier> idWeakReference;
    private final HectorResource hectorResource;
    
    /**
     * Constructor
     * 
     * @param itemIdentifier an HTRCItemIdentifier object as the identifier of the item to be fetched
     * @param hectorResource an HectorResource object for communication with Cassandra
     */
    public CallableVolumeFetcher(HTRCItemIdentifier itemIdentifier, HectorResource hectorResource) {
        this.idWeakReference = new WeakReference<HTRCItemIdentifier>(itemIdentifier);
        this.hectorResource = hectorResource;
        
    }
    
    /**
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public VolumeReader call() throws Exception {
        HTRCItemIdentifier itemIdentifier = idWeakReference.get();
        VolumeReaderImpl volumeReaderImpl = null;
        if (itemIdentifier != null) {
            volumeReaderImpl = new VolumeReaderImpl(itemIdentifier);
            String volumeID = itemIdentifier.getVolumeID();
            
            List<String> pageSequences = itemIdentifier.getPageSequences();
            if (pageSequences != null) {
                List<ContentReader> pageContents = hectorResource.retrievePageContents(volumeID, pageSequences);
                volumeReaderImpl.setPages(pageContents);
            }
            
            
            List<String> metadataNames = itemIdentifier.getMetadataNames();
            if (metadataNames != null) {
                List<ContentReader> metadataContents = hectorResource.retrieveMetadata(volumeID, metadataNames);
                volumeReaderImpl.setMetadata(metadataContents);
            }
        } else {
            if (log.isDebugEnabled()) log.debug("An identifier went away");
        }
        return volumeReaderImpl;
    }

}


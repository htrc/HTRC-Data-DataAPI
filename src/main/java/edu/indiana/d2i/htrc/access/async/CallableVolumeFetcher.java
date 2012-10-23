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
# File:  CallableFetch.java
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
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.id.VolumePageIdentifier;
import edu.indiana.d2i.htrc.access.read.HectorResource;
import edu.indiana.d2i.htrc.access.read.VolumeReaderImpl;


/**
 * @author Yiming Sun
 *
 */
public class CallableVolumeFetcher implements Callable<VolumeReader> {

    private static final Logger log = Logger.getLogger(CallableVolumeFetcher.class);
    
    private final WeakReference<VolumePageIdentifier> idWeakReference;
    private final HectorResource hectorResource;
    
    public CallableVolumeFetcher(VolumePageIdentifier volumePageIdentifier, HectorResource hectorResource) {
        this.idWeakReference = new WeakReference<VolumePageIdentifier>(volumePageIdentifier);
        this.hectorResource = hectorResource;
        
    }
    
    /**
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public VolumeReader call() throws Exception {
        VolumePageIdentifier volumePageIdentifier = idWeakReference.get();
        VolumeReaderImpl volumeReaderImpl = null;
        if (volumePageIdentifier != null) {
            volumeReaderImpl = new VolumeReaderImpl(volumePageIdentifier);
            String volumeID = volumePageIdentifier.getVolumeID();
            List<String> pageSequences = volumePageIdentifier.getPageSequences();
            
            List<PageReader> pageContents = hectorResource.retrievePageContents(volumeID, pageSequences);
            volumeReaderImpl.setPages(pageContents);
        } else {
            if (log.isDebugEnabled()) log.debug("An identifier went away");
        }
        return volumeReaderImpl;
    }

}


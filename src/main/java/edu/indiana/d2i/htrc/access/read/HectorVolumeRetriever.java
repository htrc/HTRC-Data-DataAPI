/*
#
# Copyright 2007 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or areed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  HectorVolumeRetriever.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.read;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.prettyprint.hector.api.Keyspace;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.VolumeInfo;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.read.HectorResource.VolumeReaderImpl;

/**
 * @author Yiming Sun
 *
 */
public class HectorVolumeRetriever implements VolumeRetriever {
    
    private static final Logger log = Logger.getLogger(HectorVolumeRetriever.class);
    
    public static final int BAD_PAGE_COUNT = -1;
    
    protected final List<? extends HTRCItemIdentifier> identifiers;
    protected final HectorResource hectorResource;
    protected final Map<String, ? extends VolumeInfo> volumeInfoMap;
    
    protected final Iterator<? extends HTRCItemIdentifier> idIterator;
    
    public HectorVolumeRetriever(List<? extends HTRCItemIdentifier> identifiers, HectorResource hectorResource, Map<String, ? extends VolumeInfo> volumeInfoMap) {
        this.identifiers = identifiers;
        this.hectorResource = hectorResource;
        this.volumeInfoMap = volumeInfoMap;
        this.idIterator = identifiers.iterator();
    }
    
    public boolean hasMoreVolumes() {
        return idIterator.hasNext();
    }
    
    public VolumeReader nextVolume() throws KeyNotFoundException, PolicyViolationException {
        HTRCItemIdentifier itemIdentifier = idIterator.next();
        
        VolumeReader volumeReader = retrieveVolume(itemIdentifier);
        return volumeReader;
    }
    
    
    protected VolumeReader retrieveVolume(HTRCItemIdentifier identifier) throws KeyNotFoundException, PolicyViolationException {
        List<String> pageSequences = null;
//        Keyspace keyspace = hectorResource.getKeyspace();
        
        if (identifier.getPageSequences() == null) {
            VolumeInfo volumeInfo = volumeInfoMap.get(identifier.getVolumeID());
            int pageCount = volumeInfo.getPageCount();
            pageSequences = generatePageSequenceList(pageCount);
        } else {
            pageSequences = identifier.getPageSequences();
        }

        VolumeReaderImpl fullVolumeReader = hectorResource.createVolumeReader(identifier);
        List<PageReader> pageContents = hectorResource.retrievePageContents(identifier.getVolumeID(), pageSequences);
        fullVolumeReader.setPages(pageContents);
        return fullVolumeReader;
    }
    
    
    
    
    private List<String> generatePageSequenceList(int pageCount) {
        List<String> pageSequences = new ArrayList<String>();
        for (int i = 1; i < pageCount; i++) {
            String pageSequence = HTRCItemIdentifierFactory.Parser.generatePageSequenceString(i);
            pageSequences.add(pageSequence);
        }
        return Collections.<String>unmodifiableList(pageSequences);
    }
    

}


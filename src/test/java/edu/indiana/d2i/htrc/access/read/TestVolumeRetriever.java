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
# File:  TestVolumeRetriever.java
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
import java.util.Iterator;
import java.util.List;

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.id.VolumePageIdentifier;
import edu.indiana.d2i.htrc.access.read.HectorResource.VolumeReaderImpl;
import edu.indiana.d2i.htrc.access.read.HectorResource.VolumeReaderImpl.PageReaderImpl;

/**
 * @author Yiming Sun
 *
 */
public class TestVolumeRetriever implements VolumeRetriever {

    private List<VolumeReader> volumeReaders = null;
    private Iterator<VolumeReader> volumeReaderIterator = null;
    
    public TestVolumeRetriever() {
        volumeReaders = new ArrayList<VolumeReader>();

        volumeReaders.add(generateFakeVolumeReader(1, 4));
        volumeReaders.add(generateFakeVolumeReader(2, 10));
        volumeReaders.add(generateFakeVolumeReader(3, 7));

        volumeReaderIterator = volumeReaders.iterator();
    }
    
    protected VolumeReader generateFakeVolumeReader(int volumeIDIndex, int maxPageSequence) {
        VolumePageIdentifier pageId = new VolumePageIdentifier("test.volume/id/" + volumeIDIndex);
        List<PageReader> pageReaders = new ArrayList<PageReader>();
        
        for (int i = 1; i <= maxPageSequence; i++) {
            String pageSequenceString = HTRCItemIdentifierFactory.Parser.generatePageSequenceString(i);
            pageId.addPageSequence(pageSequenceString);
            
            PageReader pageReader = new PageReaderImpl(pageSequenceString, "the content of page " + i + " for volume " + pageId.getVolumeID());
            pageReaders.add(pageReader);
        }
        
        VolumeReader volumeReader = new VolumeReaderImpl(pageId);
        volumeReader.setPages(pageReaders);
        
        return volumeReader;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#hasMoreVolumes()
     */
    @Override
    public boolean hasMoreVolumes() {
        return volumeReaderIterator.hasNext();
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#nextVolume()
     */
    @Override
    public VolumeReader nextVolume() throws KeyNotFoundException {
        // TODO Auto-generated method stub
        return volumeReaderIterator.next();
    }

}


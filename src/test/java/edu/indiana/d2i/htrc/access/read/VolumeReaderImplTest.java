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
# File:  VolumeReaderImplTest.java
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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.indiana.d2i.htrc.access.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.id.VolumeIdentifier;
import edu.indiana.d2i.htrc.access.read.VolumeReaderImpl.PageReaderImpl;

/**
 * @author Yiming Sun
 *
 */
public class VolumeReaderImplTest {
    
    @Test
    public void testGetPairtreeCleanedVolumeID1() {
        String volumeID = "loc.ark:/13960/t9q23z43f";
        String expectedCleandVolumeID = "loc.ark+=13960=t9q23z43f";
        
        VolumeIdentifier volumeIdentifier = new VolumeIdentifier(volumeID);
        VolumeReaderImpl volumeReaderImpl = new VolumeReaderImpl(volumeIdentifier);
        
        String actualCleanedVolumeID = volumeReaderImpl.getPairtreeCleanedVolumeID();
        
        Assert.assertEquals(expectedCleandVolumeID, actualCleanedVolumeID);
    }

    @Test
    public void testGetPairtreeCleanedVolumeID2() {
        String volumeID = "miun.ajj3079.0001.001";
        String expectedCleandVolumeID = "miun.ajj3079,0001,001";
        
        VolumeIdentifier volumeIdentifier = new VolumeIdentifier(volumeID);
        VolumeReaderImpl volumeReaderImpl = new VolumeReaderImpl(volumeIdentifier);
        
        String actualCleanedVolumeID = volumeReaderImpl.getPairtreeCleanedVolumeID();
        
        Assert.assertEquals(expectedCleandVolumeID, actualCleanedVolumeID);
    }

    @Test
    public void testPageReaderImpl() throws KeyNotFoundException {
        List<PageReader> pageReaders = new ArrayList<PageReader>();
        String[] expectedPageSequences = new String[5];
        String[] expectedPageContents = new String[5];
        
        String[] actualPageSequences = new String[5];
        String[] actualPageContents = new String[5];
        
        
        for (int i = 1; i <= 5; i++) {
            
            String pageSequence = HTRCItemIdentifierFactory.Parser.generatePageSequenceString(i);
            String pageContents = "Page " + i;
            expectedPageSequences[i - 1] = pageSequence;
            expectedPageContents[i - 1] = pageContents;
            PageReader pageReader = new PageReaderImpl(pageSequence, pageContents);
            pageReaders.add(pageReader);
        }
        
        VolumeIdentifier id = new VolumeIdentifier("test.volume/reader");
        VolumeReader volumeReader = new VolumeReaderImpl(id);
        volumeReader.setPages(pageReaders);
        
        int index =  0;
        while (volumeReader.hasMorePages()) {
            PageReader nextPage = volumeReader.nextPage();
            actualPageSequences[index] = nextPage.getPageSequence();
            actualPageContents[index] = nextPage.getPageContent();
            
            index++;
        }
        
        Assert.assertArrayEquals(expectedPageSequences, actualPageSequences);
        Assert.assertArrayEquals(expectedPageContents, actualPageContents);
        
    }
}

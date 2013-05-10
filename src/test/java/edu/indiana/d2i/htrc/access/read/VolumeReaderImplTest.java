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

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.ContentReader;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesParserFactory;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesImpl;
import edu.indiana.d2i.htrc.access.read.VolumeReaderImpl.ContentReaderImpl;

/**
 * @author Yiming Sun
 *
 */
public class VolumeReaderImplTest {
    
    // This case tests that pairtree-cleaned volumeID is properly generated from a volumeID (colons and slashes are properly escaped)
    @Test
    public void testGetPairtreeCleanedVolumeID1() {
        String volumeID = "loc.ark:/13960/t9q23z43f";
        String expectedCleandVolumeID = "loc.ark+=13960=t9q23z43f";
        
        ItemCoordinatesImpl volumeIdentifier = new ItemCoordinatesImpl(volumeID);
        VolumeReaderImpl volumeReaderImpl = new VolumeReaderImpl(volumeIdentifier);
        
        String actualCleanedVolumeID = volumeReaderImpl.getPairtreeCleanedVolumeID();
        
        Assert.assertEquals(expectedCleandVolumeID, actualCleanedVolumeID);
    }

    // This case tests that pairtree-cleaned volumeID is properly generated from a volumeID (periods are properly escaped)
    @Test
    public void testGetPairtreeCleanedVolumeID2() {
        String volumeID = "miun.ajj3079.0001.001";
        String expectedCleandVolumeID = "miun.ajj3079,0001,001";
        
        ItemCoordinatesImpl volumeIdentifier = new ItemCoordinatesImpl(volumeID);
        VolumeReaderImpl volumeReaderImpl = new VolumeReaderImpl(volumeIdentifier);
        
        String actualCleanedVolumeID = volumeReaderImpl.getPairtreeCleanedVolumeID();
        
        Assert.assertEquals(expectedCleandVolumeID, actualCleanedVolumeID);
    }

    // This case tests that page ContentReader properly outputs the page data it holds
    @Test
    public void testPageContentReaderImpl() throws KeyNotFoundException, Exception {
        final int PAGE_COUNT = 5;
        
        List<ContentReader> pageReaders = new ArrayList<ContentReader>();
        String[] expectedPageSequences = new String[PAGE_COUNT];
        byte[][] expectedPageContents = new byte[PAGE_COUNT][];
        
        String[] actualPageSequences = new String[PAGE_COUNT];
        byte[][] actualPageContents = new byte[PAGE_COUNT][];
        
        for (int i = 1; i <= PAGE_COUNT; i++) {
            
            String pageSequence = ItemCoordinatesParserFactory.Parser.generatePageSequenceString(i);
            byte[] pageContents = ("Page " + i).getBytes("utf-8");
            expectedPageSequences[i - 1] = pageSequence;
            expectedPageContents[i - 1] = pageContents;
            ContentReader pageReader = new ContentReaderImpl(pageSequence, pageContents);
            pageReaders.add(pageReader);
        }
        
        ItemCoordinatesImpl id = new ItemCoordinatesImpl("test.volume/reader");
        VolumeReader volumeReader = new VolumeReaderImpl(id);
        volumeReader.setPages(pageReaders);
        
        int index =  0;
        while (volumeReader.hasMorePages()) {
            ContentReader nextPage = volumeReader.nextPage();
            actualPageSequences[index] = nextPage.getContentName();
            actualPageContents[index] = nextPage.getContent();
            
            index++;
        }
        
        Assert.assertArrayEquals(expectedPageSequences, actualPageSequences);
        Assert.assertArrayEquals(expectedPageContents, actualPageContents);
        
    }
    
    // This case tests that metadata ContentReader properly outputs the metadata it holds
    @Test
    public void testMetadataContentReaderImpl() throws KeyNotFoundException, Exception {
        final int METADATA_COUNT = 1;
        
        TestVolumeRetriever volRetriever = new TestVolumeRetriever();
        
        List<ContentReader> metadataReaders = new ArrayList<ContentReader>();
        String[] expectedMetadataNames = new String[METADATA_COUNT];
        byte[][] expectedMetadataContents = new byte[METADATA_COUNT][];
        
        String[] actualMetadataNames = new String[METADATA_COUNT];
        byte[][] actualMetadataContents = new byte[METADATA_COUNT][];
        
        ItemCoordinatesImpl id = new ItemCoordinatesImpl("test.volume/reader");
        String metsString = volRetriever.generateFakeMETSString(id.getVolumeID());
        
        for (int i = 0; i < METADATA_COUNT; i++) {
            byte[] metadataContents = metsString.getBytes("utf-8");
            expectedMetadataNames[i] = HectorResource.CN_VOLUME_METS;
            expectedMetadataContents[i] = metadataContents;
            ContentReader metadataReader = new ContentReaderImpl(HectorResource.CN_VOLUME_METS, metadataContents);
            metadataReaders.add(metadataReader);
        }
        
        VolumeReader volumeReader = new VolumeReaderImpl(id);
        volumeReader.setMetadata(metadataReaders);
        
        int index = 0;
        while (volumeReader.hasMoreMetadata()) {
            ContentReader nextMetadata = volumeReader.nextMetadata();
            actualMetadataNames[index] = nextMetadata.getContentName();
            actualMetadataContents[index] = nextMetadata.getContent();
            index++;
        }
        
        Assert.assertArrayEquals(expectedMetadataNames, actualMetadataNames);
        Assert.assertArrayEquals(expectedMetadataContents, actualMetadataContents);
    }
}

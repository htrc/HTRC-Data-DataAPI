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
# File:  TestHectorResource.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.read;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.exceptions.HTimedOutException;
import edu.indiana.d2i.htrc.access.Constants;
import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.VolumeInfo;
import edu.indiana.d2i.htrc.access.VolumeReader.ContentReader;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesParserFactory;
import edu.indiana.d2i.htrc.access.read.VolumeReaderImpl.ContentReaderImpl;

/**
 * @author Yiming Sun
 *
 */
public class TestHectorResource extends HectorResource {

    
    public static final String[] VOLUME_IDS = {"test.fake/0001/volume1", "test.fake:/0002/volume2", "test.fake:/0003/volume3", "test.fake:/0004/volume4"};
    public static final int[] PAGE_COUNTS = {6, 5, 4, 3};
    public static final int[] METADATA_COUNTS = {1, 1, 1, 1};
    
    protected final Map<String, VolumeInfo> volumeInfoMap;
    protected final Map<String, Map<String, ContentReader>> pageReadersMap;
    protected final Map<String, Map<String, ContentReader>> metadataReaderMap;
    
    /**
     * @param parameterContainer
     */
    public TestHectorResource(final ParameterContainer parameterContainer) throws Exception {
        super(parameterContainer);
        volumeInfoMap = new HashMap<String, VolumeInfo>(VOLUME_IDS.length);
        pageReadersMap = new HashMap<String, Map<String, ContentReader>>(VOLUME_IDS.length);
        metadataReaderMap = new HashMap<String, Map<String, ContentReader>>(VOLUME_IDS.length);
        initializeFakeData();
    }
    
    @Override
    public Cluster getCluster() {
        return null;
    }
    
    @Override
    public Keyspace getKeyspace() {
        return null;
    }
    
    @Override
    public VolumeInfo getVolumeInfo(String volumeID) throws KeyNotFoundException, HTimedOutException {
        VolumeInfo volumeInfo = volumeInfoMap.get(volumeID);
        if (volumeInfo == null) {
            throw new KeyNotFoundException(volumeID);
        }
        
        return volumeInfo;
    }
    
    
    @Override
    public List<ContentReader> retrievePageContents(String volumeID, List<String> pageSequences) throws KeyNotFoundException, HTimedOutException {
        List<ContentReader> pageReaderList = new ArrayList<ContentReader>();
        Map<String, ContentReader> pageReaderMap = pageReadersMap.get(volumeID);
        if (pageReaderMap == null) {
            throw new KeyNotFoundException(volumeID);
        } else {
            for (String pageSequence : pageSequences) {
                ContentReader pageReader = pageReaderMap.get(pageSequence);
                if (pageReader == null) {
                    throw new KeyNotFoundException(volumeID + Constants.PAGE_SEQ_START_MARK + pageSequence + Constants.PAGE_SEQ_END_MARK);
                } else {
                    pageReaderList.add(pageReader);
                }
            }
        }
        return pageReaderList;
    }
    
    protected void initializeFakeData() throws Exception {
        
        for (int i = 0; i < VOLUME_IDS.length; i++) {
            BasicVolumeInfo volumeInfo = new BasicVolumeInfo(VOLUME_IDS[i]);
            volumeInfo.setCopyright(CopyrightEnum.PUBLIC_DOMAIN);
            volumeInfo.setPageCount(PAGE_COUNTS[i]);
            volumeInfoMap.put(VOLUME_IDS[i], volumeInfo);
         
            Map<String, ContentReader> pageReaderMap = new HashMap<String, ContentReader>(PAGE_COUNTS[i]);
            for (int j = 1; j <= PAGE_COUNTS[i]; j++) {
                ContentReader pageReader = generateFakePage(VOLUME_IDS[i], j);
                pageReaderMap.put(ItemCoordinatesParserFactory.Parser.generatePageSequenceString(j), pageReader);
            }
            pageReadersMap.put(VOLUME_IDS[i], pageReaderMap);
            
            Map<String, ContentReader> metadataReaderMap = new HashMap<String, ContentReader>(METADATA_COUNTS[i]);
            for (int j = 1; j < METADATA_COUNTS[i]; j++) {
                ContentReader metadataReader = generateFakeMetadata(VOLUME_IDS[i], HectorResource.CN_VOLUME_METS);
                metadataReaderMap.put(HectorResource.CN_VOLUME_METS, metadataReader);
            }
        }
            
            
        
        
       
    }
    
    protected ContentReader generateFakePage(String volumeID, int page) throws Exception {
        String pageSequenceString = ItemCoordinatesParserFactory.Parser.generatePageSequenceString(page);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write("Page contents for Volume ".getBytes("utf-8"));
        outputStream.write(volumeID.getBytes("utf-8"));
        outputStream.write(" Page ".getBytes("utf-8"));
        outputStream.write(pageSequenceString.getBytes("utf-8"));
        outputStream.close();
        
        ContentReader pageReader = new ContentReaderImpl(pageSequenceString, outputStream.toByteArray());
        
        return pageReader;
    }
    
    
    protected ContentReader generateFakeMetadata(String volumeID, String metadataName) throws Exception {
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Since TestVolumeRetriever already has a method for generating fake METS data, we use it here as well
        TestVolumeRetriever volRetriever = new TestVolumeRetriever();
        
        outputStream.write(volRetriever.generateFakeMETSString(volumeID).getBytes("utf-8"));
        
        ContentReader metadataReader = new ContentReaderImpl(metadataName, outputStream.toByteArray());
        
        return metadataReader;
        
        
    }
    

}


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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.exceptions.HTimedOutException;
import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.VolumeInfo;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.read.HectorResource.VolumeReaderImpl.PageReaderImpl;

/**
 * @author Yiming Sun
 *
 */
public class TestHectorResource extends HectorResource {

    
    public static final String[] VOLUME_IDS = {"test.fake/0001/volume1", "test.fake:/0002/volume2", "test.fake:/0003/volume3", "test.fake:/0004/volume4"};
    public static final int[] PAGE_COUNTS = {6, 5, 4, 3};
    
    protected final Map<String, VolumeInfo> volumeInfoMap;
    protected final Map<String, Map<String, PageReader>> pageReadersMap;
    
    /**
     * @param parameterContainer
     */
    public TestHectorResource(final ParameterContainer parameterContainer) {
        super(parameterContainer);
        volumeInfoMap = new HashMap<String, VolumeInfo>(VOLUME_IDS.length);
        pageReadersMap = new HashMap<String, Map<String, PageReader>>(VOLUME_IDS.length);
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
    public List<PageReader> retrievePageContents(String volumeID, List<String> pageSequences) throws KeyNotFoundException, HTimedOutException {
        List<PageReader> pageReaderList = new ArrayList<PageReader>();
        Map<String, PageReader> pageReaderMap = pageReadersMap.get(volumeID);
        if (pageReaderMap == null) {
            throw new KeyNotFoundException(volumeID);
        } else {
            for (String pageSequence : pageSequences) {
                PageReader pageReader = pageReaderMap.get(pageSequence);
                if (pageReader == null) {
                    throw new KeyNotFoundException(volumeID + "<" + pageSequence + ">");
                } else {
                    pageReaderList.add(pageReader);
                }
            }
        }
        return pageReaderList;
    }
    
    protected void initializeFakeData() {
        
        for (int i = 0; i < VOLUME_IDS.length; i++) {
            BasicVolumeInfo volumeInfo = new BasicVolumeInfo(VOLUME_IDS[i]);
            volumeInfo.setCopyright(CopyrightEnum.PUBLIC_DOMAIN);
            volumeInfo.setPageCount(PAGE_COUNTS[i]);
            volumeInfoMap.put(VOLUME_IDS[i], volumeInfo);
         
            Map<String, PageReader> pageReaderMap = new HashMap<String, PageReader>(PAGE_COUNTS[i]);
            for (int j = 1; j <= PAGE_COUNTS[i]; j++) {
                PageReader pageReader = generateFakePage(VOLUME_IDS[i], j);
                pageReaderMap.put(HTRCItemIdentifierFactory.Parser.generatePageSequenceString(j), pageReader);
            }
            pageReadersMap.put(VOLUME_IDS[i], pageReaderMap);
        }
            
            
        
        
       
    }
    
    protected PageReader generateFakePage(String volumeID, int page) {
        String pageString = Integer.toString(page);
        String pageSequenceString = HTRCItemIdentifierFactory.Parser.generatePageSequenceString(page);
        
        StringBuilder contentsBuilder = new StringBuilder("Page contents for Volume ");
        contentsBuilder.append(volumeID);
        contentsBuilder.append(" Page ");
        contentsBuilder.append(pageSequenceString);
        
        PageReader pageReader = new PageReaderImpl(pageSequenceString, contentsBuilder.toString());
        
        return pageReader;
        
    }
    

}


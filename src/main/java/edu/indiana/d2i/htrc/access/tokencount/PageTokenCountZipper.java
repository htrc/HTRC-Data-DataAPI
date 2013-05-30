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
# File:  PageTokenCountZipper.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;

import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.DataAPIException;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory;
import edu.indiana.d2i.htrc.audit.Auditor;
import gov.loc.repository.pairtree.Pairtree;

/**
 * @author Yiming Sun
 *
 */
public class PageTokenCountZipper implements TokenCountZipper {
    
    protected final Auditor auditor;
    protected static final String TOKEN_COUNT_ACCESSED_ACTION = "TOKEN_COUNT_ACCESSED";

    public PageTokenCountZipper(Auditor auditor) {
        this.auditor = auditor;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.tokencount.TokenCountZipper#countAndZip(java.io.OutputStream, edu.indiana.d2i.htrc.access.VolumeRetriever, edu.indiana.d2i.htrc.access.tokencount.Tokenizer, edu.indiana.d2i.htrc.access.tokencount.TokenFilter, java.util.Comparator)
     */
    @Override
    public void countAndZip(OutputStream outputStream, VolumeRetriever volumeRetriever, Tokenizer tokenizer, TokenFilter tokenFilter, Comparator<Entry<String, Count>> comparator) throws IOException {
        Pairtree pairtree = new Pairtree();
        ContentIdentifier identifier = null;
        String currentVolumeID = null;
        List<String> currentPageSequences = null;
        Map<String, Count> map = null;
        List<Exception> exceptionList = new LinkedList<Exception>();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setLevel(0);
        
        Iterator<TokenPackage> iterator = tokenizer.tokenize(volumeRetriever);
        
        while (iterator.hasNext()) {
            TokenPackage tokenPackage = tokenFilter.filter(iterator.next());
            identifier = tokenPackage.getContentIdentifier();
            map = new HashMap<String, Count>();
            
            try {
                
                if (!identifier.getVolumeID().equals(currentVolumeID)) {
                    if (currentVolumeID != null) {
                        auditor.audit(TOKEN_COUNT_ACCESSED_ACTION, currentVolumeID, currentPageSequences.toArray(new String[0]));
                    }
                    currentVolumeID = identifier.getVolumeID();
                    currentPageSequences = new LinkedList<String>();
                }
                
                currentPageSequences.add(identifier.getPageSequenceID());
                
                List<String> tokenList = tokenPackage.getTokenList();
                for (String token : tokenList) {
                    TokenCountZipperFactory.Helper.countToken(token, map);
                }
                String entryName = identifier.getPrefix() + "." + pairtree.cleanId(identifier.getHeadlessID()) + "/" + identifier.getPageSequenceID() + ".count";
                TokenCountZipperFactory.Helper.sendEntry(map, entryName, zipOutputStream, comparator);
                
                
            } catch (DataAPIException e) {
                exceptionList.add(e);
            }
        }
        
        if (currentVolumeID != null) {
            auditor.audit(TOKEN_COUNT_ACCESSED_ACTION, currentVolumeID, currentPageSequences.toArray(new String[0]));
        }
        
        if (!exceptionList.isEmpty()) {
            ZipMakerFactory.Helper.injectErrorEntry(zipOutputStream, false, exceptionList);
        }
        
        zipOutputStream.close();
    }

}


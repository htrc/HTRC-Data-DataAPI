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
# File:  VolumeLevelTokenCountZipper.java
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
import gov.loc.repository.pairtree.Pairtree;

/**
 * @author Yiming Sun
 *
 */
public class VolumeTokenCountZipper implements TokenCountZipper {

    /**
     * 
     * @see edu.indiana.d2i.htrc.access.tokencount.TokenCountZipper#countAndZip(java.io.OutputStream, edu.indiana.d2i.htrc.access.VolumeRetriever, edu.indiana.d2i.htrc.access.tokencount.Tokenizer, edu.indiana.d2i.htrc.access.tokencount.TokenFilter, java.util.Comparator)
     */
    @Override
    public void countAndZip(OutputStream outputStream, VolumeRetriever volumeRetriever, Tokenizer tokenizer, TokenFilter tokenFilter, Comparator<Entry<String, Count>> comparator) throws IOException {
        
        Pairtree pairtree = new Pairtree();
        ContentIdentifier currentIdentifier = null;
        String hyphenedLastWord = null;
        ContentIdentifier identifier = null;
        Map<String, Count> map = null; 
        List<Exception> exceptionList = new LinkedList<Exception>();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setLevel(0);
        
        Iterator<TokenPackage> iterator = tokenizer.tokenize(volumeRetriever);
        
        while (iterator.hasNext()) {
            TokenPackage tokenPackage = tokenFilter.filter(iterator.next());
            identifier = tokenPackage.getContentIdentifier();
            
            // if the volumeID has changed, must conclude the previous volumeID (althoug assigned in the variable named currentIdentifier) by 
            // sending its token count to the client, and then reset the map
            if (currentIdentifier == null || !identifier.getVolumeID().equals(currentIdentifier.getVolumeID())) {
                
                // currentIdentifier was initially null because there was no previous volume.  But if it is not null, we must process the previous volume
                if (currentIdentifier != null) {
                    // the hyphenedLastWord is the last word on a page that ends with a hyphen. When we encounter such a word, we usually cannot token count it, because we must combine it with the
                    // first word from the following page to make a token, unless the following page belongs to a different volume (which is the case here)
                    if (hyphenedLastWord != null) {  
                        TokenCountZipperFactory.Helper.countToken(hyphenedLastWord, map);
                        hyphenedLastWord = null;
                    }
                    String entryName = currentIdentifier.getPrefix() + "." + pairtree.cleanId(currentIdentifier.getHeadlessID()) + ".count";
                    TokenCountZipperFactory.Helper.sendEntry(map, entryName, zipOutputStream, comparator);
                }
                map = new HashMap<String, Count>();
                currentIdentifier = identifier;
            }
            
            try {
                List<String> tokenList = tokenPackage.getTokenList();
                int size = tokenList.size();
                if (size > 0) {
                    if (hyphenedLastWord != null) {
                        String pendingFullWord = hyphenedLastWord + tokenList.get(0);
                        TokenCountZipperFactory.Helper.countToken(pendingFullWord, map);
                        hyphenedLastWord = null;
                    } else {
                        TokenCountZipperFactory.Helper.countToken(tokenList.get(0), map);
                    }
                }
                
                for (int i = 1; i < size - 1; i++) {
                    TokenCountZipperFactory.Helper.countToken(tokenList.get(i), map);
                }
                
                if (size > 1) {
                    String string = tokenList.get(size - 1);
                    if (string.endsWith(Tokenizer.HYPHEN)) {
                        hyphenedLastWord = string;
                    } else {
                        TokenCountZipperFactory.Helper.countToken(string, map);
                    }
                }
            } catch (DataAPIException e) {
                exceptionList.add(e);
            }
        }
        
        // wrap up what's remaining
        if (currentIdentifier != null) {
            if (hyphenedLastWord != null) {  
                TokenCountZipperFactory.Helper.countToken(hyphenedLastWord, map);
                hyphenedLastWord = null;
            }
            String entryName = currentIdentifier.getPrefix() + "." + pairtree.cleanId(currentIdentifier.getHeadlessID()) + ".count";
            TokenCountZipperFactory.Helper.sendEntry(map, entryName, zipOutputStream, comparator);
        }
        
        if (!exceptionList.isEmpty()) {
            ZipMakerFactory.Helper.injectErrorEntry(zipOutputStream, false, exceptionList);
        }

        zipOutputStream.close();
    }
    
}


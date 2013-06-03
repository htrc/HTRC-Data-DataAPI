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
# File:  SimpleTokenizerTest.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Test;

import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.DataAPIException;

/**
 * @author Yiming Sun
 *
 */
public class SimpleTokenizerTest {
    
    // this case tests that the SimpleTokenizer can properly tokenize the data from the TestTokenCountVolumeRetrieverImpl
    @Test
    public void testTokenize1() throws DataAPIException {
        
        VolumeRetriever volumeRetriever = new TestTokenCountVolumeRetrieverImpl();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Tokenizer tokenizer = new SimpleTokenizer(executorService, new TestTokenCountParameterContainerImpl());
        Iterator<TokenPackage> actualIterator = tokenizer.tokenize(volumeRetriever);
        
        List<TokenPackage> expectedTokenPackages = new LinkedList<TokenPackage>();
        expectedTokenPackages.add(createExpectedTokenPackage1());
        expectedTokenPackages.add(createExpectedTokenPackage2());
        expectedTokenPackages.add(createExpectedTokenPackage3());
        expectedTokenPackages.add(createExpectedTokenPackage4());
        expectedTokenPackages.add(createExpectedTokenPackage5());
        
        Iterator<TokenPackage> expectedIterator = expectedTokenPackages.iterator();
        
        try {
            while (expectedIterator.hasNext()) {
                Assert.assertTrue(actualIterator.hasNext());
                
                TokenPackage expectedTokenPackage = expectedIterator.next();
                TokenPackage actualTokenPackage = actualIterator.next();
                
                ContentIdentifier expectedIdentifier = expectedTokenPackage.getContentIdentifier();
                ContentIdentifier actualIdentifier = actualTokenPackage.getContentIdentifier();
                
                Assert.assertEquals(expectedIdentifier.getVolumeID(), actualIdentifier.getVolumeID());
                Assert.assertEquals(expectedIdentifier.getPageSequenceID(), actualIdentifier.getPageSequenceID());
                
                List<String> expectedTokenList = expectedTokenPackage.getTokenList();
                List<String> actualTokenList = actualTokenPackage.getTokenList();
                
                Assert.assertEquals(expectedTokenList.size(), actualTokenList.size());
                
                for (int i = 0; i < expectedTokenList.size(); i++) {
                    Assert.assertEquals(expectedTokenList.get(i), actualTokenList.get(i));
                }
            }
            
            Assert.assertEquals(expectedIterator.hasNext(), actualIterator.hasNext());
            
        } finally {
            executorService.shutdown();
        }
        
    }
    
    private TokenPackage createExpectedTokenPackage1(){
        ContentIdentifier contentIdentifier = new ContentIdentifierImpl("test.volume1", "00000001");
        String[] tokens = {"line", "without", "hyphen.", "line", "ends", "with", "hy-phen", "and", "continues."};
        TokenPackage tokenPackage = new SimpleTokenPackageImpl(contentIdentifier, Arrays.asList(tokens));
        return tokenPackage;
    }

    private TokenPackage createExpectedTokenPackage2(){
        ContentIdentifier contentIdentifier = new ContentIdentifierImpl("test.volume1", "00000002");
        String[] tokens = {"this", "page", "is", "about", "hyphen", "at", "end", "of", "page", "such", "as", "hy-"};
        TokenPackage tokenPackage = new SimpleTokenPackageImpl(contentIdentifier, Arrays.asList(tokens));
        return tokenPackage;
    }
    

    private TokenPackage createExpectedTokenPackage3(){
        ContentIdentifier contentIdentifier = new ContentIdentifierImpl("test.volume1", "00000003");
        String[] tokens = {"phen", "and", "continues."};
        TokenPackage tokenPackage = new SimpleTokenPackageImpl(contentIdentifier, Arrays.asList(tokens));
        return tokenPackage;
    }

    
    private TokenPackage createExpectedTokenPackage4(){
        ContentIdentifier contentIdentifier = new ContentIdentifierImpl("test.volume2", "00000001");
        String[] tokens = {"first", "line", "in", "second", "volume", "com-"};
        TokenPackage tokenPackage = new SimpleTokenPackageImpl(contentIdentifier, Arrays.asList(tokens));
        return tokenPackage;
    }


    private TokenPackage createExpectedTokenPackage5(){
        ContentIdentifier contentIdentifier = new ContentIdentifierImpl("test.volume2", "00000002");
        String[] tokens = {"munication", "media", "is", "good", "for", "com-munication"};
        TokenPackage tokenPackage = new SimpleTokenPackageImpl(contentIdentifier, Arrays.asList(tokens));
        return tokenPackage;
    }

}


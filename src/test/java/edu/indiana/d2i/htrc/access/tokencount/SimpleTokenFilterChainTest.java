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
# File:  SimpleTokenFilterChainTest.java
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
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import edu.indiana.d2i.htrc.access.exception.DataAPIException;

/**
 * @author Yiming Sun
 *
 */
public class SimpleTokenFilterChainTest {

    private static class TestFilter1 implements TokenFilter {

        /**
         * @see edu.indiana.d2i.htrc.access.tokencount.TokenFilter#filter(edu.indiana.d2i.htrc.access.tokencount.TokenPackage)
         */
        @Override
        public TokenPackage filter(TokenPackage tokenPackage) {
            TokenPackage newTokenPackage = null;
            try {
                List<String> tokenList = tokenPackage.getTokenList();
                List<String> newTokenList = new LinkedList<String>();
                
                for (String token : tokenList) {
                    newTokenList.add(token.substring(1, token.length()) + token.substring(0, 1));
                }
                
               newTokenPackage = new SimpleTokenPackageImpl(tokenPackage.getContentIdentifier(), newTokenList);
            } catch (DataAPIException e) {
                
            }
            
            return newTokenPackage;
        }
        
    }
    
    private static class TestFilter2 implements TokenFilter {

        /**
         * @see edu.indiana.d2i.htrc.access.tokencount.TokenFilter#filter(edu.indiana.d2i.htrc.access.tokencount.TokenPackage)
         */
        @Override
        public TokenPackage filter(TokenPackage tokenPackage) {
            TokenPackage newTokenPackage = null;
            try {
                List<String> tokenList = tokenPackage.getTokenList();
                List<String> newTokenList = new LinkedList<String>();
                
                for (String token : tokenList) {
                    newTokenList.add(token + "ay");
                }
                
                newTokenPackage = new SimpleTokenPackageImpl(tokenPackage.getContentIdentifier(), newTokenList);
            } catch (DataAPIException e) {
                
            }
            
            return newTokenPackage;
        }
    }
    
    // this case tests the proper functionality of a SimpleTokenFilterChainImpl. the 2 test TokenFilter objects in the chain will pig-latinize a list of tokens
    @Test
    public void testChainFilter1() throws DataAPIException {
        TokenFilterChain chain = new SimpleTokenFilterChain();
        
        chain.addFilter(new TestFilter1());
        chain.addFilter(new TestFilter2());
        
        String[] tokens = {"this", "line", "to", "be", "pig", "latinized", "i"};
        List<String> tokenList = Arrays.asList(tokens);
        
        ContentIdentifier contentIdentifier = new ContentIdentifierImpl("test.012345", "00000001");
        TokenPackage tokenPackage = new SimpleTokenPackageImpl(contentIdentifier, tokenList);
        
        TokenPackage filteredTokenPackage = chain.filter(tokenPackage);
        
        final String expectedVolumeID = "test.012345";
        final String expectedPageSequenceID = "00000001";
        final String[] expectedTokens = {"histay", "inelay", "otay", "ebay", "igpay", "atinizedlay", "iay"};
        
        ContentIdentifier filteredIdentifier = filteredTokenPackage.getContentIdentifier();
        
        final String actualVolumeID = filteredIdentifier.getVolumeID();
        final String actualPageSequenceID = filteredIdentifier.getPageSequenceID();
        final String[] actualTokens = filteredTokenPackage.getTokenList().toArray(new String[0]);
        
        Assert.assertEquals(expectedVolumeID, actualVolumeID);
        Assert.assertEquals(expectedPageSequenceID, actualPageSequenceID);
        Assert.assertEquals(expectedTokens.length, actualTokens.length);
        
        for (int i = 0; i < expectedTokens.length; i++) {
            Assert.assertEquals(expectedTokens[i], actualTokens[i]);
        }
        
    }
}


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
# File:  EmptyTokenRemovalFilterTest.java
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
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import edu.indiana.d2i.htrc.access.exception.DataAPIException;

/**
 * @author Yiming Sun
 *
 */
public class EmptyTokenRemovalFilterTest {
    
    // this case tests that the EmptyTokenRemovalFilter removes empty tokens from a token package
    @Test
    public void testFilter1() throws DataAPIException {
        ContentIdentifier contentIdentifier = new ContentIdentifierImpl("test.012345", "00000001");
        String[] tokens = {"token", "list", "", "contains", "some", "", "", "empty", "tokens"};
        List<String> tokenList = Arrays.asList(tokens);
        
        TokenPackage tokenPackage = new SimpleTokenPackageImpl(contentIdentifier, tokenList);
        
        TokenFilter tokenFilter = new EmptyTokenRemovalFilter();
        TokenPackage filteredTokenPackage = tokenFilter.filter(tokenPackage);
        
        ContentIdentifier filteredContentIdentifier = filteredTokenPackage.getContentIdentifier();
        List<String> filteredTokenList = filteredTokenPackage.getTokenList();
        
        String expectedVolumeID = "test.012345";
        String expectedPageSequenceID = "00000001";
        
        String[] expectedTokens = {"token", "list", "contains", "some", "empty", "tokens"};
        
        String actualVolumeID = filteredContentIdentifier.getVolumeID();
        String actualPageSequenceID = filteredContentIdentifier.getPageSequenceID();
        
        String[] actualTokens = filteredTokenList.toArray(new String[0]);
        
        Assert.assertEquals(expectedVolumeID, actualVolumeID);
        Assert.assertEquals(expectedPageSequenceID, actualPageSequenceID);
        Assert.assertEquals(expectedTokens.length, actualTokens.length);
        for (int i = 0; i < expectedTokens.length; i++) {
            Assert.assertEquals(expectedTokens[i], actualTokens[i]);
        }
        
    }

}


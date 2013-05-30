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
# File:  TokenCountComparatorFactoryTest.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.util.Comparator;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Test;

import edu.indiana.d2i.htrc.access.tokencount.TokenCountComparatorFactory.TokenCountComparatorTypeEnum;

/**
 * @author Yiming Sun
 *
 */
public class TokenCountComparatorFactoryTest {
    
    // this case tests that the factory returns a TokenLexAscendingComparator when the enum TOKEN_LEX_ASC is passed
    @Test
    public void testGetTokenLexAscendingComparator() {
        Comparator<Entry<String, Count>> comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_LEX_ASC);
        Assert.assertTrue(comparator instanceof TokenCountComparatorFactory.TokenLexAscendingComparator);
    }

    // this case tests that the factory returns a TokenLexDescendingComparator when the enum TOKEN_LEX_DESC is passed
    @Test
    public void testGetTokenLexDescendingComparator() {
        Comparator<Entry<String, Count>> comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_LEX_DESC);
        Assert.assertTrue(comparator instanceof TokenCountComparatorFactory.TokenLexDescendingComparator);
    }

    // this case tests that the factory returns a TokenCountAscendingComparator when the enum TOKEN_COUNT_ASC is passed
    @Test
    public void testGetTokenCountAsccendingComparator() {
        Comparator<Entry<String, Count>> comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_COUNT_ASC);
        Assert.assertTrue(comparator instanceof TokenCountComparatorFactory.TokenCountAscendingComparator);
    }

    // this case tests that the factory returns a TokenCountDescendingComparator when the enum TOKEN_COUNT_DESC is passed
    @Test
    public void testGetTokenCountDesccendingComparator() {
        Comparator<Entry<String, Count>> comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_COUNT_DESC);
        Assert.assertTrue(comparator instanceof TokenCountComparatorFactory.TokenCountDescendingComparator);
    }

    // this case tests that the factory returns a NullTokenCountComparator when the enum DEFAULT is passed
    @Test
    public void testGetNullTokenCountComparator() {
        Comparator<Entry<String, Count>> comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.DEFAULT);
        Assert.assertTrue(comparator instanceof TokenCountComparatorFactory.NullTokenCountComparator);
    }

}


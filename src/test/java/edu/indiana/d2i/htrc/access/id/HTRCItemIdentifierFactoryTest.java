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
# File:  TestHTRCItemIdentifierFactory.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.id;

import junit.framework.Assert;

import org.junit.Test;

import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser;

/**
 * @author Yiming Sun
 *
 */
public class HTRCItemIdentifierFactoryTest {
    
    @Test
    public void testGeneratePageSequenceString() {
        int pageSeq = 1;
        String expectedPageSequence = "00000001";
        
        String pageSequence = Parser.generatePageSequenceString(pageSeq);
        
        Assert.assertEquals(expectedPageSequence, pageSequence);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGeneratePageSequenceStringError() {
        int pageSeq = 0;
        String pageSequence = Parser.generatePageSequenceString(pageSeq);
    }
}


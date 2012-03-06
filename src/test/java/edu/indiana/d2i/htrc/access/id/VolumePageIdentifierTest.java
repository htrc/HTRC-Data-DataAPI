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
# File:  VolumePageIdentifierTest.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.id;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Yiming Sun
 *
 */
public class VolumePageIdentifierTest {

    // This case tests that a VolumePageIdentifier should output its page sequences sorted in ascending order
    @Test
    public void testGetPageSequences1() {
        String[] inputString = new String[] {"00029340", "00021022","99999999", "00000099", "34521334" };
        
        String[] expectedString = new String[] {"00000099", "00021022", "00029340", "34521334", "99999999" };
        
        VolumePageIdentifier id = new VolumePageIdentifier("test.volume/page/identifier");
        for (int i = 0; i < 5; i++) {
            id.addPageSequence(inputString[i]);
        }
        
        String[] actualString = id.getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(expectedString, actualString);
    }
    
    // This case tests that VolumePageIdentifier should coalesce redundant page sequences into one corrance and sort page sequences in ascending order
    @Test
    public void testGetPageSequences2() {
        String[] inputString = new String[] {"99999999","00029340", "00021022","00029340", "99999999", "00000099", "34521334" };
        
        String[] expectedString = new String[] {"00000099", "00021022", "00029340", "34521334", "99999999" };
        
        VolumePageIdentifier id = new VolumePageIdentifier("test.volume/page/identifier");
        for (int i = 0; i < inputString.length; i++) {
            id.addPageSequence(inputString[i]);
        }
        
        String[] actualString = id.getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(expectedString, actualString);
    }
    

}


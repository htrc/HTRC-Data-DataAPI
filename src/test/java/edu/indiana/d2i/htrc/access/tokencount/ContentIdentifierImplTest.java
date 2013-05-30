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
# File:  ContentIdentifierImplTest.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Yiming Sun
 *
 */
public class ContentIdentifierImplTest {
    // This case tests a content identifier without page sequence id returns the proper parts of the volumeID, and null for page sequence
    @Test
    public void testGetAllParts1() {
        final String volumeID = "test.012345";
        final String expectedVolumeID = new String(volumeID);
        final String expectedPrefix = "test";
        final String expectedHeadlessID = "012345";
        final String expectedPageSequenceID = null;
        
        ContentIdentifier contentIdentifier = new ContentIdentifierImpl(volumeID);
        
        final String actualVolumeID = contentIdentifier.getVolumeID();
        final String actualPrefix = contentIdentifier.getPrefix();
        final String actualHeadlessID = contentIdentifier.getHeadlessID();
        final String actualPageSequenceID = contentIdentifier.getPageSequenceID();
        
        Assert.assertEquals(expectedVolumeID, actualVolumeID);
        Assert.assertEquals(expectedPrefix, actualPrefix);
        Assert.assertEquals(expectedHeadlessID, actualHeadlessID);
        Assert.assertEquals(expectedPageSequenceID, actualPageSequenceID);
    }

    // This case tests a content identifier with page sequence id returns the proper parts of everything
    @Test
    public void testGetAllParts2() {
        final String volumeID = "test.012345";
        final String pageSequenceID = "00000007";
        
        final String expectedVolumeID = new String(volumeID);
        final String expectedPrefix = "test";
        final String expectedHeadlessID = "012345";
        final String expectedPageSequenceID = new String(pageSequenceID);
        
        ContentIdentifier contentIdentifier = new ContentIdentifierImpl(volumeID, pageSequenceID);
        
        final String actualVolumeID = contentIdentifier.getVolumeID();
        final String actualPrefix = contentIdentifier.getPrefix();
        final String actualHeadlessID = contentIdentifier.getHeadlessID();
        final String actualPageSequenceID = contentIdentifier.getPageSequenceID();
        
        Assert.assertEquals(expectedVolumeID, actualVolumeID);
        Assert.assertEquals(expectedPrefix, actualPrefix);
        Assert.assertEquals(expectedHeadlessID, actualHeadlessID);
        Assert.assertEquals(expectedPageSequenceID, actualPageSequenceID);
    }
}


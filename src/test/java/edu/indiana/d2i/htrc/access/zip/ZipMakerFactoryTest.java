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
# File:  ZipMakerFactoryTest.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import edu.indiana.d2i.htrc.access.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.ZipMaker;
import edu.indiana.d2i.htrc.access.read.ExceptionalVolumeRetriever;
import edu.indiana.d2i.htrc.access.read.TestVolumeRetriever;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory.ZipTypeEnum;


/**
 * @author Yiming Sun
 *
 */
public class ZipMakerFactoryTest {
    
    
    @Test
    public void testSeparatePageZipMaker() throws IOException, KeyNotFoundException {
        VolumeRetriever volumeRetriever = new TestVolumeRetriever();
        byte[] expected = MakeZipUtility.getSeparatePageZipAsByteArray(volumeRetriever);
        
        VolumeRetriever volumeRetriever2 = new TestVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.SEPARATE_PAGE);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        
        zipMaker.makeZipFile(actual, volumeRetriever2);
     
        Assert.assertArrayEquals(expected, actual.toByteArray());
    }
    
    
    @Test
    public void testCombinePageZipMaker() throws IOException, KeyNotFoundException {
        VolumeRetriever volumeRetriever = new TestVolumeRetriever();
        byte[] expected = MakeZipUtility.getCombinePageZipByteArray(volumeRetriever);
        
        VolumeRetriever volumeRetriever2 = new TestVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.COMBINE_PAGE);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        
        zipMaker.makeZipFile(actual, volumeRetriever2);
     
        Assert.assertArrayEquals(expected, actual.toByteArray());
    }
    
    
    @Test
    public void testWordBagPageZipMaker() throws IOException, KeyNotFoundException {
        VolumeRetriever volumeRetriever = new TestVolumeRetriever();
        byte[] expected = MakeZipUtility.getWordBagZipByteArray(volumeRetriever);
        
        VolumeRetriever volumeRetriever2 = new TestVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.WORD_BAG);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        
        zipMaker.makeZipFile(actual, volumeRetriever2);
     
        Assert.assertArrayEquals(expected, actual.toByteArray());
    }
    
    @Test(expected = KeyNotFoundException.class)
    public void testSeparatePageZipMakerError() throws IOException, KeyNotFoundException {
        VolumeRetriever volumeRetriever = new ExceptionalVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.SEPARATE_PAGE);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        
        zipMaker.makeZipFile(actual, volumeRetriever);
        
    }
    

    @Test(expected = KeyNotFoundException.class)
    public void testCombinePageZipMakerError() throws IOException, KeyNotFoundException {
        VolumeRetriever volumeRetriever = new ExceptionalVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.COMBINE_PAGE);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        
        zipMaker.makeZipFile(actual, volumeRetriever);
        
    }

    @Test(expected = KeyNotFoundException.class)
    public void testWordBagPageZipMakerError() throws IOException, KeyNotFoundException {
        VolumeRetriever volumeRetriever = new ExceptionalVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.WORD_BAG);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        
        zipMaker.makeZipFile(actual, volumeRetriever);
        
    }
}


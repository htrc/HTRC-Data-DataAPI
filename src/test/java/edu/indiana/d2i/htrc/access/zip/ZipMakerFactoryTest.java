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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Test;

import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.ZipMaker;
import edu.indiana.d2i.htrc.access.exception.DataAPIException;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.access.read.ExceptionalVolumeRetriever;
import edu.indiana.d2i.htrc.access.read.TestVolumeRetriever;
import edu.indiana.d2i.htrc.access.zip.ZipMakerFactory.ZipTypeEnum;
import edu.indiana.d2i.htrc.audit.NullAuditor;


/**
 * @author Yiming Sun
 *
 */
public class ZipMakerFactoryTest {
    
    // This case tests that SeparatePageZipMaker properly generates a zip file with each volume as a directory, and each page belonging to the volume a separate text file under the directory
    @Test
    public void testSeparatePageZipMaker() throws IOException, KeyNotFoundException, PolicyViolationException, RepositoryException, DataAPIException, Exception {
        VolumeRetriever volumeRetriever = new TestVolumeRetriever();
        byte[] expected = MakeZipUtility.getSeparatePageZipAsByteArray(volumeRetriever);
        
        VolumeRetriever volumeRetriever2 = new TestVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.SEPARATE_PAGE, new NullAuditor(null));
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        
        zipMaker.makeZipFile(actual, volumeRetriever2);
     
        Assert.assertArrayEquals(expected, actual.toByteArray());
    }
    
    // This case tests that CombinePageZipMaker properly generates a zip file with all pages of each volume concatenated into a single text file for that volume
    @Test
    public void testCombinePageZipMaker() throws IOException, KeyNotFoundException, PolicyViolationException, RepositoryException, DataAPIException, Exception {
        VolumeRetriever volumeRetriever = new TestVolumeRetriever();
        byte[] expected = MakeZipUtility.getCombinePageZipByteArray(volumeRetriever);
        
        VolumeRetriever volumeRetriever2 = new TestVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.COMBINE_PAGE, new NullAuditor(null));
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        
        zipMaker.makeZipFile(actual, volumeRetriever2);
     
        Assert.assertArrayEquals(expected, actual.toByteArray());
    }
    
    // This case tests that WordBagPageZipMaker properly generates a zip file with all pages from all volumes concatenated into a single "bag of words" text file
    @Test
    public void testWordSequencePageZipMaker() throws IOException, KeyNotFoundException, PolicyViolationException, RepositoryException, DataAPIException, Exception {
        VolumeRetriever volumeRetriever = new TestVolumeRetriever();
        byte[] expected = MakeZipUtility.getWordSequenceZipByteArray(volumeRetriever);
        
        VolumeRetriever volumeRetriever2 = new TestVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.WORD_SEQUENCE, new NullAuditor(null));
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        
        zipMaker.makeZipFile(actual, volumeRetriever2);
     
        Assert.assertArrayEquals(expected, actual.toByteArray());
    }
    
    // This case tests that SeparatePageZipMaker should add an ERROR.err entry to the zip file when VolumeRetriever throws an exception
    @Test
    public void testSeparatePageZipMakerErrorEntry() throws IOException, DataAPIException {

        boolean hasErrorEntry = false;

        VolumeRetriever volumeRetriever = new ExceptionalVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.SEPARATE_PAGE, new NullAuditor(null));
        ByteArrayOutputStream actual = new ByteArrayOutputStream();

        zipMaker.makeZipFile(actual, volumeRetriever);


        ByteArrayInputStream inputStream = new ByteArrayInputStream(actual.toByteArray());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        do {
            zipEntry = zipInputStream.getNextEntry();
            if (zipEntry != null) {
                String name = zipEntry.getName();
                zipInputStream.closeEntry();
                if ("ERROR.err".equals(name)) {
                    hasErrorEntry = true;
                }
            }
        } while (zipEntry != null);
        zipInputStream.close();

        Assert.assertEquals(true, hasErrorEntry);
    }
    

    
    // This case tests that CombinePageZipMaker should add an ERROR.err entry to the zip file when VolumeRetriever throws an exception
    @Test
    public void testCombinePageZipMakerErrorEntry() throws IOException, DataAPIException {
        
        boolean hasErrorEntry = false;

        VolumeRetriever volumeRetriever = new ExceptionalVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.COMBINE_PAGE, new NullAuditor(null));
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        zipMaker.makeZipFile(actual, volumeRetriever);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(actual.toByteArray());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        do {
            zipEntry = zipInputStream.getNextEntry();
            if (zipEntry != null) {
                String name = zipEntry.getName();
                zipInputStream.closeEntry();
                if ("ERROR.err".equals(name)) {
                    hasErrorEntry = true;
                }
            }
        } while (zipEntry != null);
        zipInputStream.close();

        Assert.assertEquals(true, hasErrorEntry);
    }
    

    // This case tests that WordBagZipMaker should add an ERROR.err entry to the zip file when VolumeRetriever throws an exception
    @Test
    public void testWordSequenceZipMakerErrorEntry() throws IOException, DataAPIException {

        boolean hasErrorEntry = false;

        VolumeRetriever volumeRetriever = new ExceptionalVolumeRetriever();
        ZipMaker zipMaker = ZipMakerFactory.newInstance(ZipTypeEnum.WORD_SEQUENCE, new NullAuditor(null));
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        zipMaker.makeZipFile(actual, volumeRetriever);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(actual.toByteArray());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        do {
            zipEntry = zipInputStream.getNextEntry();
            if (zipEntry != null) {
                String name = zipEntry.getName();
                zipInputStream.closeEntry();
                if ("ERROR.err".equals(name)) {
                    hasErrorEntry = true;
                }
            }
        } while (zipEntry != null);
        zipInputStream.close();

        Assert.assertEquals(true, hasErrorEntry);
    }
}


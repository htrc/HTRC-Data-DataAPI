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
# File:  PageTokenCountZipperTest.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;

import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.tokencount.TokenCountComparatorFactory.TokenCountComparatorTypeEnum;
import edu.indiana.d2i.htrc.audit.Auditor;
import edu.indiana.d2i.htrc.audit.NullAuditor;

/**
 * @author Yiming Sun
 *
 */
public class PageTokenCountZipperTest {
    
    // this case tests the token count and zip at page level using token lexical ascending order comparator
    @Test
    public void testCountAndZip1() throws IOException {
        Auditor auditor = new NullAuditor(new HashMap<String, List<String>>());
        VolumeRetriever volumeRetriever = new TestTokenCountVolumeRetrieverImpl();
        ByteArrayOutputStream actualOutputStream = new ByteArrayOutputStream();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Tokenizer tokenizer = new SimpleTokenizer(executorService);
        TokenFilter tokenFilter = new SimpleTokenFilterChain();
        Comparator<Entry<String, Count>> comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_LEX_ASC);
        
        TokenCountZipper tokenCountZipper = new PageTokenCountZipper(auditor);
        
        try {
            tokenCountZipper.countAndZip(actualOutputStream, volumeRetriever, tokenizer, tokenFilter, comparator);
            
            ByteArrayOutputStream expectedOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(expectedOutputStream);
            zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
            
            ZipEntry zipEntry = new ZipEntry("test.volume1/00000001.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[] {"and 1", "continues. 1", "ends 1", "hy-phen 1", "hyphen. 1", "line 2", "with 1", "without 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume1/00000002.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"about 1", "as 1", "at 1", "end 1", "hy- 1", "hyphen 1", "is 1", "of 1", "page 2", "such 1", "this 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume1/00000003.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[] {"and 1", "continues. 1", "phen 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume2/00000001.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"com- 1", "first 1", "in 1", "line 1", "second 1", "volume 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume2/00000002.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"com-munication 1", "for 1", "good 1", "is 1", "media 1", "munication 1"});
            zipOutputStream.closeEntry();
            
            zipOutputStream.close();
            
//            FileOutputStream fileOutputStream = new FileOutputStream("d:\\temp\\actual.zip");
//            fileOutputStream.write(actualOutputStream.toByteArray());
//            fileOutputStream.close();
//            
//            fileOutputStream = new FileOutputStream("d:\\temp\\expected.zip");
//            fileOutputStream.write(expectedOutputStream.toByteArray());
//            fileOutputStream.close();
            
            Assert.assertArrayEquals(expectedOutputStream.toByteArray(), actualOutputStream.toByteArray());
            
        } finally {
            executorService.shutdown();
        }
    }
    
    // this case tests the token count and zip at page level using token lexical descending order comparator
    @Test
    public void testCountAndZip2() throws IOException {
        Auditor auditor = new NullAuditor(new HashMap<String, List<String>>());
        VolumeRetriever volumeRetriever = new TestTokenCountVolumeRetrieverImpl();
        ByteArrayOutputStream actualOutputStream = new ByteArrayOutputStream();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Tokenizer tokenizer = new SimpleTokenizer(executorService);
        TokenFilter tokenFilter = new SimpleTokenFilterChain();
        Comparator<Entry<String, Count>> comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_LEX_DESC);
        
        TokenCountZipper tokenCountZipper = new PageTokenCountZipper(auditor);
        
        try {
            tokenCountZipper.countAndZip(actualOutputStream, volumeRetriever, tokenizer, tokenFilter, comparator);
            
            ByteArrayOutputStream expectedOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(expectedOutputStream);
            zipOutputStream.setLevel(0);
            
            ZipEntry zipEntry = new ZipEntry("test.volume1/00000001.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[] {"without 1", "with 1", "line 2", "hyphen. 1", "hy-phen 1", "ends 1", "continues. 1","and 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume1/00000002.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"this 1", "such 1", "page 2", "of 1", "is 1", "hyphen 1", "hy- 1", "end 1", "at 1", "as 1", "about 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume1/00000003.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[] {"phen 1", "continues. 1", "and 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume2/00000001.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"volume 1", "second 1", "line 1", "in 1", "first 1", "com- 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume2/00000002.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"munication 1", "media 1", "is 1", "good 1", "for 1", "com-munication 1"});
            zipOutputStream.closeEntry();
            
            zipOutputStream.close();
            
//            FileOutputStream fileOutputStream = new FileOutputStream("d:\\temp\\actual.zip");
//            fileOutputStream.write(actualOutputStream.toByteArray());
//            fileOutputStream.close();
//            
//            fileOutputStream = new FileOutputStream("d:\\temp\\expected.zip");
//            fileOutputStream.write(expectedOutputStream.toByteArray());
//            fileOutputStream.close();
            
            Assert.assertArrayEquals(expectedOutputStream.toByteArray(), actualOutputStream.toByteArray());
            
        } finally {
            executorService.shutdown();
        }
    }
    
    // this case tests the token count and zip at page level using token count ascending order comparator
    @Test
    public void testCountAndZip3() throws IOException {
        Auditor auditor = new NullAuditor(new HashMap<String, List<String>>());
        VolumeRetriever volumeRetriever = new TestTokenCountVolumeRetrieverImpl();
        ByteArrayOutputStream actualOutputStream = new ByteArrayOutputStream();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Tokenizer tokenizer = new SimpleTokenizer(executorService);
        TokenFilter tokenFilter = new SimpleTokenFilterChain();
        Comparator<Entry<String, Count>> comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_COUNT_ASC);
        
        TokenCountZipper tokenCountZipper = new PageTokenCountZipper(auditor);
        
        try {
            tokenCountZipper.countAndZip(actualOutputStream, volumeRetriever, tokenizer, tokenFilter, comparator);
            
            ByteArrayOutputStream expectedOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(expectedOutputStream);
            zipOutputStream.setLevel(0);
            
            ZipEntry zipEntry = new ZipEntry("test.volume1/00000001.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[] {"and 1", "continues. 1", "ends 1", "hy-phen 1", "hyphen. 1", "with 1", "without 1", "line 2", });
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume1/00000002.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"about 1", "as 1", "at 1", "end 1", "hy- 1", "hyphen 1", "is 1", "of 1", "such 1", "this 1", "page 2"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume1/00000003.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[] {"and 1", "continues. 1", "phen 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume2/00000001.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"com- 1", "first 1", "in 1", "line 1", "second 1", "volume 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume2/00000002.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"com-munication 1", "for 1", "good 1", "is 1", "media 1", "munication 1"});
            zipOutputStream.closeEntry();
            
            zipOutputStream.close();
            
//            FileOutputStream fileOutputStream = new FileOutputStream("d:\\temp\\actual.zip");
//            fileOutputStream.write(actualOutputStream.toByteArray());
//            fileOutputStream.close();
//            
//            fileOutputStream = new FileOutputStream("d:\\temp\\expected.zip");
//            fileOutputStream.write(expectedOutputStream.toByteArray());
//            fileOutputStream.close();
            
            Assert.assertArrayEquals(expectedOutputStream.toByteArray(), actualOutputStream.toByteArray());
            
        } finally {
            executorService.shutdown();
        }
    }

    
    // this case tests the token count and zip at page level using token count descending order comparator
    @Test
    public void testCountAndZip4() throws IOException {
        Auditor auditor = new NullAuditor(new HashMap<String, List<String>>());
        VolumeRetriever volumeRetriever = new TestTokenCountVolumeRetrieverImpl();
        ByteArrayOutputStream actualOutputStream = new ByteArrayOutputStream();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Tokenizer tokenizer = new SimpleTokenizer(executorService);
        TokenFilter tokenFilter = new SimpleTokenFilterChain();
        Comparator<Entry<String, Count>> comparator = TokenCountComparatorFactory.getComparator(TokenCountComparatorTypeEnum.TOKEN_COUNT_DESC);
        
        TokenCountZipper tokenCountZipper = new PageTokenCountZipper(auditor);
        
        try {
            tokenCountZipper.countAndZip(actualOutputStream, volumeRetriever, tokenizer, tokenFilter, comparator);
            
            ByteArrayOutputStream expectedOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(expectedOutputStream);
            zipOutputStream.setLevel(0);
            
            ZipEntry zipEntry = new ZipEntry("test.volume1/00000001.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[] {"line 2", "without 1", "with 1", "hyphen. 1", "hy-phen 1", "ends 1", "continues. 1","and 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume1/00000002.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"page 2", "this 1", "such 1", "of 1", "is 1", "hyphen 1", "hy- 1", "end 1", "at 1", "as 1", "about 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume1/00000003.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[] {"phen 1", "continues. 1", "and 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume2/00000001.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"volume 1", "second 1", "line 1", "in 1", "first 1", "com- 1"});
            zipOutputStream.closeEntry();
            
            zipEntry = new ZipEntry("test.volume2/00000002.count");
            zipOutputStream.putNextEntry(zipEntry);
            writeZipContent(zipOutputStream, new String[]{"munication 1", "media 1", "is 1", "good 1", "for 1", "com-munication 1"});
            zipOutputStream.closeEntry();
            
            zipOutputStream.close();
            
//            FileOutputStream fileOutputStream = new FileOutputStream("d:\\temp\\actual.zip");
//            fileOutputStream.write(actualOutputStream.toByteArray());
//            fileOutputStream.close();
//            
//            fileOutputStream = new FileOutputStream("d:\\temp\\expected.zip");
//            fileOutputStream.write(expectedOutputStream.toByteArray());
//            fileOutputStream.close();
            
            Assert.assertArrayEquals(expectedOutputStream.toByteArray(), actualOutputStream.toByteArray());
            
        } finally {
            executorService.shutdown();
        }
    }
    
    // it would be difficult to test the NullTokenCountComparator because the order of the token placement in a Map is indeterministic 
    
    
    private void writeZipContent(ZipOutputStream zipOutputStream, String[] tokenCounts) throws IOException {
        for (String tokenCount : tokenCounts) {
            zipOutputStream.write(tokenCount.getBytes(TokenCountZipperFactory.Helper.UTF_8));
            zipOutputStream.write(System.getProperty("line.separator").getBytes(TokenCountZipperFactory.Helper.UTF_8));
        }
    }

}


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
# File:  TestVolumeReaderImplForTokenCountTest.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.ContentReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import gov.loc.repository.pairtree.Pairtree;

/**
 * @author Yiming Sun
 *
 */
public class TestTokenCountVolumeRetrieverImpl implements VolumeRetriever {
    
    private static class TestVolumeReaderImpl implements VolumeReader {

        private List<ContentReader> pageReaders;
        private Iterator<ContentReader> pageReaderIterator;
        private final String volumeID;
        private final String cleanedVolumeID;
        
        TestVolumeReaderImpl(String volumeID) {
            this.volumeID = volumeID;
            Pairtree pairtree = new Pairtree();
            int index = volumeID.indexOf('.');
            this.cleanedVolumeID = volumeID.substring(0, index + 1) + pairtree.cleanId(volumeID.substring(index + 1));
            this.pageReaders = null;
            this.pageReaderIterator = null;
        }
        
        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#getVolumeID()
         */
        @Override
        public String getVolumeID() {
            return this.volumeID;
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#getPairtreeCleanedVolumeID()
         */
        @Override
        public String getPairtreeCleanedVolumeID() {
            return this.cleanedVolumeID;
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#nextPage()
         */
        @Override
        public ContentReader nextPage() {
            return pageReaderIterator.next();
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#hasMorePages()
         */
        @Override
        public boolean hasMorePages() {
            return this.pageReaderIterator.hasNext();
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#setPages(java.util.List)
         */
        @Override
        public void setPages(List<ContentReader> pageReaders) {
            this.pageReaders = pageReaders;
            this.pageReaderIterator = pageReaders.iterator();
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#nextMetadata()
         */
        @Override
        public ContentReader nextMetadata() {
            return null;
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#hasMoreMetadata()
         */
        @Override
        public boolean hasMoreMetadata() {
            return false;
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#setMetadata(java.util.List)
         */
        @Override
        public void setMetadata(List<ContentReader> metadataReaders) {
            
        }
        
    }
    
    private static class TestContentReaderImpl implements ContentReader {

        private final String contentName;
        private final byte[] content;
        
        TestContentReaderImpl(String contentName, byte[] content) {
            this.contentName = contentName;
            this.content = content;
        }
        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader.ContentReader#getContentName()
         */
        @Override
        public String getContentName() {
            return this.contentName;
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader.ContentReader#getContent()
         */
        @Override
        public byte[] getContent() {
            return this.content;
        }
    }
    
    private List<VolumeReader> volumeReaders = null;
    private Iterator<VolumeReader> volumeReaderIterator = null;
    
    TestTokenCountVolumeRetrieverImpl() {
        volumeReaders = new LinkedList<VolumeReader>();
        volumeReaders.add(createVolumeReader1());
        volumeReaders.add(createVolumeReader2());
        this.volumeReaderIterator = volumeReaders.iterator();
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#hasMoreVolumes()
     */
    @Override
    public boolean hasMoreVolumes() {
        return this.volumeReaderIterator.hasNext();
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#nextVolume()
     */
    @Override
    public VolumeReader nextVolume() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        return this.volumeReaderIterator.next();
    }

    protected VolumeReader createVolumeReader1() {
        List<ContentReader> contentReaders = new LinkedList<ContentReader>();
        ContentReader contentReader = new TestContentReaderImpl("00000001", "line without hyphen.\nline ends with hy-\nphen and continues.".getBytes());
        contentReaders.add(contentReader);
        contentReader = new TestContentReaderImpl("00000002", "this page is\n about hyphen at end of page such as hy-".getBytes());
        contentReaders.add(contentReader);
        contentReader = new TestContentReaderImpl("00000003", "phen and continues.".getBytes());
        contentReaders.add(contentReader);
        
        VolumeReader volumeReader = new TestVolumeReaderImpl("test.volume1");
        volumeReader.setPages(contentReaders);
        
        return volumeReader;
    }
    
    protected VolumeReader createVolumeReader2() {
        List<ContentReader> contentReaders = new LinkedList<ContentReader>();
        ContentReader contentReader = new TestContentReaderImpl("00000001", "first line in second volume\ncom-".getBytes());
        contentReaders.add(contentReader);
        contentReader = new TestContentReaderImpl("00000002", "munication media is good for com-\nmunication".getBytes());
        contentReaders.add(contentReader);
        
        VolumeReader volumeReader = new TestVolumeReaderImpl("test.volume2");
        volumeReader.setPages(contentReaders);
        
        return volumeReader;
    }
    
    
}


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
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  VolumeReaderImpl.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */



/**
 * 
 */
package edu.indiana.d2i.htrc.access.read;

import java.util.List;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.VolumeReader;
import gov.loc.repository.pairtree.Pairtree;

/**
 * @author Yiming Sun
 *
 */
public class VolumeReaderImpl implements VolumeReader {

    public static class ContentReaderImpl implements ContentReader {

        protected final String contentName;
        protected final byte[] content;

        protected ContentReaderImpl(String contentName, byte[] content) {
            this.contentName = contentName;
            this.content = content;
        }
        /**
         * @see edu.indiana.d2i.htrc.access.PageReader#getPageSequence()
         */
        @Override
        public String getContentName() {
            return contentName;
        }

        /**
         * @see edu.indiana.d2i.htrc.access.PageReader#getPageContent()
         */
        @Override
        public byte[] getContent() {
            return content;
        }
    }

    protected final String volumeID;
    protected final String pairtreeCleanedVolumeID;
    protected List<ContentReader> pages;
    protected List<ContentReader> metadata;


    public VolumeReaderImpl(HTRCItemIdentifier identifier) {
        Pairtree pairtree = new Pairtree();
        this.volumeID = identifier.getVolumeID();
        this.pairtreeCleanedVolumeID = getPrefix(volumeID) + "." + pairtree.cleanId(getHeadlessVolumeID(volumeID));
        this.pages = null;
    }

    public void setPages(List<ContentReader> pages) {
        this.pages = pages;
    }
    
    public void setMetadata(List<ContentReader> metadata) {
        this.metadata = metadata;
    }

    private String getPrefix(String volumeID) {
        int indexOf = volumeID.indexOf('.');
        return volumeID.substring(0, indexOf);
    }

    private String getHeadlessVolumeID(String volumeID) {
        int indexOf = volumeID.indexOf('.');
        return volumeID.substring(indexOf + 1);
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
        return this.pairtreeCleanedVolumeID;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeReader#nextPage()
     */
    @Override
    public ContentReader nextPage() {
        ContentReader contentReader = null;
        if (hasMorePages()) {
            contentReader = pages.remove(0);
        } 
        return contentReader;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeReader#hasMorePages()
     */
    @Override
    public boolean hasMorePages() {
        return (pages != null && (!pages.isEmpty()));
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeReader#nextMetadata()
     */
    @Override
    public ContentReader nextMetadata() {
        ContentReader contentReader = null;
        if (hasMoreMetadata()) {
            contentReader = metadata.remove(0);
        }
        return contentReader;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeReader#hasMoreMetadata()
     */
    @Override
    public boolean hasMoreMetadata() {
        return (metadata != null && (!metadata.isEmpty()));
    }
    
    

}




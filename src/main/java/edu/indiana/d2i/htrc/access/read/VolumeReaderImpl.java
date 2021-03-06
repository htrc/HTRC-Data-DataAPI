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
# File:  VolumeReaderImpl.java
# Description:  This class is an implementation of the VolumeReader interface
#
# -----------------------------------------------------------------
# 
 */



/**
 * 
 */
package edu.indiana.d2i.htrc.access.read;

import java.util.List;

import edu.indiana.d2i.htrc.access.RequestedItemCoordinates;
import edu.indiana.d2i.htrc.access.VolumeReader;
import gov.loc.repository.pairtree.Pairtree;

/**
 * This class is an implementation of the VolumeReader interface
 * 
 * @author Yiming Sun
 *
 */
public class VolumeReaderImpl implements VolumeReader {

    /**
     * This class is an implementation of the ContentReader interface
     * @author Yiming Sun
     *
     */
    public static class ContentReaderImpl implements ContentReader {

        protected final String contentName;
        protected final byte[] content;

        /**
         * Constructor
         * 
         * @param contentName name of the content, which can be the name of a metadata entry, or the page sequence number of a page
         * @param content the content
         */
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


    /**
     * Constructor
     * @param identifier an HTRCItemIdentifier object the VolumeReader is associated with
     */
    public VolumeReaderImpl(RequestedItemCoordinates identifier) {
        Pairtree pairtree = new Pairtree();
        this.volumeID = identifier.getVolumeID();
        this.pairtreeCleanedVolumeID = getPrefix(volumeID) + "." + pairtree.cleanId(getHeadlessVolumeID(volumeID));
        this.pages = null;
        this.metadata = null;
    }

    /**
     * Method to set a List of ContentReader objects holding page content
     * @see edu.indiana.d2i.htrc.access.VolumeReader#setPages(java.util.List)
     */
    public void setPages(List<ContentReader> pages) {
        this.pages = pages;
    }
    
    /**
     * Method to set a List of ContentReader objects holding metadata entry content
     * @see edu.indiana.d2i.htrc.access.VolumeReader#setMetadata(java.util.List)
     */
    public void setMetadata(List<ContentReader> metadata) {
        this.metadata = metadata;
    }

    /**
     * Method to return the prefix portion of the volumeID
     * @param volumeID volumeID
     * @return the prefix portion of the volumeID
     */
    private String getPrefix(String volumeID) {
        int indexOf = volumeID.indexOf('.');
        return volumeID.substring(0, indexOf);
    }

    /**
     * Method to return the local portion of the volumeID, i.e. the volumeID sans the prefix
     * @param volumeID volumeID
     * @return the local portion of the volumeID
     */
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




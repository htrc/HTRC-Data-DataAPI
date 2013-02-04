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
# Unless required by applicable law or areed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  VolumeReader.java
# Description:  Interface definition for VolumeReader
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.util.List;

/**
 * Interface definition for VolumeReader
 * 
 * @author Yiming Sun
 *
 */
public interface VolumeReader {
    
    /**
     * Interface definition for ContentReader, which is supposed to return the content of a page or a metadata entry
     * 
     * @author Yiming Sun
     *
     */
    public static interface ContentReader {
        /**
         * Method to return the name of the content, i.e. the name of a metadata entry or the page sequence string of a page
         * @return name of the content
         */
        public String getContentName();
        
        /**
         * Method to return the content
         * @return the content as a byte array
         */
        public byte[] getContent();
    }
    
    /**
     * Method to return the volumeID
     * @return the volumeID
     */
    public String getVolumeID();
    
    /**
     * Method to return a morph of the volumeID which is can be considered cleaned by the Pairtree.
     * 
     * A volumeID consists of a prefix and a local identifier.  Strictly speaking, the Pairtree cleaning process can only be applied to the local identifier because the prefix is not considered
     * a part of the Pairtree path.  Implementation of this method should apply the Pairtree cleaning process to the local identifier, and then re-attach the prefix in front of the cleaned local
     * identifier.
     * 
     * @return Pairtree cleaned volumeID
     */
    public String getPairtreeCleanedVolumeID();

    /**
     * Method to return the ContentReader object for the next page in the volume
     * 
     * @return the ContentReader object for the next page in the volume
     */
    public ContentReader nextPage();
    
    /**
     * Method for checking if there are more pages in the volume
     * 
     * @return <code>true</code> if there are more pages in the volume, and <code>false</code> otherwise.
     */
    public boolean hasMorePages();
    
    /**
     * Method to set a List of ContentReaders representing pages of the volume
     * 
     * @param pageReaders a List of ContentReaders representing pages of the volume
     */
    public void setPages(List<ContentReader> pageReaders);
    
    /**
     * Method to return the ContentReader object for the next metadata entry of the volume
     * 
     * @return the ContentReader object for the next metadata entry of the volume
     */
    public ContentReader nextMetadata();
    
    /**
     * Method for checking if there are more metadata entries of the volume
     * @return <code>true</code> if there are more metadata entries in the volume, and <code>false</code> otherwise.
     */
    public boolean hasMoreMetadata();
    
    /**
     * Method to set a List of ContentReaders representing metadata entries of the volume
     * @param metadataReaders a List of ContentReaders representing metadata entries of the volume
     */
    public void setMetadata(List<ContentReader> metadataReaders);

}


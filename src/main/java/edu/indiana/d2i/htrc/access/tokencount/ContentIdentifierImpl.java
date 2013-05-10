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
# File:  ContentIdentifierImpl.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

/**
 * @author Yiming Sun
 *
 */
public class ContentIdentifierImpl implements ContentIdentifier {
    protected final String volumeID;
    protected final String pageSequence;
    
    public ContentIdentifierImpl(String volumeID) {
        this.volumeID = volumeID;
        this.pageSequence = null;
    }
    
    public ContentIdentifierImpl(String volumeID, String pageSequence) {
        this.volumeID = volumeID;
        this.pageSequence = pageSequence;
    }
    
    public String getVolumeID() {
        return volumeID;
    }
    
    public String getPageSequenceID() {
        return pageSequence;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.tokencount.ContentIdentifier#getPrefix()
     */
    @Override
    public String getPrefix() {
        int index = volumeID.indexOf(".");
        return volumeID.substring(0, index);
    }

    /**
     * @see edu.indiana.d2i.htrc.access.tokencount.ContentIdentifier#getHeadlessID()
     */
    @Override
    public String getHeadlessID() {
        int index = volumeID.indexOf(".");
        return volumeID.substring(index + 1);
    }

}


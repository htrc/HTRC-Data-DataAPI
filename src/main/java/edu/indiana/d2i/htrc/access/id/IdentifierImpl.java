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
# File:  IdentifierImpl.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.id;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;

/**
 * @author Yiming Sun
 *
 */
public class IdentifierImpl implements HTRCItemIdentifier {

    protected final String volumeID;
    protected final Set<String> metadataNameSet;
    protected final Set<String> pageSequenceSet;

    public IdentifierImpl(String volumeID) {
        this.volumeID = volumeID;
        this.metadataNameSet = new HashSet<String>();
        this.pageSequenceSet = new HashSet<String>();
        
    }
    /**
     * @see edu.indiana.d2i.htrc.access.HTRCItemIdentifier#getVolumeID()
     */
    @Override
    public String getVolumeID() {
        return this.volumeID;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.HTRCItemIdentifier#getPageSequences()
     */
    @Override
    public List<String> getPageSequences() {
        List<String> sortedList = null;
        if (!pageSequenceSet.isEmpty()) {
            sortedList = new LinkedList<String>(pageSequenceSet);
            Collections.<String>sort(sortedList);
        }
        return sortedList;
    }
    
    public void addPageSequence(String pageSequence) {
        pageSequenceSet.add(pageSequence);
    }

    public void clearPageSequences() {
        this.pageSequenceSet.clear();
    }
    
    public int getPageSequenceCount() {
        return pageSequenceSet.size();
    }

    /**
     * @see edu.indiana.d2i.htrc.access.HTRCItemIdentifier#getMetadataNames()
     */
    @Override
    public List<String> getMetadataNames() {
        List<String> sortedList = null;
        if (!metadataNameSet.isEmpty()) {
            sortedList = new LinkedList<String>(metadataNameSet);
            Collections.<String>sort(sortedList);
        }
        return sortedList;
    }
    
    public void addMetadataName(String metadataName) {
        this.metadataNameSet.add(metadataName);
    }
    
    public void clearMetadataNames() {
        this.metadataNameSet.clear();
    }

    public int getMetadataNameCount() {
        return metadataNameSet.size();
    }
}


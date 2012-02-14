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
# File:  VolumeIdentifier.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.id;

import java.util.List;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;

/**
 * @author Yiming Sun
 *
 */
public class VolumeIdentifier implements HTRCItemIdentifier {

    
    protected final String volumeID;
    
    public VolumeIdentifier(String volumeID) {
        this.volumeID = volumeID;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.HTRCItemIdentifier#getVolumeID()
     */
    @Override
    public String getVolumeID() {
        return volumeID;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.HTRCItemIdentifier#getPageSequences()
     */
    @Override
    public List<String> getPageSequences() {
        // a volume identifier should not have page sequences
        return null;
    }

}


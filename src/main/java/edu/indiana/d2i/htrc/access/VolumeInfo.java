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
# File:  VolumeInfo.java
# Description:  This abstract class holds some very basic information of a volume
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import edu.indiana.d2i.htrc.access.read.HectorResource.CopyrightEnum;

/**
 * This abstract class holds some very basic information of a volume
 * 
 * @author Yiming Sun
 *
 */
public abstract class VolumeInfo {
    protected final String volumeID;
    
    /**
     * Constructor
     * 
     * @param volumeID ID of the volume it represents
     */
    public VolumeInfo(String volumeID) {
        this.volumeID = volumeID;
    }
    
    /**
     * Method to return the volumeID
     * @return the volumeID
     */
    public String getVolumeID() {
        return this.volumeID;
    }
    
    /**
     * Method to return the number of pages in the volume
     * @return the number of pages in the volume
     */
    public abstract int getPageCount();
    
    /**
     * Method to return the copyright of the volume
     * @return the copyright of the volume
     */
    public abstract CopyrightEnum getCopyright();
    

}


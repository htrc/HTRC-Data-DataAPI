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
# File:  VolumeInfo.java
# Description:  
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
 * @author Yiming Sun
 *
 */
public abstract class VolumeInfo {
    protected final String volumeID;
    
    public VolumeInfo(String volumeID) {
        this.volumeID = volumeID;
    }
    
    public String getVolumeID() {
        return this.volumeID;
    }
    
    public abstract int getPageCount();
    public abstract CopyrightEnum getCopyright();
    

}


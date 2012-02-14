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
# File:  VolumeReader.java
# Description:  
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
 * @author Yiming Sun
 *
 */
public interface VolumeReader {
    
    public static interface PageReader {
        public String getPageSequence();
        public String getPageContent();
    }
    
    public String getVolumeID();
    public String getPairtreeCleanedVolumeID();
    public PageReader nextPage() throws KeyNotFoundException;
    public boolean hasMorePages();
    public void setPages(List<PageReader> pageReaders);

}


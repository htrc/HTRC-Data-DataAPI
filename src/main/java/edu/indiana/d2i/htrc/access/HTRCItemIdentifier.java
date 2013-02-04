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
# File:  HTRCItemIdentifier.java
# Description: This is an interface for HathiTrust Research Center item identifier
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
 * This is an interface for HathiTrust Research Center item identifier
 * 
 * @author Yiming Sun
 *
 */
public interface HTRCItemIdentifier {
    
    /**
     * Method to get volume ID portion of the item identifier
     * 
     * @return volume ID of the item 
     */
    public String getVolumeID();
    
    /**
     * Method to get page sequence numbers of the item identifier if applicable
     * 
     * @return a List of String containing page sequence numbers, or null if not applicable
     */
    public List<String> getPageSequences();
    
    /**
     * Method to get metadata names of the item identifier if applicable
     * 
     * @return a List of String containing metadata names, or null if not applicable
     */
    public List<String> getMetadataNames();
    
}


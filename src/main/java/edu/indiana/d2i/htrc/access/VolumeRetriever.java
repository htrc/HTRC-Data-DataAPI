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
# File:  VolumeRetriever.java
# Description:  Interface definition for retrieving volumes
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;

/**
 * Interface definition for retrieving volumes
 * 
 * @author Yiming Sun
 *
 */
public interface VolumeRetriever {

    /**
     * Method for checking if there are more volumes
     * @return <code>true</code> if there are more volumes, <code>false</code> otherwise
     */
    public boolean hasMoreVolumes();
    
    
    /**
     * Method to get the next volume
     * @return a VolumeReader object for the next volume
     * @throws KeyNotFoundException thrown if the volumeID does not exist
     * @throws PolicyViolationException thrown if the retrieval of the volume violates any policies
     * @throws RepositoryException thrown if error occurs at the backend repository
     */
    public VolumeReader nextVolume() throws KeyNotFoundException, PolicyViolationException, RepositoryException;
    
}


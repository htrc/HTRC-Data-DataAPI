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
# File:  ExceptionalVolumeRetriever.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.read;

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;

/**
 * @author Yiming Sun
 *
 */
public class ExceptionalVolumeRetriever implements VolumeRetriever {
    private boolean hasMoreVolumes = true;

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#hasMoreVolumes()
     */
    @Override
    public boolean hasMoreVolumes() {
        return hasMoreVolumes;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#nextVolume()
     */
    @Override
    public VolumeReader nextVolume() throws KeyNotFoundException {
        hasMoreVolumes = false;
        throw new KeyNotFoundException("test.offending/key");
    }

}


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
# File:  VolumePageIdentifer.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Yiming Sun
 *
 */
public class VolumePageIdentifier extends VolumeIdentifier {
    
    protected final Set<String> pageSequenceSet;
    
    public VolumePageIdentifier(String volumeID) {
        super(volumeID);
        this.pageSequenceSet = new HashSet<String>();
        
    }
    
    @Override
    public List<String> getPageSequences() {
        
        List<String> sortedList = new ArrayList<String>(pageSequenceSet);
        Collections.<String>sort(sortedList);
        return Collections.<String>unmodifiableList(sortedList);
    }
    
    public void addPageSequence(String pageSequence) {
        pageSequenceSet.add(pageSequence);
    }
}


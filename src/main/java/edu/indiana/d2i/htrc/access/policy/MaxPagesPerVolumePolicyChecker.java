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
# File:  MaxPagesPerVolumePolicyChecker.java
# Description:  This implementation of PolicyChecker is a policy on the maximum number of pages can be retrieved from each volume 
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.policy;

import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.PolicyChecker;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;

/**
 * This implementation of PolicyChecker is a policy on the maximum number of pages can be retrieved from each volume
 * 
 * @author Yiming Sun
 *
 */
public class MaxPagesPerVolumePolicyChecker implements PolicyChecker {

    public static final String PN_MAX_PAGES_PER_VOLUME_ALLOWED = "max.pages.per.volume.allowed";
    public static final String POLICY_NAME = "Max Pages Per Volume Allowed";
    
    private final int maxPagesPerVolumeAllowed;
   
    /**
     * Constructor
     * 
     * @param parameterContainer an ParameterContainer object
     */
    public MaxPagesPerVolumePolicyChecker(ParameterContainer parameterContainer) {
        int defaultValue = 0;
        String value = parameterContainer.getParameter(PN_MAX_PAGES_PER_VOLUME_ALLOWED);
        if (value != null) {
            int intVal = Integer.valueOf(value);
            defaultValue = (intVal > 0) ? intVal : 0;
        }

        this.maxPagesPerVolumeAllowed = defaultValue;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.PolicyChecker#check(int)
     */
    @Override
    public void check(int value, String token) throws PolicyViolationException {
        if (this.maxPagesPerVolumeAllowed > 0 && value > maxPagesPerVolumeAllowed) {
            throw new PolicyViolationException(POLICY_NAME, this.maxPagesPerVolumeAllowed, token);
        }
    }

    /**
     * @see edu.indiana.d2i.htrc.access.PolicyChecker#getLimit()
     */
    @Override
    public int getLimit() {
        return maxPagesPerVolumeAllowed;
    }

    
}


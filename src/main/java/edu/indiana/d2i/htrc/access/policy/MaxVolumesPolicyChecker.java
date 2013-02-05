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
# File:  MaxVolumesPolicyChecker.java
# Description: This implementation of PolicyChecker is a policy on the maximum number of volumes may be accessed (regardless of number of pages) in a single request  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.policy;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.PolicyChecker;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;

/**
 * This implementation of PolicyChecker is a policy on the maximum number of volumes may be accessed (regardless of number of pages) in a single request
 * 
 * @author Yiming Sun
 *
 */
public final class MaxVolumesPolicyChecker implements PolicyChecker {

    private static Logger log = Logger.getLogger(MaxVolumesPolicyChecker.class);
    
    public static final String PN_MAX_VOLUMES_ALLOWED = "max.volumes.allowed";
    public static final String POLICY_NAME = "Max Volumes Allowed";
    
    private final int maxVolumesAllowed;
    
    /**
     * Constructor
     * 
     * @param parameterContainer a ParameterContainer object
     */
    public MaxVolumesPolicyChecker(ParameterContainer parameterContainer) {
        int defaultValue = 0;
        if (parameterContainer != null) {
            String value = parameterContainer.getParameter(PN_MAX_VOLUMES_ALLOWED);
            if (value != null) {
                int intVal = Integer.valueOf(value);
                defaultValue = (intVal > 0) ? intVal : 0;
            }
        }
        this.maxVolumesAllowed = defaultValue;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.PolicyChecker#check(int)
     */
    @Override
    public void check(int value, String token) throws PolicyViolationException {
        if (log.isDebugEnabled()) log.debug("maxVolumesAllowed: " + maxVolumesAllowed + " check value: " + value);
        if (this.maxVolumesAllowed > 0 && value > maxVolumesAllowed) {
            throw new PolicyViolationException(POLICY_NAME, this.maxVolumesAllowed, token);
        }
    }

    /**
     * @see edu.indiana.d2i.htrc.access.PolicyChecker#getLimit()
     */
    @Override
    public int getLimit() {
        return maxVolumesAllowed;
    }

}


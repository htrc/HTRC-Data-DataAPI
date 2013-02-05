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
# File:  MaxTotalPagesPolicyChecker.java
# Description:  This implementation of PolicyChecker is a policy on the maximum number of pages can be retrieved from a single request
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
 * This implementation of PolicyChecker is a policy on the maximum number of pages can be retrieved from a single request
 * 
 * @author Yiming Sun
 *
 */
public class MaxTotalPagesPolicyChecker implements PolicyChecker {

    public static final String PN_MAX_TOTAL_PAGES_ALLOWED = "max.total.pages.allowed";
    public static final String POLICY_NAME = "Max Total Pages Allowed";
    
    private final int maxTotalPagesAllowed;
    
    /**
     * Constructor
     * 
     * @param parameterContainer a ParameterContainer object
     */
    public MaxTotalPagesPolicyChecker(ParameterContainer parameterContainer) {
        int defaultValue = 0;
        String value = parameterContainer.getParameter(PN_MAX_TOTAL_PAGES_ALLOWED);
        if (value != null) {
            int intVal = Integer.valueOf(value);
            defaultValue = (intVal > 0) ? intVal : 0;
        }

        this.maxTotalPagesAllowed = defaultValue;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.PolicyChecker#check(int)
     */
    @Override
    public void check(int value, String token) throws PolicyViolationException {
        if (this.maxTotalPagesAllowed > 0 && value > maxTotalPagesAllowed) {
            throw new PolicyViolationException(POLICY_NAME, this.maxTotalPagesAllowed, token);
        }
    }

    /**
     * @see edu.indiana.d2i.htrc.access.PolicyChecker#getLimit()
     */
    @Override
    public int getLimit() {
        return maxTotalPagesAllowed;
    }

}


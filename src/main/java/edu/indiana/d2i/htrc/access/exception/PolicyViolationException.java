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
# File:  PolicyViolationException.java
# Description:  This class is the Exception to be thrown when a request violates any definied policies
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.exception;

/**
 * This class is the Exception to be thrown when a request violates any definied policies
 * 
 * @author Yiming Sun
 *
 */
public class PolicyViolationException extends DataAPIException {
    
    /**
     * Constructor
     * @param policyName Name of the policy that is violated
     * @param max the max limit defined by the policy
     * @param offendingID the specific ID that violates the policy
     */
    public PolicyViolationException(String policyName, int max, String offendingID) {
        super("Request violates " + policyName + " " + max + ". Offending ID: " + offendingID);
    }

}


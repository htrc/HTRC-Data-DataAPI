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
# File:  PolicyChecker.java
# Description: This is an interface for policy checkers that can be used to place certain restrictions and limitations on client requests
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;

/**
 * This is an interface for policy checkers that can be used to place certain restrictions and limitations on client requests
 * 
 * @author Yiming Sun
 *
 */
public interface PolicyChecker {
    
    /**
     * Method to check if a policy has been violated
     * 
     * @param value a value to check against the limit/restriction set by the specifc PolicyChecker
     * @param token a flexible token for displaying more detailed information on the violation
     * @throws PolicyViolationException thrown if the value violates the policy
     */
    public void check(int value, String token) throws PolicyViolationException;
    
    /**
     * Method to get the limit set by the specific PolicyChecker
     * 
     * @return the limit value
     */
    public int getLimit();

}


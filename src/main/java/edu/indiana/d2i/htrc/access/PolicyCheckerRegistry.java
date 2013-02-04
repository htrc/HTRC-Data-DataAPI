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
# File:  PolicyCheckerRegistry.java
# Description: This is an interface for a registry holding different PolicyChecker implementations
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

/**
 * This is an interface for a registry holding different PolicyChecker implementations
 * 
 * @author Yiming Sun
 *
 */
public interface PolicyCheckerRegistry {
    /**
     * Method to get the PolicyChecker implementation identified by the key
     * 
     * @param key key to identify the PolicyChecker implementation
     * @return the PolicyChecker implementation identified by the key
     */
    PolicyChecker getPolicyChecker(String key);

}


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
# File:  PolicyCheckerRegistryImpl.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.policy;

import java.util.HashMap;
import java.util.Map;

import edu.indiana.d2i.htrc.access.PolicyChecker;
import edu.indiana.d2i.htrc.access.PolicyCheckerRegistry;

/**
 * @author Yiming Sun
 *
 */
public class PolicyCheckerRegistryImpl implements PolicyCheckerRegistry {

    
    protected Map<String, PolicyChecker> checkerMap = null;
    
    protected static PolicyCheckerRegistryImpl instance = null;
    
    protected PolicyCheckerRegistryImpl() {
        this.checkerMap = new HashMap<String, PolicyChecker>();
    }
    
    public static synchronized PolicyCheckerRegistryImpl getInstance() {
        if (instance == null) {
            instance = new PolicyCheckerRegistryImpl();
        }
        return instance;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.PolicyCheckerRegistry#getPolicyChecker(java.lang.String)
     */
    @Override
    public PolicyChecker getPolicyChecker(String key) {
        PolicyChecker policyChecker = checkerMap.get(key);
        if (policyChecker == null) {
            policyChecker = new NullPolicyChecker();
        }
        return policyChecker;
    }
    
    public void registerPolicyChecker(String key, PolicyChecker policyChecker) {
        this.checkerMap.put(key, policyChecker);
    }

}


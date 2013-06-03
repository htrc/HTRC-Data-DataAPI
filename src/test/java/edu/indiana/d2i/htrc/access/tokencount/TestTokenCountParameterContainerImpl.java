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
# File:  TestTokenCountParameterContainerImpl.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.util.HashMap;
import java.util.Map;

import edu.indiana.d2i.htrc.access.ParameterContainer;

/**
 * @author Yiming Sun
 *
 */
public class TestTokenCountParameterContainerImpl implements ParameterContainer{
    
    Map<String, String> map = null;
    
    TestTokenCountParameterContainerImpl() {
        this.map = new HashMap<String, String>();
        map.put("max.tokenization.tasks", "10");
        map.put("min.tokenization.tasks", "2");
    }

    /**
     * @see edu.indiana.d2i.htrc.access.ParameterContainer#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String parameterName) {
        return map.get(parameterName);
    }

    /**
     * @see edu.indiana.d2i.htrc.access.ParameterContainer#setParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void setParameter(String name, String value) {
        this.map.put(name, value);
    }

}


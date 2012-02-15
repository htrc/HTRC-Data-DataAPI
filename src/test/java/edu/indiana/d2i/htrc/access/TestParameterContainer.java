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
# File:  TestParameterContainer.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yiming Sun
 *
 */
public class TestParameterContainer implements ParameterContainer {

    private final Map<String, String> map;
    
    public TestParameterContainer() {
        this.map = new HashMap<String, String>();
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


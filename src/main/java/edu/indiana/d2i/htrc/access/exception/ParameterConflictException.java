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
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  ParameterConflictException.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.exception;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Yiming Sun
 *
 */
public class ParameterConflictException extends DataAPIException {
    
    protected final List<String> offendingParameters;
    protected final String context;
    
    
    public ParameterConflictException(String context, List<String> parameters) {
        super(ParameterConflictException.formMessage(context, parameters));
        this.context = context;
        offendingParameters = new LinkedList<String>();
        for (String param : parameters) {
            offendingParameters.add(param);
        }

    }
    
    public ParameterConflictException(List<String> parameters) {
        this(null, parameters);
    }
    
    private static String formMessage(String context, List<String> offendingParameters) {
        StringBuilder stringBuilder = new StringBuilder("Conflicting parameters");
        if (context != null) {
            stringBuilder.append(" in " + context);
        }
        stringBuilder.append(". Offending Parameters: ");
        stringBuilder.append(offendingParameters.get(0));
        for (int i = 1; i < offendingParameters.size(); i++) {
            stringBuilder.append(", " + offendingParameters.get(i));
        }
        
        return stringBuilder.toString();
    }
    
}


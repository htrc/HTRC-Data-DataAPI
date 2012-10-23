/*
#
# Copyright 2012 The Trustees of Indiana University
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
# File:  ThrowableContainer.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

/**
 * @author Yiming Sun
 *
 */
public class ExceptionContainer {
    public static enum ExceptionType {
        EXCEPTION_KEY_NOT_FOUND,
        EXCEPTION_REPOSITORY,
        EXCEPTION_POLICY_VIOLATION;
    }
    
    private final Exception exception;
    private final ExceptionType exceptionType;
    
    public ExceptionContainer(Exception exception, ExceptionType exceptionType) {
        this.exception = exception;
        this.exceptionType = exceptionType;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public ExceptionType getExceptionType() {
        return exceptionType;
    }

}


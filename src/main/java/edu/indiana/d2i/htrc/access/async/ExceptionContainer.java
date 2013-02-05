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
# File:  ExceptionContainer.java
# Description:  This class is for holding an Exception thrown during the asynchronous fetch
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

/**
 * This class is for holding an Exception thrown during the asynchronous fetch
 * 
 * @author Yiming Sun
 *
 */
public class ExceptionContainer {
    /**
     * Types of Exceptions that the container may hold
     * @author Yiming Sun
     *
     */
    public static enum ExceptionType {
        EXCEPTION_KEY_NOT_FOUND,
        EXCEPTION_REPOSITORY,
        EXCEPTION_POLICY_VIOLATION;
    }
    
    private final Exception exception;
    private final ExceptionType exceptionType;
    
    /**
     * Constructor
     * 
     * @param exception an Exception object to be held in the container
     * @param exceptionType the type of the Exception object to be held
     */
    public ExceptionContainer(Exception exception, ExceptionType exceptionType) {
        this.exception = exception;
        this.exceptionType = exceptionType;
    }
    
    /**
     * Method to return the held Exception object
     * @return an Exception object
     */
    public Exception getException() {
        return exception;
    }
    
    /**
     * Method to return the type of the held Exception object
     * @return an ExceptionType enum
     */
    public ExceptionType getExceptionType() {
        return exceptionType;
    }

}


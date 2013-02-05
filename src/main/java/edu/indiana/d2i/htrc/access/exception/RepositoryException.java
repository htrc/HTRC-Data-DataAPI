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
# File:  RepositoryException.java
# Description:  This is the Exception to be thrown when the backend repository fails
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.exception;

/**
 * This is the Exception to be thrown when the backend repository fails
 * 
 * @author Yiming Sun
 *
 */
public class RepositoryException extends DataAPIException {
    
    /**
     * Constructor that takes a message
     * 
     * @param message a String message for the Exception
     */
    public RepositoryException(String message) {
        super(message);
    }
    
    /**
     * Constructor that wraps a Throwable object and takes a message
     * @param message a String message for the Exception
     * @param e a Throwable object to wrap
     */
    public RepositoryException(String message, Throwable e) {
        super(message, e);
    }

}


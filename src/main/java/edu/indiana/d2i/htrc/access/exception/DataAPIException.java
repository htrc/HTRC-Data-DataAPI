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
# File:  DataAPIException.java
# Description:  This abstract class is the superclass for all DataAPI generated Exceptions
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.exception;

/**
 * This abstract class is the superclass for all DataAPI generated Exceptions
 * 
 * @author Yiming Sun
 *
 */
public abstract class DataAPIException extends Exception {
   
    /**
     * Constructor that wraps a Throwable object and a message into a DataAPIException object
     * @param message a String message for the Exception
     * @param throwable a Throwable object to be wrapped
     */
    public DataAPIException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    /**
     * Constructor that takes a message
     * @param message a String message for the Exception
     */
    public DataAPIException(String message) {
        super(message);
    }

}


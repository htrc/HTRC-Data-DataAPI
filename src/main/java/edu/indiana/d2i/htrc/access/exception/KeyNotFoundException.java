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
# File:  KeyNotFoundException.java
# Description:  This class is the Exception to be thrown when a given ID does not exist in the backend
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.exception;

/**
 * This class is the Exception to be thrown when a given ID does not exist in the backend
 * 
 * @author Yiming Sun
 *
 */
public class KeyNotFoundException extends DataAPIException {
    
    /**
     * Constructor that takes the offending ID
     * @param key the offending ID
     */
    public KeyNotFoundException(String key) {
        super("Offending key: " + key);
    }
    
    /**
     * Constructor that takes the offending ID and a message
     * @param key the offending ID
     * @param message a String message
     */
    public KeyNotFoundException(String key, String message) {
        super("Offending key: " + key + " " + message);
    }
    

}


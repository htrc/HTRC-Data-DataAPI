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
# File:  RequestValidityChecker.java
# Description: This is an interface for checking the validity of requests
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.util.List;
import java.util.Map;

import edu.indiana.d2i.htrc.access.exception.DataAPIException;

/**
 * This is an interface for checking the validity of requests.  It is deprecated because with
 * asynchronous retrieval, the data API no longer performs a validity check up-front, which can be slow.
 * 
 * @author Yiming Sun
 * 
 */
@Deprecated
public interface RequestValidityChecker {
    
    /**
     * Method to validate a request, and throws DataAPIException if the request is not valid
     * 
     * @param idList a List of HTRCItemIdentifiers requested by client
     * @return a Map containing some basic metadata information on the requested items, if all items are valid
     * @throws DataAPIException thrown if one or more items are invalid (if they do not exist)
     */
    public Map<String, ? extends VolumeInfo> validateRequest(List<? extends HTRCItemIdentifier> idList) throws DataAPIException;

}


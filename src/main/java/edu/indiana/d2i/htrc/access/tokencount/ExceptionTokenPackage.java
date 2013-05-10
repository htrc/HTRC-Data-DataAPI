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
# File:  ExceptionTokenPackage.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.util.List;

import edu.indiana.d2i.htrc.access.exception.DataAPIException;

/**
 * @author Yiming Sun
 *
 */
public class ExceptionTokenPackage implements TokenPackage {
    
    protected final DataAPIException exception;
    ExceptionTokenPackage(DataAPIException dataAPIException) {
        this.exception = dataAPIException;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.tokencount.TokenPackage#getContentIdentifier()
     */
    @Override
    public ContentIdentifier getContentIdentifier() {
        return null;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.tokencount.TokenPackage#getTokenList()
     */
    @Override
    public List<String> getTokenList() throws DataAPIException {
        throw exception;
    }
}


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
# File:  SimpleTokenPackageImpl.java
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
public class SimpleTokenPackageImpl implements TokenPackage{
    
    protected final ContentIdentifier contentIdentifier;
    protected final List<String> tokenList;
    
    public SimpleTokenPackageImpl(ContentIdentifier contentIdentifier, List<String> tokenList) {
        this.contentIdentifier = contentIdentifier;
        this.tokenList = tokenList;
    }

    
    
    /**
     * @see edu.indiana.d2i.htrc.access.tokencount.TokenPackage#getContentIdentifier()
     */
    @Override
    public ContentIdentifier getContentIdentifier() {
        return this.contentIdentifier;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.tokencount.TokenPackage#getTokenList()
     */
    @Override
    public List<String> getTokenList() throws DataAPIException {
        return this.tokenList;
    }

}


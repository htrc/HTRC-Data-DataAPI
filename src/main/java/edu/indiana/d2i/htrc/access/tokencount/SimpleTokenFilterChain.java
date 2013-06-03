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
# File:  TokenFilterChain.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Yiming Sun
 *
 */
public class SimpleTokenFilterChain implements TokenFilterChain {
    private static Logger log = Logger.getLogger(SimpleTokenFilterChain.class);
    protected List<TokenFilter> chain;
    
    public SimpleTokenFilterChain() {
        this.chain = new LinkedList<TokenFilter>();
    }

    public void addFilter(TokenFilter tokenFilter) {
        chain.add(tokenFilter);
    }
    /**
     * @see edu.indiana.d2i.htrc.access.tokencount.TokenFilter#filter(edu.indiana.d2i.htrc.access.tokencount.Tokenizer.TokenPackage)
     */
    @Override
    public TokenPackage filter(TokenPackage tokenPackage) {
        TokenPackage filteredTokenPackage = tokenPackage;
        if (log.isDebugEnabled()) log.debug("feeding into filter chain");
        for (TokenFilter filter : chain) {
            filteredTokenPackage = filter.filter(filteredTokenPackage);
        }
        if (log.isDebugEnabled()) log.debug("out of filter chain");
        return filteredTokenPackage;
    }
}


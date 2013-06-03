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
# File:  EmptyTokenRemovalFilter.java
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

import edu.indiana.d2i.htrc.access.exception.DataAPIException;

/**
 * @author Yiming Sun
 *
 */
public class EmptyTokenRemovalFilter implements TokenFilter {

    private static final Logger log = Logger.getLogger(EmptyTokenRemovalFilter.class);
    /**
     * @throws DataAPIException 
     * @see edu.indiana.d2i.htrc.access.tokencount.TokenFilter#filter(edu.indiana.d2i.htrc.access.tokencount.TokenPackage)
     */
    @Override
    public TokenPackage filter(TokenPackage tokenPackage) {
        TokenPackage returnTokenPackage = null;
        if (tokenPackage instanceof ExceptionTokenPackage) {
            if (log.isDebugEnabled()) log.debug("encountered ExceptionTokenPackage");
            returnTokenPackage = tokenPackage;
        } else {
            if (log.isDebugEnabled()) log.debug("traversing token list");
            try {
                List<String> tokenList = tokenPackage.getTokenList();
                List<String> newTokenList = new LinkedList<String>();
                for (String token : tokenList) {
                    if (!"".equals(token)) {
                        newTokenList.add(token);
                    }
                }
                if (log.isDebugEnabled()) log.debug("done traversing token list");
                returnTokenPackage = new SimpleTokenPackageImpl(tokenPackage.getContentIdentifier(), newTokenList);
            } catch (DataAPIException e) {
                returnTokenPackage = new ExceptionTokenPackage(e);
                log.error("non ExceptionTokenPackage threw DataAPIException", e);
            }
        }
        
        return returnTokenPackage;
    }

}


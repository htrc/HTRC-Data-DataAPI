/*
#
# Copyright 2007 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or areed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  NullAuditor.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.audit;

import edu.indiana.d2i.htrc.access.Auditor;
import edu.indiana.d2i.htrc.access.ContextExtractor;

/**
 * @author Yiming Sun
 *
 */
public class NullAuditor extends Auditor {

    public NullAuditor(ContextExtractor contextExtractor) {
        super(contextExtractor);
    }
    /**
     * @see edu.indiana.d2i.htrc.access.Auditor#audit(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public void audit(String action, String volumeID, String... pageSequences) {
        // do nothing
    }
    
    @Override
    public void error(String errorType, String message, String cause) {
        // do nothing
    }

}


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
# File:  Log4jAuditer.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.audit;

import java.util.List;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.Auditor;
import edu.indiana.d2i.htrc.access.ContextExtractor;

/**
 * @author Yiming Sun
 *
 */
public final class Log4jAuditor extends Auditor {
    
    private static final Logger auditLogger = Logger.getLogger("audit");
    private static Logger log = Logger.getLogger(Log4jAuditor.class);
    
//    private static final String SPACE = " ";
    private static final String TAB = "\t";
    private static final String COMMA = ",";
    private static final String UNKNOWN = "UNKNOWN";
    private static final String VIA = "->";
    
//    private String userID = UNKNOWN;
//    private String userIP = UNKNOWN;
    
    private String userIdentity = null;
    
    public Log4jAuditor(ContextExtractor contextExtractor) {
        super(contextExtractor);
        String userID = UNKNOWN;
        String userIP = UNKNOWN;

        List<String> context = contextExtractor.getContext("remoteUser");
        userID = listToString(context);
        
        context = contextExtractor.getContext("remoteAddr");
        String remoteAddr = listToString(context);
        if (log.isDebugEnabled()) log.debug("remoteAddr:" + remoteAddr);
        
        context = contextExtractor.getContext("X-Forwarded-For");
       
        String forwardedFor = listToString(context);
        
        if (log.isDebugEnabled()) log.debug("X-Forwarded-For:" + forwardedFor);
        
        if (UNKNOWN.equals(forwardedFor)) {
            userIP = remoteAddr;
        } else {
            userIP = forwardedFor + VIA + remoteAddr;
        }
        
        StringBuilder builder  = new StringBuilder(userID);
        builder.append(TAB).append(userIP).append(TAB);
        userIdentity = builder.toString();
    }
  
    /**
     * @see edu.indiana.d2i.htrc.access.Auditor#audit(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public void audit(String action, String volumeID, String... pageSequences) {
//        StringBuilder builder = new StringBuilder(userID);
//        builder.append(TAB).append(userIP).append(TAB).append(action).append(TAB).append(volumeID);

    	StringBuilder builder = new StringBuilder(userIdentity);
    	builder.append(action).append(TAB).append(volumeID);
    	
        if (pageSequences != null) {
            int length = pageSequences.length;
            if (length > 0) {
                builder.append(TAB).append(pageSequences[0]);
                for (int i = 1; i < length; i++) {
                    builder.append(COMMA).append(pageSequences[i]);
                }
            }
        }
        auditLogger.info(builder.toString());
    }
    
    
    @Override
    public void error(String errorType, String message, String cause) {
//        StringBuilder builder = new StringBuilder(userID);
//        builder.append(TAB).append(userIP).append(TAB).append(errorType).append(TAB).append(message).append(TAB).append(cause);
    	StringBuilder builder = new StringBuilder(userIdentity);
    	builder.append(errorType).append(TAB).append(message).append(TAB).append(cause);
        auditLogger.error(builder.toString());
    }
    
    
    protected String listToString(List<String> list) {
        StringBuilder builder = new StringBuilder();
        
        if (list != null && !list.isEmpty()) {
            builder.append(list.get(0));
            for (int i = 1; i < list.size(); i++) {
                builder.append(COMMA).append(list.get(i));
            }
        } else {
            builder.append(UNKNOWN);
        }
        return builder.toString();
    }
    
   

}


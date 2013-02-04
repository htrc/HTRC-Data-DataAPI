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
# File:  ContextExtractor.java
# Description: This class extracts client information from HTTP request headers
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;

/**
 * This class extracts client information from HTTP request headers
 * 
 * @author Yiming Sun
 *
 */
public class ContextExtractor {
    
    private static Logger log = Logger.getLogger(ContextExtractor.class);
    
    protected final Map<String, List<String>> contextMap; 
    
    /**
     * Constructor
     * 
     * @param httpServletRequest HTTP Servlet Request object
     * @param httpHeaders HTTP request headers
     */
    public ContextExtractor(HttpServletRequest httpServletRequest, HttpHeaders httpHeaders) {
        contextMap = new HashMap<String, List<String>>();
        extractFromRequest(contextMap, httpServletRequest);
        if (httpHeaders != null) {
            extractFromHeaders(contextMap, httpHeaders);
        } else {
            extractHeadersFromRequest(contextMap, httpServletRequest);
        }
    }
    
    /**
     * Get values of a specific context key as a list of Strings
     * 
     * @param key context key
     * @return context values as a list of strings
     */
    public List<String> getContext(String key) {
        List<String> list = null;
        List<String> list2 = contextMap.get(key.toLowerCase());
        if (list2 != null) {
            list = Collections.unmodifiableList(list2);
        }
        return list;
    }
    
    /**
     * Get all context information as a Map
     * 
     * @return a map containing all context keys and values
     */
    public Map<String, List<String>> getContextMap() {
        return this.contextMap;
    }
    

    /**
     * Extract context information from HTTP Servlet request object and put the information into a Map
     * 
     * @param map a Map object to hold extracted context information
     * @param httpServletRequest an HTTP Servlet request
     */
    protected void extractFromRequest(Map<String, List<String>> map, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        putIntoMap(map, "remoteaddr", remoteAddr);
        if (log.isDebugEnabled()) log.debug("remoteAddr: " + remoteAddr);
        
        String remoteHost = httpServletRequest.getRemoteHost();
        putIntoMap(map, "remotehost", remoteHost);
        if (log.isDebugEnabled()) log.debug("remoteHost: " + remoteHost);
        
        int remotePort = httpServletRequest.getRemotePort();
        putIntoMap(map, "remoteport", Integer.toString(remotePort));
        if (log.isDebugEnabled()) log.debug("remotePort: " + remotePort);
        
        String remoteUser = httpServletRequest.getRemoteUser();
        putIntoMap(map, "remoteuser", remoteUser);
        if (log.isDebugEnabled()) log.debug("remoteUser: " + remoteUser);
        
        String requestURI = httpServletRequest.getRequestURI();
        putIntoMap(map, "requesturi", requestURI);
        if (log.isDebugEnabled()) log.debug("requestURI: " + requestURI);
        
    }
    
    /**
     * Utility method to put name-value pair into a Map
     * 
     * @param map a Map object to hold the name-value pair
     * @param name name of the name-value pair
     * @param value value of the name-value pair
     */
    protected void putIntoMap(Map<String, List<String>> map, String name, String value) {
        if (name != null && value != null) {
            if (!"".equals(name) && !"".equals(value)) {
                List<String> list = map.get(name);
                if (list == null) {
                    list = new ArrayList<String>();
                    map.put(name, list);
                }
                list.add(value);
            }
        }
    }
    
    
    /**
     * Extract context information from HTTP Request headers and put the information into a map
     * 
     * @param map a Map object to hold extracted context information
     * @param httpHeaders an HTTP Request headers object
     */
    protected void extractFromHeaders(Map<String, List<String>> map, HttpHeaders httpHeaders) {
        if (httpHeaders != null) {
            MultivaluedMap<String, String> requestHeaders = httpHeaders.getRequestHeaders();
            Set<String> keySet = requestHeaders.keySet();
            for (String key : keySet) {
                List<String> list = requestHeaders.get(key.toLowerCase());
                if (list != null && !list.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        StringBuilder builder = new StringBuilder(key);
                        builder.append(":");
                        for (String string : list) {
                            builder.append(string).append(" ");
                        }
                        log.debug(builder.toString());
                    }
                    
                    List<String> list2 = map.get(key);
                    if (list2 == null) {
                        list2 = new ArrayList<String>(list);
                        map.put(key.toLowerCase(), list2);
                    } else {
                        list2.addAll(list);
                    }
                } else {
                    if (log.isDebugEnabled()) log.debug(key + ": null | empty");
                }
            }
        }        
    }
    
    /**
     * Extract context information from the headers of an HTTP Servlet request object and put the information into a map
     * @param map a Map object to hold extracted context information
     * @param httpServletRequest an HTTP Servlet request object
     */
    // the warning is due to raw Enumeration type returned from HttpServletRequest.getHeaderNames()
    @SuppressWarnings("unchecked")
    protected void extractHeadersFromRequest(Map<String, List<String>> map, HttpServletRequest httpServletRequest) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String value = httpServletRequest.getHeader(headerName);
            List<String> valueList = decomposeValueToList(value);
            map.put(headerName, valueList);
            if (log.isDebugEnabled()) {
                StringBuilder builder = new StringBuilder(headerName);
                builder.append(":");
                for (String string : valueList) {
                    builder.append(string).append(" ");
                }
                log.debug(builder.toString());
            }
        }
    }

    /**
     * Converts comma-separated values in a single String into a List of Strings
     * 
     * @param value comma-separated values as a single String
     * @return a List of Strings containing individual values
     */
    protected List<String> decomposeValueToList(String value) {
        List<String> list = new ArrayList<String>();
        
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreTokens()) {
            String val = tokenizer.nextToken().trim();
            if (!"".equals(val)) {
                list.add(val);
            }
        }
        return list;
    }
}


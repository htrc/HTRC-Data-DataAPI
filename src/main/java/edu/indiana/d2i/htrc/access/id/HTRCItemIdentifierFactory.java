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
# File:  HTRCItemIdentifierFactory.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.id;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.PolicyCheckerRegistry;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.policy.NullPolicyCheckerRegistry;

/**
 * @author Yiming Sun
 *
 */
public class HTRCItemIdentifierFactory {
    
    public static abstract class Parser {
        
        public static final int PAGE_SEQUENCE_LENGTH = 8;
        
        public static final String PN_MAX_TOTAL_PAGES_ALLOWED = "max.total.pages.allowed";
        public static final String PN_MAX_PAGES_PER_VOLUME_ALLOWED = "max.pages.per.volume.allowed";
        
        protected PolicyCheckerRegistry policyCheckerRegistry = new NullPolicyCheckerRegistry();
        
        public abstract List<? extends HTRCItemIdentifier> parse(String string) throws ParseException, PolicyViolationException;
        
        
        public void setPolicyCheckerRegistry(PolicyCheckerRegistry policyCheckerRegistry) {
            this.policyCheckerRegistry = policyCheckerRegistry;
        }
        
        public static String generatePageSequenceString(int pageSequence) {
            if (pageSequence > 0) {
                String sequenceString = Integer.toString(pageSequence);
                StringBuilder builder = new StringBuilder();
                int padLength = PAGE_SEQUENCE_LENGTH - sequenceString.length();
                
                for (int i = 0; i < padLength; i++) {
                    builder.append('0');
                }
                builder.append(sequenceString);
                return builder.toString();
            } else {
                throw new IllegalArgumentException("Page sequence must be positive");
            }
        }
        
    }
    

    static class VolumeIDsParser extends Parser {
        /**
         * @see edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser#parse(java.lang.String)
         */
        @Override
        public List<VolumeIdentifier> parse(String identifiersString) throws ParseException, PolicyViolationException {
            
            Map<String, VolumeIdentifier> volumeIDMap = new HashMap<String, VolumeIdentifier>();

            StringTokenizer tokenizer = new StringTokenizer(identifiersString, "|");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (!"".equals(token)) {
                    VolumeIdentifier volumeIdentifier = volumeIDMap.get(token);
                    if (volumeIdentifier == null) {
                        volumeIdentifier = new VolumeIdentifier(token);
                        volumeIDMap.put(token, volumeIdentifier);

                    }
                }
            }

            if (volumeIDMap.isEmpty()) {
                throw new ParseException(identifiersString, 0);
            }
            return Collections.<VolumeIdentifier>unmodifiableList(new ArrayList<VolumeIdentifier>(volumeIDMap.values()));
        }
    }
    
    static class PageIDsParser extends Parser {

        private static Logger log = Logger.getLogger(PageIDsParser.class);
        
        static final int MIN_VOLUME_ID_LENGTH = 4;
        
        /**
         * @see edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser#parse(java.lang.String)
         */
        @Override
        public List<VolumePageIdentifier> parse(String identifiersString) throws ParseException, PolicyViolationException {

            Map<String, VolumePageIdentifier> pageIDMap = new HashMap<String, VolumePageIdentifier>();
            StringTokenizer tokenizer = new StringTokenizer(identifiersString, "|");
            while (tokenizer.hasMoreTokens()) {
                String rawUnit = tokenizer.nextToken().trim();
                
                if (!"".equals(rawUnit)) {
                    int length = rawUnit.length();
                    
                    if (rawUnit.charAt(length - 1) == '>') {
                        int lastIndex = rawUnit.lastIndexOf('<');
                        if (lastIndex > MIN_VOLUME_ID_LENGTH) {
                            boolean hasPageSequence = false;
                            
                            String volumeID = rawUnit.substring(0, lastIndex).trim();
                            if (log.isDebugEnabled()) log.debug("parsed volumeID: " + volumeID);
                            
                            VolumePageIdentifier volumePageIdentifier = pageIDMap.get(volumeID);
                            if (volumePageIdentifier == null) {
                                volumePageIdentifier = new VolumePageIdentifier(volumeID);
                                pageIDMap.put(volumeID, volumePageIdentifier);
                            }

                            String pageSeqRaw = rawUnit.substring(lastIndex + 1, length - 1).trim();
                            
                            StringTokenizer pageTokenizer = new StringTokenizer(pageSeqRaw, ",");
                            while (pageTokenizer.hasMoreTokens()) {
                                String pageSeqStr = pageTokenizer.nextToken().trim();
                                if (!"".equals(pageSeqStr)) {
                                    try {
                                        int pageSeqInt = Integer.valueOf(pageSeqStr);
                                        String pageSequence = generatePageSequenceString(pageSeqInt);
                                        volumePageIdentifier.addPageSequence(pageSequence);
                                        hasPageSequence = true;
                                    } catch (NumberFormatException e) {
                                        log.error("NumberFormatException while parsing page sequence", e);
                                        throw new ParseException(volumeID + "<" + pageSeqStr + ">", 0);
                                    } catch (IllegalArgumentException e) {
                                        log.error("IllegalArgumentException while parsing page sequence", e);
                                        throw new ParseException(volumeID + "<" + pageSeqStr + ">", 0);
                                    }
                                    
                                }
                            }

                            if (!hasPageSequence) {
                                throw new ParseException(rawUnit, lastIndex);
                            }
                        } else {
                            throw new ParseException(rawUnit, 0);
                        }
                        
                    } else {
                        throw new ParseException(rawUnit, rawUnit.length() - 1);
                    }
                }
            }
            if (pageIDMap.isEmpty()) {
                throw new ParseException(identifiersString, 0);
            }
            return Collections.<VolumePageIdentifier>unmodifiableList(new ArrayList<VolumePageIdentifier>(pageIDMap.values()));
        }
        
    }
    
    public static enum IDTypeEnum {
        VOLUME_ID(new VolumeIDsParser()),
        PAGE_ID(new PageIDsParser());
        
        final Parser parser;
        IDTypeEnum(Parser parser) {
            this.parser = parser;
        }
        
        private Parser getParser(PolicyCheckerRegistry policyCheckerRegistry) {
            if (policyCheckerRegistry == null) {
                parser.setPolicyCheckerRegistry(new NullPolicyCheckerRegistry());
            } else {
                parser.setPolicyCheckerRegistry(policyCheckerRegistry);
            }
            return parser;
        }
    }
    
    public static Parser getParser(IDTypeEnum type, PolicyCheckerRegistry policyCheckerRegistry) {
        Parser parser = type.getParser(policyCheckerRegistry);
        return parser;
    }

}


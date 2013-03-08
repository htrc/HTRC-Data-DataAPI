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
# Unless required by applicable law or areed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  IdentifierParserFactory.java
# Description:  This is a factory class of identifier parsers
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.id;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.Constants;
import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.PolicyCheckerRegistry;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.policy.NullPolicyCheckerRegistry;
import edu.indiana.d2i.htrc.access.read.HectorResource;

/**
 * This is a factory class of identifier parsers
 * 
 * @author Yiming Sun
 *
 */
public class IdentifierParserFactory {
    
    /**
     * This is an abstract Parser class
     * 
     * @author Yiming Sun
     *
     */
    public static abstract class Parser {
        
        protected static final String VOLUME_ID_PATTERN_STRING = ".+\\..+";
        protected static final Pattern VOLUME_ID_PATTERN = Pattern.compile(VOLUME_ID_PATTERN_STRING);
        
        public static final int PAGE_SEQUENCE_LENGTH = 8;
        
        public static final String PN_MAX_TOTAL_PAGES_ALLOWED = "max.total.pages.allowed";
        public static final String PN_MAX_PAGES_PER_VOLUME_ALLOWED = "max.pages.per.volume.allowed";
        
        protected PolicyCheckerRegistry policyCheckerRegistry = new NullPolicyCheckerRegistry();
        protected boolean retrieveMETS = false;
        
        /**
         * Abstract method to perform the parsing
         * @param string a String object containing a list of IDs to be parsed
         * @return a List of HTRCItemIdentifier objects that are created from the parsing
         * @throws ParseException thrown if the list of IDs contain malformed tokens
         * @throws PolicyViolationException if the list of IDs violate any policies
         */
        public abstract List<? extends HTRCItemIdentifier> parse(String string) throws ParseException, PolicyViolationException;
        
       
        /**
         * Method to set the PolicyCheckerRegistry
         * @param policyCheckerRegistry a PolicyCheckerRegistry object
         */
        public void setPolicyCheckerRegistry(PolicyCheckerRegistry policyCheckerRegistry) {
            this.policyCheckerRegistry = policyCheckerRegistry;
        }
        
        /**
         * Method to set the flag indicating if METS metadata should be retrieved
         * @param retrieveMETS a boolean flag to indicate if METS metadata should be retrieved
         */
        public void setRetrieveMETS(boolean retrieveMETS) {
            this.retrieveMETS = retrieveMETS;
        }
        
        /**
         * Method to check the boolean flag on whether METS metadata should be retrieved
         * @return <code>true</code> if METS metadata is to be retrieved, <code>false</code> otherwise
         */
        public boolean isRetrieveMETS() {
            return this.retrieveMETS;
        }
        
        /**
         * Utility method for generating a page sequence number string
         * @param pageSequence an integer page sequence number
         * @return generated page sequence number as a String
         */
        public static String generatePageSequenceString(int pageSequence) {
            if (pageSequence > 0) {
                String sequenceString = String.format("%08d", pageSequence);
                return sequenceString;
            } else {
                throw new IllegalArgumentException("Page sequence must be positive");
            }
        }
        
    }
    

    /**
     * This class is a Parser for parsing volumeID list
     * 
     * @author Yiming Sun
     *
     */
    static class VolumeIDsParser extends Parser {
        private static final Logger log = Logger.getLogger(VolumeIDsParser.class);
        /**
         * @see edu.indiana.d2i.htrc.access.id.IdentifierParserFactory.Parser#parse(java.lang.String)
         */
        @Override
        public List<IdentifierImpl> parse(String identifiersString) throws ParseException, PolicyViolationException {
            
            Map<String, IdentifierImpl> volumeIDMap = new HashMap<String, IdentifierImpl>();

            StringTokenizer tokenizer = new StringTokenizer(identifiersString, Constants.ID_SEPARATOR);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (!"".equals(token)) {
                    if (log.isDebugEnabled()) log.debug("unverified volume ID: " + token);
                    Matcher matcher = VOLUME_ID_PATTERN.matcher(token);
                    if (matcher.matches()) {
                        if (log.isDebugEnabled()) log.debug("volume ID: " + token);
                        IdentifierImpl identifierImpl = volumeIDMap.get(token);
                        if (identifierImpl == null) {
                            identifierImpl = new IdentifierImpl(token);
                            if (isRetrieveMETS()) {
                                identifierImpl.addMetadataName(HectorResource.CN_VOLUME_METS);
                            }
                            volumeIDMap.put(token, identifierImpl);
                        }
                    } else {
                        throw new ParseException(token, 0);
                    }
                }
            }

            if (volumeIDMap.isEmpty()) {
                throw new ParseException(identifiersString, 0);
            }
            
            List<IdentifierImpl> list = new LinkedList<IdentifierImpl>(volumeIDMap.values());
            return list;
        }
    }
    
    /**
     * This class is a Parser for parsing pageID list
     * @author Yiming Sun
     *
     */
    static class PageIDsParser extends Parser {

        private static Logger log = Logger.getLogger(PageIDsParser.class);
        
        static final int MIN_VOLUME_ID_LENGTH = 4;
        
        /**
         * @see edu.indiana.d2i.htrc.access.id.IdentifierParserFactory.Parser#parse(java.lang.String)
         */
        @Override
        public List<IdentifierImpl> parse(String identifiersString) throws ParseException, PolicyViolationException {

            Map<String, IdentifierImpl> pageIDMap = new HashMap<String, IdentifierImpl>();
            StringTokenizer tokenizer = new StringTokenizer(identifiersString, Constants.ID_SEPARATOR);
            while (tokenizer.hasMoreTokens()) {
                String rawUnit = tokenizer.nextToken().trim();
                
                if (!"".equals(rawUnit)) {
                    int length = rawUnit.length();
                    
                    if (rawUnit.charAt(length - 1) == Constants.PAGE_SEQ_END_MARK) {
                        int lastIndex = rawUnit.lastIndexOf(Constants.PAGE_SEQ_START_MARK);
                        if (lastIndex > MIN_VOLUME_ID_LENGTH) {
                            boolean hasPageSequence = false;
                            
                            String volumeID = rawUnit.substring(0, lastIndex).trim();
                            if (log.isDebugEnabled()) log.debug("unverified volumeID: " + volumeID);
                            
                            Matcher matcher = VOLUME_ID_PATTERN.matcher(volumeID);
                            if (matcher.matches()) {
                                if (log.isDebugEnabled()) log.debug("volume ID: " + volumeID);
                                IdentifierImpl identifierImpl = pageIDMap.get(volumeID);
                                if (identifierImpl == null) {
                                    identifierImpl = new IdentifierImpl(volumeID);
                                    if (isRetrieveMETS()) {
                                        identifierImpl.addMetadataName(HectorResource.CN_VOLUME_METS);
                                    }
                                    pageIDMap.put(volumeID, identifierImpl);
                                }
    
                                String pageSeqRaw = rawUnit.substring(lastIndex + 1, length - 1).trim();
                                
                                StringTokenizer pageTokenizer = new StringTokenizer(pageSeqRaw, Constants.PAGE_SEQ_SEPARATOR);
                                while (pageTokenizer.hasMoreTokens()) {
                                    String pageSeqStr = pageTokenizer.nextToken().trim();
                                    if (!"".equals(pageSeqStr)) {
                                        try {
                                            int pageSeqInt = Integer.valueOf(pageSeqStr);
                                            String pageSequence = generatePageSequenceString(pageSeqInt);
                                            identifierImpl.addPageSequence(pageSequence);
                                            hasPageSequence = true;
                                        } catch (NumberFormatException e) {
                                            log.error("NumberFormatException while parsing page sequence", e);
                                            throw new ParseException(volumeID + Constants.PAGE_SEQ_START_MARK + pageSeqStr + Constants.PAGE_SEQ_END_MARK, 0);
                                        } catch (IllegalArgumentException e) {
                                            log.error("IllegalArgumentException while parsing page sequence", e);
                                            throw new ParseException(volumeID + Constants.PAGE_SEQ_START_MARK + pageSeqStr + Constants.PAGE_SEQ_END_MARK, 0);
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
            
            List<IdentifierImpl> list = new LinkedList<IdentifierImpl>(pageIDMap.values());
            return list;
        }
        
    }
    
    /**
     * This enum indicates the type of an ID
     * @author Yiming Sun
     *
     */
    public static enum IDTypeEnum {
        VOLUME_ID,
        PAGE_ID;
    }
    
    /**
     * Factory method to return an appropriate Parser
     * 
     * @param type the type of IDs to be parsed
     * @param policyCheckerRegistry a PolicyCheckerRegistry object
     * @return an appropriate Parser for the type
     */
    public static Parser getParser(IDTypeEnum type, PolicyCheckerRegistry policyCheckerRegistry) {
        Parser parser = null;
        PolicyCheckerRegistry registry = (policyCheckerRegistry == null) ? new NullPolicyCheckerRegistry() : policyCheckerRegistry;
        
        switch (type) {
        case VOLUME_ID:
        {
            parser = new VolumeIDsParser();
        } 
        break;
        case PAGE_ID:
        {
            parser = new PageIDsParser();
        }
        }
        parser.setPolicyCheckerRegistry(registry);
        return parser;
    }

}


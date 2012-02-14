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
import java.util.List;
import java.util.StringTokenizer;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;

/**
 * @author Yiming Sun
 *
 */
public class HTRCItemIdentifierFactory {
    
    public static abstract class Parser {
        
        public static final int PAGE_SEQUENCE_LENGTH = 8;

        public abstract List<? extends HTRCItemIdentifier> parse(String string) throws ParseException;
        
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
                throw new IllegalArgumentException("Page sequence cannot be negative");
            }
        }
        
    }
    

    static class VolumeIDsParser extends Parser {
        /**
         * @see edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser#parse(java.lang.String)
         */
        @Override
        public List<VolumeIdentifier> parse(String identifiersString) throws ParseException {
            List<VolumeIdentifier> volumeIDList = new ArrayList<VolumeIdentifier>();
            StringTokenizer tokenizer = new StringTokenizer(identifiersString, "|");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (!"".equals(token)) {
                    VolumeIdentifier id = new VolumeIdentifier(token);
                    volumeIDList.add(id);
                }
            }
            
            if (volumeIDList.isEmpty()) {
                throw new ParseException(identifiersString, 0);
            }
            return volumeIDList;
        }
    }
    
    static class PageIDsParser extends Parser {

        static final int MIN_VOLUME_ID_LENGTH = 4;
        /**
         * @see edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser#parse(java.lang.String)
         */
        @Override
        public List<VolumePageIdentifier> parse(String identifiersString) throws ParseException {
            List<VolumePageIdentifier> pageIDList = new ArrayList<VolumePageIdentifier>();
            StringTokenizer tokenizer = new StringTokenizer(identifiersString, "|");
            while (tokenizer.hasMoreTokens()) {
                String rawUnit = tokenizer.nextToken().trim();
                int length = rawUnit.length();
                
                if (rawUnit.charAt(length - 1) == '>') {
                    int lastIndex = rawUnit.lastIndexOf('<');
                    if (lastIndex > MIN_VOLUME_ID_LENGTH) {
                        boolean hasPageSequence = false;
                        
                        String volumeID = rawUnit.substring(0, lastIndex);
                        
                        VolumePageIdentifier id = new VolumePageIdentifier(volumeID);
                        
                        String pageSeqRaw = rawUnit.substring(lastIndex + 1, length - 1);
                        
                        StringTokenizer pageTokenizer = new StringTokenizer(pageSeqRaw, ",");
                        while (pageTokenizer.hasMoreTokens()) {
                            String pageSeqStr = pageTokenizer.nextToken().trim();
                            int pageSeqInt = Integer.valueOf(pageSeqStr);
                            String pageSequence = generatePageSequenceString(pageSeqInt);
                            id.addPageSequence(pageSequence);
                            hasPageSequence = true;
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
            
            return pageIDList;
        }
        
    }
    
    public static enum IDTypeEnum {
        VOLUME_ID(new VolumeIDsParser()),
        PAGE_ID(new PageIDsParser());
        
        final Parser parser;
        IDTypeEnum(Parser parser) {
            this.parser = parser;
        }
        
        Parser getParser() {
            return parser;
        }
    }
    
    public static Parser getParser(IDTypeEnum type) {
        return type.getParser();
    }
    

}


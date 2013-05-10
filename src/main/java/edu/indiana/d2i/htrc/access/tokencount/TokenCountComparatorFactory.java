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
# File:  TokenCountComparatorFactory.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.util.Comparator;
import java.util.Map.Entry;

/**
 * @author Yiming Sun
 *
 */
public class TokenCountComparatorFactory {
    
    static class TokenLexAscendingComparator implements Comparator<Entry<String, Count>>{

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Entry<String, Count> o1, Entry<String, Count> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
        

    }
    
    static class TokenLexDescendingComparator implements Comparator<Entry<String, Count>> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Entry<String, Count> o1, Entry<String, Count> o2) {
            return o2.getKey().compareTo(o1.getKey());
        }

    }
    
    static class TokenCountAscendingComparator implements Comparator<Entry<String, Count>> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Entry<String, Count> o1, Entry<String, Count> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
        

    }


    static class TokenCountDescendingComparator implements Comparator<Entry<String, Count>> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Entry<String, Count> o1, Entry<String, Count> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }
        

    }
    
    static class NullTokenCountComparator implements Comparator<Entry<String, Count>> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Entry<String, Count> o1, Entry<String, Count> o2) {
            return 0;
        }

    }

    public static enum TokenCountComparatorTypeEnum {
        DEFAULT (new NullTokenCountComparator()),
        TOKEN_LEX_ASC (new TokenLexAscendingComparator()),
        TOKEN_LEX_DESC (new TokenLexDescendingComparator()),
        TOKEN_COUNT_ASC (new TokenCountAscendingComparator()),
        TOKEN_COUNT_DESC (new TokenCountDescendingComparator());
        
        private final Comparator<Entry<String, Count>> comparator;
        
        private TokenCountComparatorTypeEnum(Comparator<Entry<String, Count>> comparator) {
            this.comparator = comparator;
        }
        
        protected Comparator<Entry<String, Count>> getComparator() {
            return this.comparator;
        }
        
        
        
    }
    
    public static Comparator<Entry<String, Count>> getComparator(TokenCountComparatorTypeEnum type) {
        return type.getComparator();
    }

}


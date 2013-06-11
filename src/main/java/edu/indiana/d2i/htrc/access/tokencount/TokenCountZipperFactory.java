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
# File:  TokenCountZipperFactory.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.indiana.d2i.htrc.audit.Auditor;

/**
 * @author Yiming Sun
 *
 */
public class TokenCountZipperFactory {
    
    public static class Helper {
        public static final String UTF_8 = "utf-8";
        public static final String LINE_FEED = "\n";

        
        protected static void sendEntry(Map<String, Count> map, String entryName, ZipOutputStream outputStream, Comparator<Entry<String, Count>> comparator) throws IOException {
            try {
                ZipEntry zipEntry = new ZipEntry(entryName);
                outputStream.putNextEntry(zipEntry);
                Set<Entry<String, Count>> entrySet = map.entrySet();
                List<Entry<String, Count>> list = new ArrayList<Entry<String, Count>>(entrySet.size());
                list.addAll(entrySet);
                Collections.sort(list, comparator);
                for (Entry<String, Count> entry : list) {
                    outputStream.write(entry.getKey().getBytes(UTF_8));
                    outputStream.write(" ".getBytes(UTF_8));
                    outputStream.write(Integer.toString(entry.getValue().value()).getBytes(UTF_8));
                    outputStream.write(LINE_FEED.getBytes(UTF_8));
                }
            } finally {
                outputStream.closeEntry();
            }
        }
        protected static void countToken(String token, Map<String, Count> map) {
            Count count = map.get(token);
            if (count == null) {
                count = new Count();
                map.put(token, count);
            }
            count.increment();
        }


        
    }
    
    public static enum TokenCountZipTypeEnum {
        VOLUME_LEVEL,
        PAGE_LEVEL;
    }
    
    public static TokenCountZipper newInstance(TokenCountZipTypeEnum type, Auditor auditor) {
        TokenCountZipper zipper = null;
        switch (type) {
        case VOLUME_LEVEL:
            zipper = new VolumeTokenCountZipper(auditor);
            break;
        case PAGE_LEVEL:
            zipper = new PageTokenCountZipper(auditor);
            break;
        }
        return zipper;
    }
}


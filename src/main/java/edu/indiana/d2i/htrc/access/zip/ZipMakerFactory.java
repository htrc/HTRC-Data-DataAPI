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
# File:  ZipMakerFactory.java
# Description:  This class is a factory for ZipMaker implementations
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.zip;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.indiana.d2i.htrc.access.ZipMaker;
import edu.indiana.d2i.htrc.access.read.HectorResource;
import edu.indiana.d2i.htrc.audit.Auditor;

/**
 * This class is a factory for ZipMaker implementations
 * 
 * @author Yiming Sun
 *
 */
public class ZipMakerFactory {
    
    /**
     * This helper class provides some utility methods for ZipMaker implementations to use
     * @author Yiming Sun
     *
     */
    protected static class Helper {
        
        protected static final String ERROR_ENTRY_HEADING = "Caught the following errors while generating the ZIP file.  This ZIP file is likely to be incomplete and missing some entries." + System.getProperty("line.separator");
        protected static final Map<String, String> metadataSuffixMap = new HashMap<String, String>();
        protected static final Map<String, String> metadataEntryMap = new HashMap<String, String>();
        protected static final String METS_ENTRY_SUFFIX = ".mets.xml";
        protected static final String METS_ENTRY_FILENAME = "mets.xml";
        
        static {
            metadataSuffixMap.put(HectorResource.CN_VOLUME_METS, METS_ENTRY_SUFFIX);
            metadataEntryMap.put(HectorResource.CN_VOLUME_METS, METS_ENTRY_FILENAME);
        }
        
   
        /**
         * Utility method for adding exceptions into the ERROR.err entry
         * 
         * @param outputStream a ZipOutputStream object to which the zip content is written to
         * @param entryOpen a boolean flag indicating if ZipOutputStream has an open ZipEntry. If <code>true</code> it must close the current ZipEntry first
         * @param exceptionList a List of Exception objects to be written to the ERROR.err entry
         * @throws IOException thrown if output to the ZipOutputStream object failed
         */
        protected static void injectErrorEntry(ZipOutputStream outputStream, boolean entryOpen, List<Exception> exceptionList) throws IOException {
            if (entryOpen) {
                outputStream.closeEntry();
            }

            ZipEntry zipEntry = new ZipEntry("ERROR.err");
            outputStream.putNextEntry(zipEntry);
            outputStream.write(ERROR_ENTRY_HEADING.getBytes());
            PrintStream printStream = new PrintStream(outputStream);
            for (Exception e : exceptionList) {
                e.printStackTrace(printStream);
                printStream.println();
            }
            outputStream.closeEntry();
        }
        
        /**
         * Utility method for looking up metadata entry file extension based on the metadata name.
         * 
         * For example, the metadata name of the METS in the backend is volume.METS, but in the zip, the filename extension should be .mets.xml
         * 
         * @param metadataName name of the metadata entry
         * @return the corresponding file extension for the metadata name, or <code>null</code> if the metadata name does not have a mapping
         */
        protected static String getEntrySuffixFromMetadataName(String metadataName) {
            return metadataSuffixMap.get(metadataName);
        }
        
        /**
         * Utility method for looking up metadata entry full filename based on the metadata name.
         * 
         * For example, the metadata name of the METS in the backend is volume.METS, but in the zip, the full filename should be mets.xml
         * 
         * @param metadataName name of the metadata entry
         * @return the corresponding full filename for the metadata name, or <code>null</code> if the metadata name does not have a mapping
         */
        protected static String getEntryFullnameFromMetadataName(String metadataName) {
            return metadataEntryMap.get(metadataName);
        }
        
    }
    
    /**
     * This enum is for the types of ZipMaker implementations
     * @author Yiming Sun
     *
     */
    public static enum ZipTypeEnum {
        SEPARATE_PAGE,
        COMBINE_PAGE,
        WORD_SEQUENCE;
    }
    
    /**
     * Method to get a new instance of a ZipMaker implementations based on the type
     * @param type a ZipTypeEnum object indicating the type of ZipMaker implementation to create
     * @param auditor an Auditor object
     * @return a ZipMaker implementation
     */
    public static ZipMaker newInstance(ZipTypeEnum type, Auditor auditor) {
        ZipMaker zipMaker = null;
        switch (type) {
        case COMBINE_PAGE:
            zipMaker = new CombinePageVolumeZipMaker(auditor);
            break;
        case SEPARATE_PAGE:
            zipMaker = new SeparatePageVolumeZipMaker(auditor);
            break;
        case WORD_SEQUENCE:
            zipMaker = new WordSequenceZipMaker(auditor);
            break;
        }
        
        return zipMaker;
    }

}


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
# File:  ZipMakerFactory.java
# Description:  
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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.indiana.d2i.htrc.access.ZipMaker;
import edu.indiana.d2i.htrc.audit.Auditor;

/**
 * @author Yiming Sun
 *
 */
public class ZipMakerFactory {
    
    protected static class Helper {
        protected static final String ERROR_ENTRY_HEADING = "Caught the following errors while generating the ZIP file.  This ZIP file is likely to be incomplete and missing some entries." + System.getProperty("line.separator");
        
        protected static void injectErrorEntry(ZipOutputStream outputStream, boolean entryOpen, Exception e) throws IOException {
            if (entryOpen) {
                outputStream.closeEntry();
            }
            ZipEntry zipEntry = new ZipEntry("ERROR.err");
            outputStream.putNextEntry(zipEntry);
            outputStream.write(ERROR_ENTRY_HEADING.getBytes());
            PrintStream printStream = new PrintStream(outputStream);
            e.printStackTrace(printStream);
            
            outputStream.closeEntry();
        }
        
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
    }
    
    public static enum ZipTypeEnum {
        SEPARATE_PAGE,
        COMBINE_PAGE,
        WORD_SEQUENCE;
    }
    
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


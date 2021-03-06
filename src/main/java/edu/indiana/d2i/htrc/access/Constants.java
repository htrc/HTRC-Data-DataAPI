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
# File:  Constants.java
# Description: This class holds constants used throughout other codes
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

/**
 * This class holds constants used throughout other codes
 * 
 * @author Yiming Sun
 *
 */
public final class Constants {
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    
    public static final String CONTENT_TYPE_APPLICATION_ZIP = "application/zip";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT_XHTML = "text/xhtml";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    
    public static final String FILENAME_VOLUMES_ZIP = "filename=\"volumes.zip\"";
    public static final String FILENAME_PAGES_ZIP = "filename=\"pages.zip\"";
    public static final String FILENAME_TOKENCOUNT_ZIP = "filename=\"tokencount.zip\"";
    
    public static final String ID_SEPARATOR = "|";
    public static final char PAGE_SEQ_START_MARK = '[';
    public static final char PAGE_SEQ_END_MARK = ']';
    public static final String PAGE_SEQ_SEPARATOR = ",";
    public static final char PAGE_SEQ_PADDING_CHAR = '0';
}


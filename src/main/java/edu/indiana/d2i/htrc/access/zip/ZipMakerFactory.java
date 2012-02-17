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

import edu.indiana.d2i.htrc.access.Auditor;
import edu.indiana.d2i.htrc.access.ZipMaker;

/**
 * @author Yiming Sun
 *
 */
public class ZipMakerFactory {
    
    public static enum ZipTypeEnum {
        SEPARATE_PAGE,
        COMBINE_PAGE,
        WORD_BAG;
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
        case WORD_BAG:
            zipMaker = new WordBagZipMaker(auditor);
            break;
        }
        
        return zipMaker;
    }

}


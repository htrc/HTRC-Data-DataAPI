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
# File:  TokenCountZipper.java
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
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Map.Entry;

import edu.indiana.d2i.htrc.access.VolumeRetriever;

/**
 * @author Yiming Sun
 *
 */
public interface TokenCountZipper {
    public void countAndZip(OutputStream outputStream, VolumeRetriever volumeRetriever, Tokenizer tokenizer, TokenFilter tokenFilter, Comparator<Entry<String, Count>> comparator) throws IOException;

}


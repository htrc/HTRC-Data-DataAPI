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
# File:  TokenCountZipStreamingOutput.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.response;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.tokencount.Count;
import edu.indiana.d2i.htrc.access.tokencount.TokenCountZipper;
import edu.indiana.d2i.htrc.access.tokencount.TokenFilter;
import edu.indiana.d2i.htrc.access.tokencount.Tokenizer;

/**
 * @author Yiming Sun
 *
 */
public class TokenCountZipStreamingOutput implements StreamingOutput {
    
    protected final VolumeRetriever volumeRetriever;
    protected final TokenCountZipper tokenCountZipper;
    protected final Tokenizer tokenizer;
    protected final TokenFilter tokenFilter;
    Comparator<Entry<String, Count>> comparator;
    
    public TokenCountZipStreamingOutput(VolumeRetriever volumeRetriever, TokenCountZipper tokenCountZipper, Tokenizer tokenizer, TokenFilter tokenFilter, Comparator<Entry<String, Count>> comparator) {
        this.volumeRetriever = volumeRetriever;
        this.tokenCountZipper = tokenCountZipper;
        this.tokenizer = tokenizer;
        this.tokenFilter = tokenFilter;
        this.comparator = comparator;
    }

    /**
     * @see javax.ws.rs.core.StreamingOutput#write(java.io.OutputStream)
     */
    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        this.tokenCountZipper.countAndZip(outputStream, volumeRetriever, tokenizer, tokenFilter, comparator);
    }

}


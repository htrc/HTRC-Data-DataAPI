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
# File:  ZipMaker.java
# Description:  Interface definition for zipping volume content into an OutputStream
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.io.IOException;
import java.io.OutputStream;

import edu.indiana.d2i.htrc.access.exception.DataAPIException;

/**
 * Interface definition for zipping volume content into an OutputStream
 * 
 * @author Yiming Sun
 *
 */
public interface ZipMaker {

    /**
     * Method to zip volume content into the provided OutputStream
     * @param outputStream an OutputStream object to which zipped volume content is written
     * @param volumeRetriever an VolumeRetriever object for retrieving volume content
     * @throws IOException thrown if the zip process encounters errors
     * @throws DataAPIException thrown if retrieval of volumes encounters errors
     */
    public void makeZipFile(OutputStream outputStream, VolumeRetriever volumeRetriever) throws IOException, DataAPIException;
}


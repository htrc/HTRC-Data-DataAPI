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
# File:  SeparatePageVolumeZipMaker.java
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
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.indiana.d2i.htrc.access.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.ZipMaker;

/**
 * @author Yiming Sun
 *
 */
public class SeparatePageVolumeZipMaker implements ZipMaker {

    SeparatePageVolumeZipMaker() {
        
    }
    /**
     * @see edu.indiana.d2i.htrc.access.ZipMaker#makeZipFile(java.io.OutputStream, java.lang.String, edu.indiana.d2i.htrc.access.VolumeReader)
     */
    @Override
    public void makeZipFile(OutputStream outputStream, VolumeRetriever volumeRetriever) throws IOException, KeyNotFoundException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setLevel(Deflater.BEST_COMPRESSION);
        while (volumeRetriever.hasMoreVolumes()) {
            VolumeReader volumeReader = volumeRetriever.nextVolume();
            String volumeIDDirName = volumeReader.getPairtreeCleanedVolumeID() + "/";
            ZipEntry zipEntry = new ZipEntry(volumeIDDirName);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.closeEntry();
            
            while (volumeReader.hasMorePages()) {
                PageReader pageReader = volumeReader.nextPage();
                String pageSequence = pageReader.getPageSequence();
                ZipEntry pageContentsEntry = new ZipEntry(volumeIDDirName + pageSequence + ".txt");
                zipOutputStream.putNextEntry(pageContentsEntry);
                zipOutputStream.write(pageReader.getPageContent().getBytes());
                zipOutputStream.closeEntry();
            }
            
        }
        zipOutputStream.close();

    }

}


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

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.Auditor;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.ZipMaker;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;

/**
 * @author Yiming Sun
 *
 */
public class SeparatePageVolumeZipMaker implements ZipMaker {

    private static Logger log = Logger.getLogger(SeparatePageVolumeZipMaker.class);
    protected final Auditor auditor;
    
    SeparatePageVolumeZipMaker(Auditor auditor) {
        this.auditor = auditor;
    }
    /**
     * @see edu.indiana.d2i.htrc.access.ZipMaker#makeZipFile(java.io.OutputStream, java.lang.String, edu.indiana.d2i.htrc.access.VolumeReader)
     */
    @Override
    public void makeZipFile(OutputStream outputStream, VolumeRetriever volumeRetriever) throws IOException, KeyNotFoundException, PolicyViolationException, RepositoryException {
        
        boolean entryOpen = false;
        
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setLevel(Deflater.BEST_COMPRESSION);
        
        try {
            while (volumeRetriever.hasMoreVolumes()) {
                VolumeReader volumeReader = volumeRetriever.nextVolume();
                String volumeIDDirName = volumeReader.getPairtreeCleanedVolumeID() + "/";
                ZipEntry zipEntry = new ZipEntry(volumeIDDirName);
                zipOutputStream.putNextEntry(zipEntry);
                entryOpen = true;
                zipOutputStream.closeEntry();
                entryOpen = false;
                while (volumeReader.hasMorePages()) {
                    PageReader pageReader = volumeReader.nextPage();
                    String pageSequence = pageReader.getPageSequence();
                    ZipEntry pageContentsEntry = new ZipEntry(volumeIDDirName + pageSequence + ".txt");
                    zipOutputStream.putNextEntry(pageContentsEntry);
                    entryOpen = true;
                    zipOutputStream.write(pageReader.getPageContent().getBytes());
                    zipOutputStream.closeEntry();
                    entryOpen = false;
                    auditor.audit("ACCESSED", volumeReader.getVolumeID(), pageSequence);
                }
                
            }
        } catch (KeyNotFoundException e) {
            log.error("KeyNotFoundException", e);
            log.info("Caught exception while making zip file, injecting ERROR.err entry");
            ZipMakerFactory.Helper.injectErrorEntry(zipOutputStream, entryOpen, e);
            throw e;
        } catch (PolicyViolationException e) {
            log.error("PolicyViolationException", e);
            log.info("Caught exception while making zip file, injecting ERROR.err entry");
            ZipMakerFactory.Helper.injectErrorEntry(zipOutputStream, entryOpen, e);
            throw e;
        } catch (RepositoryException e) {
            log.error("RepositoryException", e);
            log.info("Caught exception while making zip file, injecting ERROR.err entry");
            ZipMakerFactory.Helper.injectErrorEntry(zipOutputStream, entryOpen, e);
            throw e;
        } finally {
            zipOutputStream.close();
        }

    }

}


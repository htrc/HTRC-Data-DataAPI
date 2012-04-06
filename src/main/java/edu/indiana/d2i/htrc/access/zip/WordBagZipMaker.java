/*
#
# Copyright 2012 The Trustees of Indiana University
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
# File:  WordBagZipMaker.java
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

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.ZipMaker;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.audit.Auditor;

public class WordBagZipMaker implements ZipMaker {
    
    private static Logger log = Logger.getLogger(WordBagZipMaker.class);
    protected final Auditor auditor;
    
    WordBagZipMaker(Auditor auditor) {
        this.auditor = auditor;
    }

    /**
     * @see edu.indiana.d2i.htrc.access.ZipMaker#makeZipFile(java.io.OutputStream, edu.indiana.d2i.htrc.access.VolumeRetriever)
     */
    @Override
    public void makeZipFile(OutputStream outputStream, VolumeRetriever volumeRetriever) throws IOException, KeyNotFoundException, PolicyViolationException, RepositoryException {
        
        boolean entryOpen = false;
        
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setLevel(Deflater.BEST_COMPRESSION);
        
        try {
            ZipEntry zipEntry = new ZipEntry("wordbag.txt");
            zipOutputStream.putNextEntry(zipEntry);
            entryOpen = true;
            while (volumeRetriever.hasMoreVolumes()) {
                VolumeReader volumeReader = volumeRetriever.nextVolume();
                while(volumeReader.hasMorePages()) {
                    PageReader pageReader = volumeReader.nextPage();
                    String pageContent = pageReader.getPageContent();
                    zipOutputStream.write(pageContent.getBytes());
                    auditor.audit("ACCESSED", volumeReader.getVolumeID(), pageReader.getPageSequence());
                }
            }
    
            zipOutputStream.closeEntry();
            entryOpen = false;
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


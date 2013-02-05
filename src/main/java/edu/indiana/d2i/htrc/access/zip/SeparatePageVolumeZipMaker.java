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
# Unless required by applicable law or areed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  SeparatePageVolumeZipMaker.java
# Description:  This implementation of the ZipMaker interface creates one ZipEntry for each page from each volume as a text file, and the name of each text file starts with the Pairtree cleaned
# volumeID as a virtual directory, and followed by the 8-digit zero-padded page sequence number and the ".txt" extension.  Metadata entries are also created as individual ZipEntry objects with the
# Pairtree cleaned volumeID as a virtual directory, but the filename of each metadata entry depends on the metadata type, e.g. a METS xml would become mets.xml.  It may also create a special entry
# ERROR.err to record any errors occurred during the asynchronous fetch process.
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.ContentReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.ZipMaker;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.audit.Auditor;

/**
 * This implementation of the ZipMaker interface creates one ZipEntry for each page from each volume as a text file, and the name of each text file starts with the Pairtree cleaned volumeID as a
 * virtual directory, and followed by the 8-digit zero-padded page sequence number and the ".txt" extension.  Metadata entries are also created as individual ZipEntry objects with the Pairtree
 * cleaned volumeID as a virtual directory, but the filename of each metadata entry depends on the metadata type, e.g. a METS xml would become mets.xml.  It may also create a special entry ERROR.err
 * to record any errors occurred during the asynchronous fetch process.
 * 
 * @author Yiming Sun
 *
 */
public class SeparatePageVolumeZipMaker implements ZipMaker {

    private static Logger log = Logger.getLogger(SeparatePageVolumeZipMaker.class);
    protected static final String ACCESSED_ACTION = "ACCESSED";
    protected static final int DEFAULT_PAGE_SEQUENCE_ARRAY_SIZE = 400;
    protected final Auditor auditor;
    
    SeparatePageVolumeZipMaker(Auditor auditor) {
        this.auditor = auditor;
    }
    /**
     * @see edu.indiana.d2i.htrc.access.ZipMaker#makeZipFile(java.io.OutputStream, java.lang.String, edu.indiana.d2i.htrc.access.VolumeReader)
     */
    @Override
    public void makeZipFile(OutputStream outputStream, VolumeRetriever volumeRetriever) throws IOException {
        
        boolean entryOpen = false;
        
        String currentVolumeID = null;
        List<String> currentPageSequences = null;
        
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setLevel(Deflater.NO_COMPRESSION);

        String volumeIDDirName = null;
        
        List<Exception> exceptionList = new LinkedList<Exception>();
        
        while (volumeRetriever.hasMoreVolumes()) {
            try {
                VolumeReader volumeReader = volumeRetriever.nextVolume();
                if (volumeReader != null) {
                    String volumeID = volumeReader.getVolumeID();
    
                    volumeIDDirName = volumeReader.getPairtreeCleanedVolumeID() + "/";
    
                    if (!volumeID.equals(currentVolumeID)) {
                        if (currentVolumeID != null) {
                            auditor.audit(ACCESSED_ACTION, currentVolumeID, currentPageSequences.toArray(new String[0]));
                        }
                        currentVolumeID = volumeID;
                        currentPageSequences = new ArrayList<String>(DEFAULT_PAGE_SEQUENCE_ARRAY_SIZE);
                    }
                    
                    while (volumeReader.hasMorePages()) {
                        ContentReader pageReader = volumeReader.nextPage();
                        String pageSequence = pageReader.getContentName();
                        ZipEntry pageContentsEntry = new ZipEntry(volumeIDDirName + pageSequence + ".txt");
                        zipOutputStream.putNextEntry(pageContentsEntry);
                        entryOpen = true;
                        byte[] pageContent = pageReader.getContent();
                        zipOutputStream.write(pageContent);
                        zipOutputStream.closeEntry();
                        entryOpen = false;
                        currentPageSequences.add(pageSequence);
                    }
                    
                    while (volumeReader.hasMoreMetadata()) {
                        ContentReader metadataReader = volumeReader.nextMetadata();
                        String metadataEntryName = ZipMakerFactory.Helper.getEntryFullnameFromMetadataName(metadataReader.getContentName());
                        if (metadataEntryName != null) {
                            ZipEntry metadataEntry = new ZipEntry(volumeIDDirName + metadataEntryName);
                            zipOutputStream.putNextEntry(metadataEntry);
                            entryOpen = true;
                            byte[] metadataContent = metadataReader.getContent();
                            zipOutputStream.write(metadataContent);
                            zipOutputStream.closeEntry();
                            entryOpen = false;
                            currentPageSequences.add(metadataReader.getContentName());
                        } else {
                            throw new NullPointerException("Unmapped metadata to entry name: " + metadataReader.getContentName());
                        }
                    }
                }
            } catch (KeyNotFoundException e) {
                log.error("KeyNotFoundException", e);
                exceptionList.add(e);
            } catch (PolicyViolationException e) {
                log.error("PolicyViolationException", e);
                exceptionList.add(e);
            } catch (RepositoryException e) {
                log.error("RepositoryException", e);
                exceptionList.add(e);
            } catch (NullPointerException e) {
                log.fatal("unmapped metadata", e);
            }
        }
        
        if (currentVolumeID != null) {
            auditor.audit(ACCESSED_ACTION, currentVolumeID, currentPageSequences.toArray(new String[0]));
        }
        if (entryOpen) {
            zipOutputStream.closeEntry();
            entryOpen = false;
        }
        
        if (!exceptionList.isEmpty()) {
            ZipMakerFactory.Helper.injectErrorEntry(zipOutputStream, entryOpen, exceptionList);
        }
        zipOutputStream.close();
        

    }
}


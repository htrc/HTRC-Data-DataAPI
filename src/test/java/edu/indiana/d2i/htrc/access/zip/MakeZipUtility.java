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
# File:  MakeZipUtility.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.ContentReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;

/**
 * @author Yiming Sun
 *
 */
public class MakeZipUtility {
    public static byte[] getSeparatePageZipAsByteArray(VolumeRetriever volumeRetriever) throws IOException, KeyNotFoundException, PolicyViolationException, RepositoryException {
        ByteArrayOutputStream expectedOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(expectedOutputStream);
        zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
        
        while (volumeRetriever.hasMoreVolumes()) {
            VolumeReader nextVolume = volumeRetriever.nextVolume();
            String safeVolumeID = nextVolume.getPairtreeCleanedVolumeID();

            while (nextVolume.hasMorePages()) {
                ContentReader nextPage = nextVolume.nextPage();
                String pageSeq = nextPage.getContentName();
                byte[] pageContent = nextPage.getContent();
                
                ZipEntry pageEntry = new ZipEntry(safeVolumeID + "/" + pageSeq + ".txt");
                zipOutputStream.putNextEntry(pageEntry);
                zipOutputStream.write(pageContent);
                zipOutputStream.closeEntry();
            }
            
            while (nextVolume.hasMoreMetadata()) {
                ContentReader nextMetadata = nextVolume.nextMetadata();
                byte[] content = nextMetadata.getContent();
                String metsFilename = ZipMakerFactory.Helper.getEntryFullnameFromMetadataName(nextMetadata.getContentName());
                
                ZipEntry metadataEntry = new ZipEntry(safeVolumeID + "/" + metsFilename);
                zipOutputStream.putNextEntry(metadataEntry);
                zipOutputStream.write(content);
                zipOutputStream.closeEntry();
            }
        }
        zipOutputStream.close();
        return expectedOutputStream.toByteArray();
    }
    
    
    public static byte[] getCombinePageZipByteArray(VolumeRetriever volumeRetriever) throws IOException, KeyNotFoundException, PolicyViolationException, RepositoryException {
        ByteArrayOutputStream expectedOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(expectedOutputStream);
        zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
        
        while (volumeRetriever.hasMoreVolumes()) {
            VolumeReader nextVolume = volumeRetriever.nextVolume();
            String safeVolumeID = nextVolume.getPairtreeCleanedVolumeID();
            ZipEntry zipEntry = new ZipEntry(safeVolumeID + ".txt");
            zipOutputStream.putNextEntry(zipEntry);
            
            while (nextVolume.hasMorePages()) {
                ContentReader nextPage = nextVolume.nextPage();
                byte[] pageContent = nextPage.getContent();
                
                zipOutputStream.write(pageContent);
            }
            zipOutputStream.closeEntry();
            
            while (nextVolume.hasMoreMetadata()) {
                ContentReader nextMetadata = nextVolume.nextMetadata();
                String suffix = ZipMakerFactory.Helper.getEntrySuffixFromMetadataName(nextMetadata.getContentName());
                String entryName = nextVolume.getPairtreeCleanedVolumeID() + suffix;
                zipEntry = new ZipEntry(entryName);
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(nextMetadata.getContent());
                zipOutputStream.closeEntry();
            }
        }
        zipOutputStream.close();
        return expectedOutputStream.toByteArray();
    }
    
    public static byte[] getWordSequenceZipByteArray(VolumeRetriever volumeRetriever) throws IOException, KeyNotFoundException, PolicyViolationException, RepositoryException {
        ByteArrayOutputStream expectedOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(expectedOutputStream);
        zipOutputStream.setLevel(Deflater.NO_COMPRESSION);

        ZipEntry zipEntry = new ZipEntry("wordseq.txt");
        zipOutputStream.putNextEntry(zipEntry);

        while (volumeRetriever.hasMoreVolumes()) {
            VolumeReader nextVolume = volumeRetriever.nextVolume();

            while (nextVolume.hasMorePages()) {
                ContentReader nextPage = nextVolume.nextPage();
                byte[] pageContent = nextPage.getContent();
                
                zipOutputStream.write(pageContent);
            }
        }
        zipOutputStream.closeEntry();
        zipOutputStream.close();
        return expectedOutputStream.toByteArray();
    }
}


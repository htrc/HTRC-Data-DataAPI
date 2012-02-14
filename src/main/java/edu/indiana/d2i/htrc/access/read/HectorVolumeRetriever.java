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
# File:  HectorVolumeRetriever.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.read;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HSuperColumn;
import me.prettyprint.hector.api.exceptions.HTimedOutException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SuperColumnQuery;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.HectorResourceSingleton;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory;
import edu.indiana.d2i.htrc.access.read.VolumeReaderImpl.PageReaderImpl;

/**
 * @author Yiming Sun
 *
 */
public class HectorVolumeRetriever implements VolumeRetriever {
    
    private static final Logger log = Logger.getLogger(HectorVolumeRetriever.class);
    
    protected final List<? extends HTRCItemIdentifier> identifiers;
    protected final HectorResourceSingleton hectorResource;
    
    protected Iterator<? extends HTRCItemIdentifier> idIterator = null;
    
    protected final Serializer<String> stringSerializer;
    protected final Serializer<byte[]> bytesArraySerializer;
    protected final Serializer<Integer> integerSerializer;
    
    protected final int maxAttempts;
    protected final long initFailDelay;
    protected final long maxFailDelay;
    
    public HectorVolumeRetriever(List<? extends HTRCItemIdentifier> identifiers, HectorResourceSingleton hectorResource) {
        this.identifiers = identifiers;
        this.hectorResource = hectorResource;
        this.idIterator = identifiers.iterator();
        this.stringSerializer = new StringSerializer();
        this.bytesArraySerializer = new BytesArraySerializer();
        this.integerSerializer = new IntegerSerializer();
        this.maxAttempts = Integer.valueOf(hectorResource.getParameter(HectorResourceSingleton.PN_HECTOR_ACCESS_MAX_ATTEMPTS));
        this.initFailDelay = Long.valueOf(hectorResource.getParameter(HectorResourceSingleton.PN_HECTOR_ACCESS_FAIL_INIT_DELAY));
        this.maxFailDelay = Long.valueOf(hectorResource.getParameter(HectorResourceSingleton.PN_HECTOR_ACCESS_FAIL_MAX_DELAY));
    }
    
    public boolean hasMoreVolumes() {
        return idIterator.hasNext();
    }
    
    public VolumeReader nextVolume() {
        HTRCItemIdentifier itemIdentifier = idIterator.next();
        
        VolumeReader volumeReader = retrieveVolume(itemIdentifier);
        return volumeReader;
    }
    
    
    protected VolumeReader retrieveVolume(HTRCItemIdentifier identifier) {
        List<String> pageSequences = null;
        Keyspace keyspace = hectorResource.getKeyspace();
        
        if (identifier.getPageSequences() == null) {
            int pageCount = retrievePageCount(identifier, keyspace);
            pageSequences = generatePageSequenceList(pageCount);
        } else {
            pageSequences = identifier.getPageSequences();
        }
        VolumeReaderImpl fullVolumeReader = new VolumeReaderImpl(identifier);
        List<PageReader> pageContents = retrievePageContents(identifier, pageSequences, keyspace);
        fullVolumeReader.setPages(pageContents);
        return fullVolumeReader;
    }
    
    
    
    protected List<PageReader> retrievePageContents(HTRCItemIdentifier identifier, List<String> pageSequences, Keyspace keyspace) throws HTimedOutException {
        List<PageReader> pageReaders = new ArrayList<PageReader>();
        
        
        String volumeID = identifier.getVolumeID();
        
        SuperColumnQuery<String, String, String, byte[]> superColumnQuery = HFactory.createSuperColumnQuery(keyspace, stringSerializer, stringSerializer, stringSerializer, bytesArraySerializer);
        superColumnQuery.setColumnFamily(hectorResource.getParameter(HectorResourceSingleton.PN_VOLUME_CONTENT_SCF_NAME));
        superColumnQuery.setKey(volumeID);
        
        for (String pageSequence : pageSequences) {

            superColumnQuery.setSuperName(pageSequence);
            boolean success = false;
            int attemptsLeft = maxAttempts;
            long failDelay = initFailDelay;

            do {
                
                try {
                    QueryResult<HSuperColumn<String, String, byte[]>> queryResult = superColumnQuery.execute();
                    
                    if (queryResult != null) {
                        HSuperColumn<String, String, byte[]> hSuperColumn = queryResult.get();
                        if (hSuperColumn != null) {
                            List<HColumn<String, byte[]>> columns = hSuperColumn.getColumns();
                            for (HColumn<String, byte[]> column : columns) {
                                if ("contents".endsWith(column.getName())) {
                                    String pageContents = stringSerializer.fromBytes(column.getValue());
                                    PageReader pageReader = new PageReaderImpl(pageSequence, pageContents); 
                                    pageReaders.add(pageReader);
                                    success = true;
                                    break;
                                }
                            }
                        }
                        
                    }
                } catch (HTimedOutException e) {
                    if (attemptsLeft > 0) {
                        attemptsLeft--;
                        
                        try {
                            Thread.sleep(failDelay);
                            
                        } catch (InterruptedException ie) {
                            log.warn("Interrupted while backing off on HTimedOutException", ie);
                        }
                        
                        failDelay = (failDelay * 2) > maxFailDelay ? maxFailDelay : (failDelay * 2);
                    } else {
                        log.error("Failed to retrieve volume metadata: " + volumeID, e);
                        throw e;
                    }
                }
                    
            } while (!success && attemptsLeft > 0);
            
            
        }
        
        return pageReaders;
        
    }
    
    private List<String> generatePageSequenceList(int pageCount) {
        List<String> pageSequences = new ArrayList<String>();
        for (int i = 1; i < pageCount; i++) {
            String pageSequence = HTRCItemIdentifierFactory.Parser.generatePageSequenceString(i);
            pageSequences.add(pageSequence);
        }
        return Collections.<String>unmodifiableList(pageSequences);
    }
    
//    private static final int PAGE_SEQUENCE_LENGTH = 8;
//    private String generatePageSequenceString (int pageSequence) {
//        String sequenceString = Integer.toString(pageSequence);
//        StringBuilder builder = new StringBuilder();
//        int padLength = PAGE_SEQUENCE_LENGTH - sequenceString.length();
//        
//        for (int i = 0; i < padLength; i++) {
//            builder.append('0');
//        }
//        builder.append(sequenceString);
//        return builder.toString();
//    }
    
    protected int retrievePageCount(HTRCItemIdentifier identifier, Keyspace keyspace) throws HTimedOutException {
        int pageCount = 0;
        
        boolean success = false;
        int attemptsLeft = maxAttempts;
        long failDelay = initFailDelay;
        

        String volumeID = identifier.getVolumeID();
        
        SuperColumnQuery<String, String, String, byte[]> superColumnQuery = HFactory.createSuperColumnQuery(keyspace, stringSerializer, stringSerializer, stringSerializer, bytesArraySerializer);
        superColumnQuery.setColumnFamily(hectorResource.getParameter(HectorResourceSingleton.PN_VOLUME_CONTENT_SCF_NAME));
        superColumnQuery.setKey(volumeID);
        superColumnQuery.setSuperName("metadata");
        
        do {
            
            try {
                QueryResult<HSuperColumn<String, String, byte[]>> queryResult = superColumnQuery.execute();
                
                if (queryResult != null) {
                    HSuperColumn<String, String, byte[]> hSuperColumn = queryResult.get();
                    if (hSuperColumn != null) {
                        List<HColumn<String, byte[]>> columns = hSuperColumn.getColumns();
                        for (HColumn<String, byte[]> column : columns) {
                            if ("pageCount".endsWith(column.getName())) {
                                pageCount = integerSerializer.fromBytes(column.getValue());
                                success = true;
                                break;
                            }
                        }
                    }
                    
                }
            } catch (HTimedOutException e) {
                if (attemptsLeft > 0) {
                    attemptsLeft--;
                    
                    try {
                        Thread.sleep(failDelay);
                        
                    } catch (InterruptedException ie) {
                        log.warn("Interrupted while backing off on HTimedOutException", ie);
                    }
                    
                    failDelay = (failDelay * 2) > maxFailDelay ? maxFailDelay : (failDelay * 2);
                } else {
                    log.error("Failed to retrieve volume metadata: " + volumeID, e);
                    throw e;
                }
            }
                
        } while (!success && attemptsLeft > 0);
        
        return pageCount;
    }

}


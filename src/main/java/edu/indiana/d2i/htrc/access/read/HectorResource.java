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
# File:  HectorResource.java
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
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.exceptions.HTimedOutException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.VolumeInfo;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.PageReader;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.read.HectorResource.VolumeReaderImpl.PageReaderImpl;
import gov.loc.repository.pairtree.Pairtree;

/**
 * @author Yiming Sun
 *
 */
public abstract class HectorResource {
    public static enum CopyrightEnum {
        PUBLIC_DOMAIN,
        IN_COPYRIGHT;
    }
    
    
    public static class BasicVolumeInfo extends VolumeInfo {

        protected int pageCount;
        protected CopyrightEnum copyright;
        
        protected BasicVolumeInfo(String volumeID) {
            super(volumeID);
            this.pageCount = -1;
            this.copyright = null;
        }
        
        protected void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }
        
        protected void setCopyright(CopyrightEnum copyright) {
            this.copyright = copyright;
        }
        /**
         * @see edu.indiana.d2i.htrc.access.RequestValidityChecker.VolumeInfo#getPageCount()
         */
        @Override
        public int getPageCount() {
            return pageCount;
        }

        /**
         * @see edu.indiana.d2i.htrc.access.RequestValidityChecker.VolumeInfo#getCopyright()
         */
        @Override
        public CopyrightEnum getCopyright() {
            return copyright;
        }

    }

    
    public static class VolumeReaderImpl implements VolumeReader {
        
        public static class PageReaderImpl implements PageReader {

            protected final String pageSequenceStr;
            protected final String pageContents;

            protected PageReaderImpl(String pageSequenceStr, String pageContents) {
                this.pageSequenceStr = pageSequenceStr;
                this.pageContents = pageContents;
            }
            /**
             * @see edu.indiana.d2i.htrc.access.PageReader#getPageSequence()
             */
            @Override
            public String getPageSequence() {
                return pageSequenceStr;
            }

            /**
             * @see edu.indiana.d2i.htrc.access.PageReader#getPageContent()
             */
            @Override
            public String getPageContent() {
                return pageContents;
            }
        }
        
        protected final String volumeID;
        protected final String pairtreeCleanedVolumeID;
        protected List<PageReader> pages;
        protected Iterator<PageReader> pageIterator;
        
        
        protected VolumeReaderImpl(HTRCItemIdentifier identifier) {
            Pairtree pairtree = new Pairtree();
            this.volumeID = identifier.getVolumeID();
            this.pairtreeCleanedVolumeID = getPrefix(volumeID) + "." + pairtree.cleanId(getHeadlessVolumeID(volumeID));
            this.pages = null;
            this.pageIterator = null;
        }

        public void setPages(List<PageReader> pages) {
            this.pages = Collections.unmodifiableList(pages);
            this.pageIterator = pages.iterator();
        }
        
        private String getPrefix(String volumeID) {
            int indexOf = volumeID.indexOf('.');
            return volumeID.substring(0, indexOf);
        }
        
        private String getHeadlessVolumeID(String volumeID) {
            int indexOf = volumeID.indexOf('.');
            return volumeID.substring(indexOf + 1);
        }
        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#getVolumeID()
         */
        @Override
        public String getVolumeID() {
            return this.volumeID;
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#getPairtreeCleanedVolumeID()
         */
        @Override
        public String getPairtreeCleanedVolumeID() {
            return this.pairtreeCleanedVolumeID;
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#nextPage()
         */
        @Override
        public PageReader nextPage() throws KeyNotFoundException {
            return pageIterator.next();
        }

        /**
         * @see edu.indiana.d2i.htrc.access.VolumeReader#hasMorePages()
         */
        @Override
        public boolean hasMorePages() {
            return pageIterator.hasNext();
        }

    }

    private static Logger log = Logger.getLogger(HectorResource.class);
    
    private static boolean initialized = false;
    private static HectorResource singletonInstance = null;
    
    public static final String PN_CASSANDRA_NODE_COUNT = "cassandra.node.count";
    public static final String PN_CASSANDRA_NODE_NAME_ = "cassandra.node.name.";
    public static final String PN_CASSANDRA_CLUSTER_NAME = "cassandra.cluster.name";
    public static final String PN_CASSANDRA_KEYSPACE_NAME = "cassandra.keyspace.name";

    public static final String PN_VOLUME_CONTENT_CF_NAME = "volume.content.cf.name";
    
    public static final String PN_HECTOR_ACCESS_MAX_ATTEMPTS = "hector.access.max.attempts";
    public static final String PN_HECTOR_ACCESS_FAIL_INIT_DELAY = "hector.access.fail.init.delay";
    public static final String PN_HECTOR_ACCESS_FAIL_MAX_DELAY = "hector.access.fail.max.delay";
    
    public static final String CN_VOLUME_PAGECOUNT = "volume.pageCount";
    public static final String CN_VOLUME_COPYRIGHT = "volume.copyright";
    
    public static final String CN_CONTENTS_SUFFIX = ".contents";

    private final ParameterContainer parameterContainer;
    
    private final Cluster cluster;
    private final Keyspace keyspace;
    
    private final Serializer<String> stringSerializer;
    private final Serializer<Integer> integerSerializer;
    private final Serializer<byte[]> bytesArraySerializer;
    
    protected final int maxAttempts;
    protected final long initFailDelay;
    protected final long maxFailDelay;
    


    public HectorResource(final ParameterContainer parameterContainer) {

        this.parameterContainer = parameterContainer;
        
        int cassandraNodeCount = Integer.valueOf(parameterContainer.getParameter(PN_CASSANDRA_NODE_COUNT));

        StringBuilder hostsBuilder = new StringBuilder();
        
        for (int i = 1; i < cassandraNodeCount + 1; i++) {
            String node = parameterContainer.getParameter(PN_CASSANDRA_NODE_NAME_ +  i);

            hostsBuilder.append(node);

            if (i < cassandraNodeCount) {
                hostsBuilder.append(",");
            }
        }
        
    
        String cassandraClusterName = parameterContainer.getParameter(PN_CASSANDRA_CLUSTER_NAME);
        if (log.isDebugEnabled()) log.debug("cassandraClusterName = " + cassandraClusterName);

        String cassandraKeyspaceName = parameterContainer.getParameter(PN_CASSANDRA_KEYSPACE_NAME);
        if (log.isDebugEnabled()) log.debug("cassandraKeyspaceName = " + cassandraKeyspaceName);
        
        
        CassandraHostConfigurator configurator = new CassandraHostConfigurator(hostsBuilder.toString());
        
        cluster = HFactory.getOrCreateCluster(cassandraClusterName, configurator);
        if (log.isDebugEnabled()) log.debug("Hector Cluster object created");
        
        keyspace = HFactory.createKeyspace(cassandraKeyspaceName, cluster);
        if (log.isDebugEnabled()) log.debug("Hector Keyspace object created");
        
        this.stringSerializer = new StringSerializer();
        this.integerSerializer = new IntegerSerializer();
        this.bytesArraySerializer = new BytesArraySerializer();
        
        this.maxAttempts = Integer.valueOf(parameterContainer.getParameter(PN_HECTOR_ACCESS_MAX_ATTEMPTS));
        this.initFailDelay = Long.valueOf(parameterContainer.getParameter(PN_HECTOR_ACCESS_FAIL_INIT_DELAY));
        this.maxFailDelay = Long.valueOf(parameterContainer.getParameter(PN_HECTOR_ACCESS_FAIL_MAX_DELAY));

    }
    
    
    public Cluster getCluster() {
        return cluster;
    }
    
    public Keyspace getKeyspace() {
        return keyspace;
    }
    
    public VolumeInfo getVolumeInfo(String volumeID) throws KeyNotFoundException, HTimedOutException {
        VolumeInfo volumeInfo = null;
        
        boolean success = false;
        int attemptsLeft = maxAttempts;
        long failDelay = initFailDelay;

        
        final String[] columnNames = new String[] {CN_VOLUME_COPYRIGHT, CN_VOLUME_PAGECOUNT};
        
        SliceQuery<String, String, byte[]> sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, bytesArraySerializer);
        sliceQuery.setColumnFamily(parameterContainer.getParameter(PN_VOLUME_CONTENT_CF_NAME));
        sliceQuery.setKey(volumeID);
        sliceQuery.setColumnNames(columnNames);
        
        do {
        
            try {
                QueryResult<ColumnSlice<String, byte[]>> queryResult = sliceQuery.execute();
                success = true;
                if (queryResult != null) {
                    ColumnSlice<String, byte[]> columnSlice = queryResult.get();
                    if (columnSlice != null) {
                        List<HColumn<String, byte[]>> columns = columnSlice.getColumns();
                        if (columns != null && !columns.isEmpty()) {
                            BasicVolumeInfo basicVolInfo = new BasicVolumeInfo(volumeID);
                            
                            for (HColumn<String, byte[]> hColumn : columns) {
                                String columnName = hColumn.getName();
                                byte[] value = hColumn.getValue();
                                
                                if (value != null) {
                                    if (CN_VOLUME_COPYRIGHT.equals(columnName)) {
                                        basicVolInfo.setCopyright(CopyrightEnum.valueOf(stringSerializer.fromBytes(hColumn.getValue())));
                                    } else if (CN_VOLUME_PAGECOUNT.equals(columnName)) {
                                        basicVolInfo.setPageCount(integerSerializer.fromBytes(hColumn.getValue()));
                                    }
                                } else {
                                    log.error("HColumn.getValue() is null for volume: " + volumeID + " column: " + columnName);
                                    throw new KeyNotFoundException(volumeID);
                                }
                            }
                            volumeInfo = basicVolInfo;
                        } else {
                            log.error("List<HColumn<>> is null or isEmpty for volume: " + volumeID);
                            throw new KeyNotFoundException(volumeID);
                        }
                        
                    } else {
                        log.error("ColumnSlice is null for volume: " + volumeID);
                        throw new KeyNotFoundException(volumeID);
                    }
                    
                } else {
                    log.error("QueryResult is null for volume: " + volumeID);
                    throw new KeyNotFoundException(volumeID);
                }
            }  catch (HTimedOutException e) {
                if (attemptsLeft > 0) {
                    attemptsLeft--;
                    
                    try {
                        Thread.sleep(failDelay);
                        
                    } catch (InterruptedException ie) {
                        log.warn("Interrupted while backing off on HTimedOutException", ie);
                    }
                    
                    failDelay = (failDelay * 2) > maxFailDelay ? maxFailDelay : (failDelay * 2);
                } else {
                    log.error("Failed to get VolumeInfo: " + volumeID, e);
                    throw e;
                }
            }
                
        } while (!success && attemptsLeft > 0);
        
        return volumeInfo;
        
    }
    
    
    
    public List<PageReader> retrievePageContents(String volumeID, List<String> pageSequences) throws KeyNotFoundException, HTimedOutException {
        List<PageReader> pageReaders = new ArrayList<PageReader>();
        
        boolean success = false;
        int attemptsLeft = maxAttempts;
        long failDelay = initFailDelay;

        String[] columnNames = pageSequences.toArray(new String[0]);
        
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] += CN_CONTENTS_SUFFIX;
        }

        SliceQuery<String, String, String> sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
        
        sliceQuery.setColumnFamily(parameterContainer.getParameter(PN_VOLUME_CONTENT_CF_NAME));
        sliceQuery.setKey(volumeID);
        sliceQuery.setColumnNames(columnNames);

        do {
            
            try {
        
                QueryResult<ColumnSlice<String, String>> queryResult = sliceQuery.execute();
                success = true;
                if (queryResult != null) {
                    ColumnSlice<String, String> columnSlice = queryResult.get();
                    if (columnSlice != null) {
                        List<HColumn<String, String>> columns = columnSlice.getColumns();
                        if (columns != null && !columns.isEmpty()) {
                            int index = 0;
                            for (HColumn<String, String> column : columns) {
                                String name = column.getName();
                                if (name.equals(columnNames[index])) {
                                    PageReader pageReader = new PageReaderImpl(pageSequences.get(index), column.getValue());
                                    pageReaders.add(pageReader);
                                    index++;
                                } else {
                                    log.error("Column names mismatch. Expected " + columnNames[index] + " Actual: " + name);
                                    throw new KeyNotFoundException(volumeID + "<" + columnNames[index] + ">");
                                }
                            }
                            
                            if (index < columnNames.length) {
                                log.error("Column count mismatch. Expected " + columnNames.length + " Actual: " + index);
                                throw new KeyNotFoundException(volumeID + "<" + columnNames[index] + ">");
                            }
                        } else {
                            log.error("List<HColumn<>> is null or isEmpty for volume: " + volumeID);
                            throw new KeyNotFoundException(volumeID);
                        }
                    } else {
                        log.error("ColumnSlice is null for volume: " + volumeID);
                        throw new KeyNotFoundException(volumeID);
                    }
                } else {
                    log.error("QueryResult is null for volume: " + volumeID);
                    throw new KeyNotFoundException(volumeID);
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
                    log.error("Failed to get page contents: " + volumeID, e);
                    throw e;
                }
            }
        
        } while (!success && attemptsLeft > 0);
        
        return pageReaders;
        
    }
    
    public VolumeReaderImpl createVolumeReader(HTRCItemIdentifier identifier) {
        VolumeReaderImpl volumeReaderImpl = new VolumeReaderImpl(identifier);
        return volumeReaderImpl;
    }
    
    
    public static synchronized void initSingletonInstance(ParameterContainer parameterContainer) {
        if (!initialized) {
            class HectorResourceSubClass extends HectorResource {
                HectorResourceSubClass(ParameterContainer parameterContainer) {
                    super(parameterContainer);
                }
            };
            singletonInstance = new HectorResourceSubClass(parameterContainer);
            initialized = true;
        }
    }

    public static HectorResource getSingletonInstance() {
        assert(initialized);
        return singletonInstance;
    }
    
    public void shutdown() {
        cluster.getConnectionManager().shutdown();
        log.info("HectorResource shutdown");
    }


}

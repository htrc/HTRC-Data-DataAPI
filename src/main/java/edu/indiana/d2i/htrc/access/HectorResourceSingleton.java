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
# File:  HectorResourceSingleton.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

/**
 * @author Yiming Sun
 *
 */
public class HectorResourceSingleton {
    
    
    public static final String PN_CASSANDRA_NODE_COUNT = "cassandra.node.count";
    public static final String PN_CASSANDRA_NODE_NAME_ = "cassandra.node.name.";
    public static final String PN_CASSANDRA_CLUSTER_NAME = "cassandra.cluster.name";
    public static final String PN_CASSANDRA_KEYSPACE_NAME = "cassandra.keyspace.name";

    public static final String PN_VOLUME_CONTENT_SCF_NAME = "volume.content.scf.name";
    
    public static final String PN_HECTOR_ACCESS_MAX_ATTEMPTS = "hector.access.max.attempts";
    public static final String PN_HECTOR_ACCESS_FAIL_INIT_DELAY = "hector.access.fail.init.delay";
    public static final String PN_HECTOR_ACCESS_FAIL_MAX_DELAY = "hector.access.fail.max.delay";

    
    private static HectorResourceSingleton instance = null;
    private static boolean initialized = false;
    
    private Cluster cluster = null;
    private Keyspace keyspace = null;
    private Map<String, String> stringParams = null;
//    private Map<String, Integer> intParams = null;
    
    private HectorResourceSingleton(final ServletConfig servletConfig) {
        
        stringParams = new HashMap<String, String>();
        
        String cassandraNodeCountStr = (String)servletConfig.getInitParameter(PN_CASSANDRA_NODE_COUNT);
        stringParams.put(PN_CASSANDRA_NODE_COUNT, cassandraNodeCountStr);
        
        int cassandraNodeCount = Integer.valueOf(cassandraNodeCountStr);
        
//        List<String> cassandraNodes = new ArrayList<String>();
        StringBuilder hostsBuilder = new StringBuilder();
        
        for (int i = 1; i < cassandraNodeCount + 1; i++) {
            String node = servletConfig.getInitParameter(PN_CASSANDRA_NODE_NAME_ + i);
            hostsBuilder.append(node);
            stringParams.put(PN_CASSANDRA_NODE_NAME_ + i, node);
            if (i < cassandraNodeCount) {
                hostsBuilder.append(",");
            }
        }
        
    
        String cassandraClusterName = (String)servletConfig.getInitParameter(PN_CASSANDRA_CLUSTER_NAME);
        stringParams.put(PN_CASSANDRA_CLUSTER_NAME, cassandraClusterName);
        
        String cassandraKeyspaceName = (String)servletConfig.getInitParameter(PN_CASSANDRA_KEYSPACE_NAME);
        stringParams.put(PN_CASSANDRA_KEYSPACE_NAME, cassandraKeyspaceName);
        
        String volumeContentSCFName = (String)servletConfig.getInitParameter(PN_VOLUME_CONTENT_SCF_NAME);
        stringParams.put(PN_VOLUME_CONTENT_SCF_NAME, volumeContentSCFName);
        
        String hectorAccessMaxAttemptsStr = (String)servletConfig.getInitParameter(PN_HECTOR_ACCESS_MAX_ATTEMPTS);
        stringParams.put(PN_HECTOR_ACCESS_MAX_ATTEMPTS, hectorAccessMaxAttemptsStr);
        
        String hectorAccessFailInitDelay = (String)servletConfig.getInitParameter(PN_HECTOR_ACCESS_FAIL_INIT_DELAY);
        stringParams.put(PN_HECTOR_ACCESS_FAIL_INIT_DELAY, hectorAccessFailInitDelay);
        
        String hectorAccessFailMaxDelay = (String)servletConfig.getInitParameter(PN_HECTOR_ACCESS_FAIL_MAX_DELAY);
        stringParams.put(PN_HECTOR_ACCESS_FAIL_MAX_DELAY, hectorAccessFailMaxDelay);
        
        
        
        CassandraHostConfigurator configurator = new CassandraHostConfigurator(hostsBuilder.toString());
        
        cluster = HFactory.getOrCreateCluster(cassandraClusterName, configurator);
        
        keyspace = HFactory.createKeyspace(cassandraKeyspaceName, cluster);
    }
    
    public static synchronized HectorResourceSingleton getInstance() {
        assert(initialized);
        return instance;
    }
    
    public Cluster getCluster() {
        return cluster;
    }
    
    public Keyspace getKeyspace() {
        return keyspace;
    }
    
    public String getParameter(String paramName) {
        return stringParams.get(paramName);
    }
    

    static synchronized void init(final ServletConfig servletConfig) {
        if (instance == null) {
            instance = new HectorResourceSingleton(servletConfig);
            initialized = true;
        }
    }
    
    

}


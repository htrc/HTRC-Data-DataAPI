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
# File:  HTRCDataAccessApplication.java
# Description: This class extends the JAX-RS Application class to perform global initialization
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.indiana.d2i.htrc.access.async.AsyncFetchManager;
import edu.indiana.d2i.htrc.access.async.ThrottledVolumeRetrieverImpl;
import edu.indiana.d2i.htrc.access.policy.MaxPagesPerVolumePolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxTotalPagesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxVolumesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.PolicyCheckerRegistryImpl;
import edu.indiana.d2i.htrc.access.read.HectorResource;
import edu.indiana.d2i.htrc.audit.Auditor;
import edu.indiana.d2i.htrc.audit.AuditorFactory;

/**
 * This class extends the JAX-RS Application class to perform global initialization
 * 
 * @author Yiming Sun
 *
 */
@ApplicationPath("/")
public class HTRCDataAccessApplication extends Application {
    
    private static Logger log = Logger.getLogger(HTRCDataAccessApplication.class);
    private Auditor auditor = null;
    
    @Context private ServletConfig servletConfig;
    
    
    /**
     * @see javax.ws.rs.core.Application#getClasses()
     */
    @Override
    public Set<Class<?>> getClasses() {
        if (log.isDebugEnabled()) log.debug("@Override getClasses() called");
        Set<Class<?>> hashSet = new HashSet<Class<?>>();
        hashSet.add(VolumeAccessResource.class);
        hashSet.add(PageAccessResource.class);
        return hashSet;
    }

    /**
     * Method to perform initialization, including reading configuration parameters and instantiate objects
     */
    @PostConstruct
    private void init() {
        if (log.isDebugEnabled()) log.debug("@PostConstruct init() called");
        configureLogger(servletConfig);

        loadParametersToContainer(servletConfig);
        
        ParameterContainer parameterContainer = ParameterContainerSingleton.getInstance();

        AuditorFactory.init(parameterContainer.getParameter("auditor.class"));
        generateSystemAuditor();
        
        
        loadPolicyCheckerRegistry(parameterContainer);
        
        HectorResource.initSingletonInstance(parameterContainer);

        AsyncFetchManager.init(parameterContainer, HectorResource.getSingletonInstance());
        
        ThrottledVolumeRetrieverImpl.init(parameterContainer, HectorResource.getSingletonInstance(), AsyncFetchManager.getInstance());
        
        auditor.log("SERVER_START");
        log.info("Application initialized");
    }
    
    /**
     * Method to configure Log4j logger using an init param value contained in servletConfig
     * 
     * @param servletConfig a ServletConfig object containing init parameter "log4j.properties.path"
     */
    private void configureLogger(ServletConfig servletConfig) {
        String log4jPropertiesPath = (String)servletConfig.getInitParameter("log4j.properties.path");
        PropertyConfigurator.configure(log4jPropertiesPath);
        if (log.isDebugEnabled()) log.debug("logger configured");
    }
    
    /**
     * Method to clean up and release resources before service shutdown
     */
    @PreDestroy
    private void fin() {
        if (log.isDebugEnabled()) log.debug("@PreDestroy fin() called");
        
        HectorResource.getSingletonInstance().shutdown();
        AsyncFetchManager.getInstance().shutdown();

        auditor.log("SERVER_SHUTDOWN");

    }
    
    /**
     * Method to load init parameters into a ParameterContainer object
     * 
     * @param servletConfig a ServletConfig object with init parameters
     */
    private void loadParametersToContainer(ServletConfig servletConfig) {
        ParameterContainer parameterContainer = ParameterContainerSingleton.getInstance();
        Enumeration<?> parameterNames = servletConfig.getInitParameterNames();
        while(parameterNames.hasMoreElements()) {
            String parameterName = (String)parameterNames.nextElement();
            String value = (String)servletConfig.getInitParameter(parameterName);
            if (log.isDebugEnabled()) log.debug(parameterName + " = " + value);
            parameterContainer.setParameter(parameterName, value);
        }
        if (log.isDebugEnabled()) log.debug("finish loading init-params");
    }
    
    /**
     * Method to initialize PolicyCheckerRegistry by loading policies defined in init parameters
     * 
     * @param parameterContainer a ParameterContainer object containing policy parameters
     */
    private void loadPolicyCheckerRegistry(ParameterContainer parameterContainer) {
        PolicyCheckerRegistryImpl registry = PolicyCheckerRegistryImpl.getInstance();
        registry.registerPolicyChecker(MaxVolumesPolicyChecker.POLICY_NAME, new MaxVolumesPolicyChecker(parameterContainer));
        registry.registerPolicyChecker(MaxTotalPagesPolicyChecker.POLICY_NAME, new MaxTotalPagesPolicyChecker(parameterContainer));
        registry.registerPolicyChecker(MaxPagesPerVolumePolicyChecker.POLICY_NAME, new MaxPagesPerVolumePolicyChecker(parameterContainer));
    }
    
    /**
     * Method to create a system auditor for auditing events such as server startup and shutdown
     */
    private void generateSystemAuditor() {
        Map<String, List<String>> systemContextMap = new HashMap<String, List<String>>();
        
        List<String> userIDList = new LinkedList<String>();
        userIDList.add("_SYSTEM_");
        systemContextMap.put(Auditor.KEY_REMOTE_USER, userIDList);
        
        List<String> userIPList = new LinkedList<String>();
        userIPList.add("0.0.0.0");
        systemContextMap.put(Auditor.KEY_REMOTE_ADDR, userIPList);
        
        auditor = AuditorFactory.getAuditor(systemContextMap);
        
    }
    
}


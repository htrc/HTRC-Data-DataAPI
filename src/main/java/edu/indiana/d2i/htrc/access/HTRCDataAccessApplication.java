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
# File:  HTRCDataAccessor.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.indiana.d2i.htrc.access.policy.MaxPagesPerVolumePolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxTotalPagesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxVolumesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.PolicyCheckerRegistryImpl;
import edu.indiana.d2i.htrc.access.read.HectorResource;
import edu.indiana.d2i.htrc.audit.AuditorFactory;

/**
 * @author Yiming Sun
 *
 */
@ApplicationPath("/")
public class HTRCDataAccessApplication extends Application {
    
    private static Logger log = Logger.getLogger(HTRCDataAccessApplication.class);
    @Context private ServletConfig servletConfig;
    
    
    /**
     * @see javax.ws.rs.core.Application#getClasses()
     */
    @Override
    public Set<Class<?>> getClasses() {
//        init();
        if (log.isDebugEnabled()) log.debug("@Override getClasses() called");
        Set<Class<?>> hashSet = new HashSet<Class<?>>();
        hashSet.add(VolumeAccessResource.class);
        hashSet.add(PageAccessResource.class);
        return hashSet;
    }

    @PostConstruct
    private void init() {
        if (log.isDebugEnabled()) log.debug("@PostConstruct init() called");
        configureLogger(servletConfig);

        loadParametersToContainer(servletConfig);
        
        ParameterContainer parameterContainer = ParameterContainerSingleton.getInstance();
        
        loadPolicyCheckerRegistry(parameterContainer);
        
        HectorResource.initSingletonInstance(parameterContainer);

        AuditorFactory.init(parameterContainer.getParameter("auditor.class"));
        
        log.info("Application initialized");
    }
    
    private void configureLogger(ServletConfig servletConfig) {
        String log4jPropertiesPath = (String)servletConfig.getInitParameter("log4j.properties.path");
        PropertyConfigurator.configure(log4jPropertiesPath);
        if (log.isDebugEnabled()) log.debug("logger configured");
    }
    
    @PreDestroy
    private void fin() {
        if (log.isDebugEnabled()) log.debug("@PreDestroy fin() called");
        HectorResource.getSingletonInstance().shutdown();
    }
    
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
    
    private void loadPolicyCheckerRegistry(ParameterContainer parameterContainer) {
        PolicyCheckerRegistryImpl registry = PolicyCheckerRegistryImpl.getInstance();
        registry.registerPolicyChecker(MaxVolumesPolicyChecker.POLICY_NAME, new MaxVolumesPolicyChecker(parameterContainer));
        registry.registerPolicyChecker(MaxTotalPagesPolicyChecker.POLICY_NAME, new MaxTotalPagesPolicyChecker(parameterContainer));
        registry.registerPolicyChecker(MaxPagesPerVolumePolicyChecker.POLICY_NAME, new MaxPagesPerVolumePolicyChecker(parameterContainer));
    }
    
}


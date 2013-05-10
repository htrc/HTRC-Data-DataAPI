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
# File:  SystemResourceContainerSingleton.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Yiming Sun
 *
 */
public class SystemResourcesContainerSingleton {
    
    private static ParameterContainer parameterContainer = null;
    private static SystemResourcesContainerSingleton instance = null;
    
    private ExecutorService tokenCountExecutorService = null;
    
    public static void init(ParameterContainer parameterContainer) {
        SystemResourcesContainerSingleton.parameterContainer = parameterContainer;
    }
    
    public static synchronized SystemResourcesContainerSingleton getInstance() {
        if (instance == null) {
            instance = new SystemResourcesContainerSingleton();
        }
        
        return instance;
    }
    
    public ExecutorService getTokenCountExecutorService() {
        return this.tokenCountExecutorService;
    }
    
    public void shutdown() {
        tokenCountExecutorService.shutdownNow();
    }
    
    private SystemResourcesContainerSingleton() {
        createTokenCountExecutorService(parameterContainer);
    }
    
    private void createTokenCountExecutorService(ParameterContainer parameterContainer) {
        int tokenCountThreadCount = Integer.parseInt(parameterContainer.getParameter("token.count.threads.count"));
        this.tokenCountExecutorService = Executors.newFixedThreadPool(tokenCountThreadCount);
    }
    

}


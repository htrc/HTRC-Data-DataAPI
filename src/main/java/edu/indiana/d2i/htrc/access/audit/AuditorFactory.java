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
# File:  AuditorFactory.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.audit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.Auditor;
import edu.indiana.d2i.htrc.access.ContextExtractor;
import edu.indiana.d2i.htrc.access.ParameterContainer;

/**
 * @author Yiming Sun
 *
 */
public class AuditorFactory {
    
    private static Logger log = Logger.getLogger(AuditorFactory.class);
    private static String auditorClassName = "edu.indiana.d2i.htrc.access.audit.NullAuditor";
    
//    protected static Auditor auditor = new NullAuditor();
    
    public static Auditor getAuditor(ContextExtractor contextExtractor) {
        Auditor auditor = null;
        try {
            Class<?> auditorClass = Class.forName(auditorClassName);
            Constructor<?> constructor = auditorClass.getConstructor(ContextExtractor.class);
            Object instance = constructor.newInstance(contextExtractor);
            if (instance != null) {
                auditor = (Auditor)instance;
            }
        } catch (ClassNotFoundException e) {
            log.error("ClassNotFoundException", e);
        } catch (NoSuchMethodException e) {
            log.error("NoSuchMethodException", e);
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException", e);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException", e);
        } catch (InstantiationException e) {
            log.error("InstantiationException", e);
        }
        
        if (auditor == null) {
            auditor = new NullAuditor(contextExtractor);
        }
        
        
        return auditor;
    }
    
    public static void init(ParameterContainer parameterContainer) {
        String auditorClassName = parameterContainer.getParameter("auditor.class").trim();
        if (auditorClassName != null && !"".equals(auditorClassName)) {
            AuditorFactory.auditorClassName = auditorClassName;
        }
       
    }

}


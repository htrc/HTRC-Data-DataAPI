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
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api-async-experimental
# File:  TypedVolumeReaderImpl.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.async;

import java.util.LinkedList;
import java.util.List;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.exception.DataAPIException;
import edu.indiana.d2i.htrc.access.read.VolumeReaderImpl;

/**
 * @author Yiming Sun
 *
 */
public class ExceptionAwareVolumeReaderImpl extends VolumeReaderImpl implements ExceptionAwareVolumeReader {
    
    protected DataType dataType;
    protected List<ExceptionContainer> exceptions;
    
    public ExceptionAwareVolumeReaderImpl(HTRCItemIdentifier identifier) {
        super(identifier);
        this.dataType = DataType.CONTENT;
        this.exceptions = new LinkedList<ExceptionContainer>();
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.async.ExceptionAwareVolumeReader#getDataType()
     */
    @Override
    public DataType getDataType() {
        return dataType;
    }

    public void addException(DataAPIException exception, ExceptionType exceptionType) {
        ExceptionContainer exceptionContainer = new ExceptionContainer(exception, exceptionType);
        exceptions.add(exceptionContainer);
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.async.ExceptionAwareVolumeReader#releaseExceptions()
     */
    @Override
    public List<ExceptionContainer> releaseExceptions() {
        List<ExceptionContainer> exceptionContainers = exceptions;
        exceptions = null;
        return exceptionContainers;
    }

    
}


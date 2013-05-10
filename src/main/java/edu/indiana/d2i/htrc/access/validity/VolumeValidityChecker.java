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
# File:  VolumeValidityChecker.java
# Description:  This class implements the RequestValidityChecker class to check the validity of volumeIDs in the request
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.validity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.indiana.d2i.htrc.access.Constants;
import edu.indiana.d2i.htrc.access.RequestedItemCoordinates;
import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.PolicyChecker;
import edu.indiana.d2i.htrc.access.PolicyCheckerRegistry;
import edu.indiana.d2i.htrc.access.RequestValidityChecker;
import edu.indiana.d2i.htrc.access.VolumeInfo;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesParserFactory;
import edu.indiana.d2i.htrc.access.policy.MaxPagesPerVolumePolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxTotalPagesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxVolumesPolicyChecker;
import edu.indiana.d2i.htrc.access.read.HectorResource;

/**
 * This class implements the RequestValidityChecker class to check the validity of volumeIDs in the request.  It is deprecated because it is no longer needed with asynchronous fetch
 * 
 * @author Yiming Sun
 *
 * @deprecated
 */
public class VolumeValidityChecker implements RequestValidityChecker  {

    protected final HectorResource hectorResource;
    protected final ParameterContainer parameterContainer;
    protected final PolicyChecker maxVolumesPolicyChecker;
    protected final PolicyChecker maxTotalPagesPolicyChecker;
    protected final PolicyChecker maxPagesPerVolumeChecker;
    
    protected int volumeCount;
    protected int totalPageCount;
    protected int perVolumePageCount;
    
    /**
     * Constructor
     * @param hectorResource a HectorResource object
     * @param parameterContainer a ParameterContainer object
     * @param policyCheckerRegistry a PolicyCheckerRegistry
     */
    public VolumeValidityChecker(HectorResource hectorResource, ParameterContainer parameterContainer, PolicyCheckerRegistry policyCheckerRegistry) {
        this.hectorResource = hectorResource;
        this.parameterContainer = parameterContainer;
        this.maxPagesPerVolumeChecker = policyCheckerRegistry.getPolicyChecker(MaxPagesPerVolumePolicyChecker.POLICY_NAME);
        this.maxTotalPagesPolicyChecker = policyCheckerRegistry.getPolicyChecker(MaxTotalPagesPolicyChecker.POLICY_NAME);
        this.maxVolumesPolicyChecker = policyCheckerRegistry.getPolicyChecker(MaxVolumesPolicyChecker.POLICY_NAME);
        
        volumeCount = 0;
        totalPageCount = 0;
        perVolumePageCount = 0;

    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.RequestValidityChecker#validateRequest(java.util.List)
     */
    @Override
    public Map<String, ? extends VolumeInfo> validateRequest(List<? extends RequestedItemCoordinates> idList) throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        Map<String, VolumeInfo> volumeInfoMap = new HashMap<String, VolumeInfo>();
        
        int previousTotalPageCount = 0;
        
        for (RequestedItemCoordinates id : idList) {
            String volumeID = id.getVolumeID();
            VolumeInfo volumeInfo = hectorResource.getVolumeInfo(volumeID);
            
            volumeCount++;
            maxVolumesPolicyChecker.check(volumeCount, volumeID);

            perVolumePageCount = volumeInfo.getPageCount();
            maxPagesPerVolumeChecker.check(perVolumePageCount, volumeID + Constants.PAGE_SEQ_START_MARK + ItemCoordinatesParserFactory.Parser.generatePageSequenceString(maxPagesPerVolumeChecker.getLimit() + 1) + Constants.PAGE_SEQ_END_MARK);
            
            totalPageCount += perVolumePageCount;
            
            int delta = maxTotalPagesPolicyChecker.getLimit() - previousTotalPageCount + 1;
            String message = (delta > 0) ? volumeID + Constants.PAGE_SEQ_START_MARK + ItemCoordinatesParserFactory.Parser.generatePageSequenceString(delta) + Constants.PAGE_SEQ_END_MARK : volumeID;
            
            maxTotalPagesPolicyChecker.check(totalPageCount, message);
            
            previousTotalPageCount = totalPageCount;
            volumeInfoMap.put(volumeID, volumeInfo);
        }
        return volumeInfoMap;
    }

}

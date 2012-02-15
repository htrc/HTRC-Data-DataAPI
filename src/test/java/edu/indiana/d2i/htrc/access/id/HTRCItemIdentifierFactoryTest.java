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
# File:  TestHTRCItemIdentifierFactory.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.id;

import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.indiana.d2i.htrc.access.HTRCItemIdentifier;
import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.TestParameterContainer;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.IDTypeEnum;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser;
import edu.indiana.d2i.htrc.access.policy.MaxPagesPerVolumePolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxTotalPagesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxVolumesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.PolicyCheckerRegistryImpl;

/**
 * @author Yiming Sun
 *
 */
public class HTRCItemIdentifierFactoryTest {
    
    @Test
    public void testGeneratePageSequenceString() {
        int pageSeq = 1;
        String expectedPageSequence = "00000001";
        
        String pageSequence = Parser.generatePageSequenceString(pageSeq);
        
        Assert.assertEquals(expectedPageSequence, pageSequence);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGeneratePageSequenceStringError() {
        int pageSeq = 0;
        String pageSequence = Parser.generatePageSequenceString(pageSeq);
    }
    
    @Test
    public void testVolumeIDsParserParse1() throws ParseException, PolicyViolationException {
        String rawString = "loc.ark:/13960/t9q23z43f |miun.ajj3079.0001.001";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID);
        String[] expected = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actual = new String[2];

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        actual[0] = list.get(0).getVolumeID();
        actual[1] = list.get(1).getVolumeID();
            
        Assert.assertArrayEquals(expected, actual);
    }
    
    @Test
    public void testVolumeIDsParserParse2() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID);
        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        String actual = list.get(0).getVolumeID();
        String expected = "loc.ark:/13960/t9q23z43f";
        
        Assert.assertEquals(expected, actual);
    }
    

    @Test
    public void testVolumeIDsParserParse3() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f ||  | miun.ajj3079.0001.001";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID);
        String[] expected = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actual = new String[2];

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        actual[0] = list.get(0).getVolumeID();
        actual[1] = list.get(1).getVolumeID();
            
        Assert.assertArrayEquals(expected, actual);
    }
    
    @Test
    public void testVolumeIDsParserParse4() throws ParseException, PolicyViolationException {
        ParameterContainer container = new TestParameterContainer();
        container.setParameter(MaxVolumesPolicyChecker.PN_MAX_VOLUMES_ALLOWED, "2");
//        MaxVolumesPolicyChecker.init(container);
        PolicyCheckerRegistryImpl registry = PolicyCheckerRegistryImpl.getInstance();
        registry.registerPolicyChecker(MaxVolumesPolicyChecker.POLICY_NAME, new MaxVolumesPolicyChecker(container));

        String rawString = "loc.ark:/13960/t9q23z43f |miun.ajj3079.0001.001";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID, registry);
        String[] expected = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actual = new String[2];

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        actual[0] = list.get(0).getVolumeID();
        actual[1] = list.get(1).getVolumeID();
            
        Assert.assertArrayEquals(expected, actual);
    }

    
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError1() throws ParseException, PolicyViolationException  {
        String rawString = " ||  |";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID);

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
    }
    
    @Test(expected = PolicyViolationException.class)
    public void testVolumeIDsParserParseError2() throws ParseException, PolicyViolationException {
        ParameterContainer container = new TestParameterContainer();
        container.setParameter(MaxVolumesPolicyChecker.PN_MAX_VOLUMES_ALLOWED, "2");
//        MaxVolumesPolicyChecker.init(container);
        PolicyCheckerRegistryImpl registry = PolicyCheckerRegistryImpl.getInstance();
        registry.registerPolicyChecker(MaxVolumesPolicyChecker.POLICY_NAME, new MaxVolumesPolicyChecker(container));
        
        String rawString = "loc.ark:/13960/t9q23z43f |  miun.ajj3079.0001.001 | loc.ark:/23456/909192";
        
        Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID, registry);
        
        parser.parse(rawString);
    }
    

    @Test
    public void testPageIDsParserParse1() throws ParseException, PolicyViolationException {
        String rawString = "loc.ark:/13960/t9q23z43f <1,2, 44, 100>|  miun.ajj3079.0001.001<4>";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actualVolIDs = new String[2];

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
        actualVolIDs[0] = list.get(0).getVolumeID();
        actualVolIDs[1] = list.get(1).getVolumeID();
        
        Assert.assertArrayEquals(expectedVolIDs, actualVolIDs);
        
        String[] expectedPageSequences1 = new String[]{"00000001", "00000002", "00000044", "00000100"};
        String[] actualPageSequences1 = list.get(0).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences1.toString(), expectedPageSequences1, actualPageSequences1);
        
        String[] expectedPageSequences2 = new String[]{"00000004"};
        String[] actualPageSequences2 = list.get(1).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences2.toString(), expectedPageSequences2, actualPageSequences2);
        
    }
    
    @Test
    public void testPageIDsParserParse2() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f <1,2, 44, 100> | ||  ";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f"};
        String[] actualVolIDs = new String[1];

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
        actualVolIDs[0] = list.get(0).getVolumeID();
        
        Assert.assertArrayEquals(expectedVolIDs, actualVolIDs);
        
        String[] expectedPageSequences1 = new String[]{"00000001", "00000002", "00000044", "00000100"};
        String[] actualPageSequences1 = list.get(0).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences1.toString(), expectedPageSequences1, actualPageSequences1);
        
        
    }
    

    @Test
    public void testPageIDsParserParse3() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f <1 ,2  , 44  , 100 >  ";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f"};
        String[] actualVolIDs = new String[1];

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
        actualVolIDs[0] = list.get(0).getVolumeID();
        
        Assert.assertArrayEquals(expectedVolIDs, actualVolIDs);
        
        String[] expectedPageSequences1 = new String[]{"00000001", "00000002", "00000044", "00000100"};
        String[] actualPageSequences1 = list.get(0).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences1.toString(), expectedPageSequences1, actualPageSequences1);
        
        
    }

    @Test
    public void testPageIDsParserParse4() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f < , , , 1, ,, 2,,, ,  44,,, 100,, ,, >  ";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f"};
        String[] actualVolIDs = new String[1];

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
        actualVolIDs[0] = list.get(0).getVolumeID();
        
        Assert.assertArrayEquals(expectedVolIDs, actualVolIDs);
        
        String[] expectedPageSequences1 = new String[]{"00000001", "00000002", "00000044", "00000100"};
        String[] actualPageSequences1 = list.get(0).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences1.toString(), expectedPageSequences1, actualPageSequences1);
        
        
    }

    @Test
    public void testPageIDsParserParse5() throws ParseException, PolicyViolationException {
        ParameterContainer container = new TestParameterContainer();
        container.setParameter(MaxVolumesPolicyChecker.PN_MAX_VOLUMES_ALLOWED, "0");
        container.setParameter(MaxTotalPagesPolicyChecker.PN_MAX_TOTAL_PAGES_ALLOWED, "0");
        container.setParameter(MaxPagesPerVolumePolicyChecker.PN_MAX_PAGES_PER_VOLUME_ALLOWED, "0");
        
        PolicyCheckerRegistryImpl registry = PolicyCheckerRegistryImpl.getInstance();

//        MaxVolumesPolicyChecker.init(container);
        registry.registerPolicyChecker(MaxVolumesPolicyChecker.POLICY_NAME, new MaxVolumesPolicyChecker(container));
        
//        MaxTotalPagesPolicyChecker.init(container);
        registry.registerPolicyChecker(MaxTotalPagesPolicyChecker.POLICY_NAME, new MaxTotalPagesPolicyChecker(container));
        
//        MaxPagesPerVolumePolicyChecker.init(container);
        registry.registerPolicyChecker(MaxPagesPerVolumePolicyChecker.POLICY_NAME, new MaxPagesPerVolumePolicyChecker(container));

        String rawString = "loc.ark:/13960/t9q23z43f <1,2, 44, 100>|  miun.ajj3079.0001.001<4>";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID, registry);

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actualVolIDs = new String[2];

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
        actualVolIDs[0] = list.get(0).getVolumeID();
        actualVolIDs[1] = list.get(1).getVolumeID();
        
        Assert.assertArrayEquals(expectedVolIDs, actualVolIDs);
        
        String[] expectedPageSequences1 = new String[]{"00000001", "00000002", "00000044", "00000100"};
        String[] actualPageSequences1 = list.get(0).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences1.toString(), expectedPageSequences1, actualPageSequences1);
        
        String[] expectedPageSequences2 = new String[]{"00000004"};
        String[] actualPageSequences2 = list.get(1).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences2.toString(), expectedPageSequences2, actualPageSequences2);
        
    }

    
    @Test (expected = IllegalArgumentException.class)
    public void testPageIDsParserParseError1() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f < 1, -1 >  ";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
    }

    @Test (expected = NumberFormatException.class)
    public void testPageIDsParserParseError2() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f < one, two >  ";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
    }


    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError3() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f <  ";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
    }


    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError4() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f 3,4,8,10>  ";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
    }

    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError5() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f<>";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
    }
    
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError6() throws ParseException, PolicyViolationException  {
        String rawString = "<1,2,3,4>";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
    }

    
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError7() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID);

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        
    }
    
    @Test (expected = PolicyViolationException.class)
    public void testPageIDsParserParseError8() throws ParseException, PolicyViolationException {
        ParameterContainer container = new TestParameterContainer();
        container.setParameter(MaxTotalPagesPolicyChecker.PN_MAX_TOTAL_PAGES_ALLOWED, "4");
        container.setParameter(MaxPagesPerVolumePolicyChecker.PN_MAX_PAGES_PER_VOLUME_ALLOWED, "4");

        PolicyCheckerRegistryImpl registry = PolicyCheckerRegistryImpl.getInstance();

//        MaxTotalPagesPolicyChecker.init(container);
        registry.registerPolicyChecker(MaxTotalPagesPolicyChecker.POLICY_NAME, new MaxTotalPagesPolicyChecker(container));
        
//        MaxPagesPerVolumePolicyChecker.init(container);
        registry.registerPolicyChecker(MaxPagesPerVolumePolicyChecker.POLICY_NAME, new MaxPagesPerVolumePolicyChecker(container));

        String rawString = "loc.ark:/13960/t9q23z43f <1,2, 44, 100>|  miun.ajj3079.0001.001<4>";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID, registry);

        parser.parse(rawString);
    }


    @Test (expected = PolicyViolationException.class)
    public void testPageIDsParserParseError9() throws ParseException, PolicyViolationException {
        ParameterContainer container = new TestParameterContainer();
        container.setParameter(MaxTotalPagesPolicyChecker.PN_MAX_TOTAL_PAGES_ALLOWED, "100");
        container.setParameter(MaxPagesPerVolumePolicyChecker.PN_MAX_PAGES_PER_VOLUME_ALLOWED, "3");
        
//        MaxTotalPagesPolicyChecker.init(container);
        PolicyCheckerRegistryImpl registry = PolicyCheckerRegistryImpl.getInstance();
        registry.registerPolicyChecker(MaxTotalPagesPolicyChecker.POLICY_NAME, new MaxTotalPagesPolicyChecker(container));
        
//        MaxPagesPerVolumePolicyChecker.init(container);
        registry.registerPolicyChecker(MaxPagesPerVolumePolicyChecker.POLICY_NAME, new MaxPagesPerVolumePolicyChecker(container));

        String rawString = "loc.ark:/13960/t9q23z43f <1,2, 44, 100>|  miun.ajj3079.0001.001<4>";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.PAGE_ID, registry);

        parser.parse(rawString);
    }



}


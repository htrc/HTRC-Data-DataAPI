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

import edu.indiana.d2i.htrc.access.RequestedItemCoordinates;
import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.TestParameterContainer;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesParserFactory.IDTypeEnum;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesParserFactory.Parser;
import edu.indiana.d2i.htrc.access.policy.MaxVolumesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.NullPolicyCheckerRegistry;
import edu.indiana.d2i.htrc.access.policy.PolicyCheckerRegistryImpl;

/**
 * @author Yiming Sun
 *
 */
public class ItemCoordinatesParserFactoryTest {
    
    // This case tests that a positive page number generates the correct page sequence string
    @Test
    public void testGeneratePageSequenceString() {
        int pageSeq = 1;
        String expectedPageSequence = "00000001";
        
        String pageSequence = Parser.generatePageSequenceString(pageSeq);
        
        Assert.assertEquals(expectedPageSequence, pageSequence);
    }
    
    // This case tests that attempts to generate page sequence string from a non-positive page number should raise an IllegalArgumentException 
    @Test(expected = IllegalArgumentException.class)
    public void testGeneratePageSequenceStringError() {
        int pageSeq = 0;
        String pageSequence = Parser.generatePageSequenceString(pageSeq);
    }
    
    // This case tests that volumeIDs separated by a pipe should be correctly parsed
    @Test
    public void testVolumeIDsParserParse1() throws ParseException, PolicyViolationException {
        String rawString = "loc.ark:/13960/t9q23z43f |miun.ajj3079.0001.001";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        String[] expected = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actual = new String[2];

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        actual[0] = list.get(0).getVolumeID();
        actual[1] = list.get(1).getVolumeID();
            
        Assert.assertArrayEquals(expected, actual);
    }
    
    // This case tests that a single volumeID without pipe should be correctly parsed
    @Test
    public void testVolumeIDsParserParse2() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        String actual = list.get(0).getVolumeID();
        String expected = "loc.ark:/13960/t9q23z43f";
        
        Assert.assertEquals(expected, actual);
    }
    
    // This cases tests that volumeIDs separated by multiple pipes and spaces should be tolerated and correctly parsed
    @Test
    public void testVolumeIDsParserParse3() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f ||  | miun.ajj3079.0001.001";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        String[] expected = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actual = new String[2];

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        actual[0] = list.get(0).getVolumeID();
        actual[1] = list.get(1).getVolumeID();
            
        Assert.assertArrayEquals(expected, actual);
    }
    
    // This case tests that redundant volumeIDs in a request should be parsed and coalesced into only one occurance 
    @Test
    public void testVolumeIDsParserParse5() throws ParseException, PolicyViolationException {
        ParameterContainer container = new TestParameterContainer();
        container.setParameter(MaxVolumesPolicyChecker.PN_MAX_VOLUMES_ALLOWED, "2");
//        MaxVolumesPolicyChecker.init(container);
        PolicyCheckerRegistryImpl registry = PolicyCheckerRegistryImpl.getInstance();
        registry.registerPolicyChecker(MaxVolumesPolicyChecker.POLICY_NAME, new MaxVolumesPolicyChecker(container));

        String rawString = "loc.ark:/13960/t9q23z43f |miun.ajj3079.0001.001 | miun.ajj3079.0001.001 | loc.ark:/13960/t9q23z43f  |loc.ark:/13960/t9q23z43f ";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, registry);
        String[] expected = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actual = new String[2];

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        actual[0] = list.get(0).getVolumeID();
        actual[1] = list.get(1).getVolumeID();
            
        Assert.assertArrayEquals(expected, actual);
    }

   
    // This case tests that at least one volumeID should be present or else the parser shall raise a ParseException
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError1() throws ParseException, PolicyViolationException  {
        String rawString = " ||  |";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
    }
    
    // This case tests that a malformed volumeID without any dot shall raise a ParseException
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError2() throws ParseException, PolicyViolationException {
        String rawString = "abc";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
    }
    
    // This case tests that a malformed volumeID with only a prefix shall raise a ParseException
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError3() throws ParseException, PolicyViolationException {
        String rawString = "abc.";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
    }

    // This case tests that a malformed volumeID with only a prefix and a trailing space shall raise a ParseException
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError4() throws ParseException, PolicyViolationException {
        String rawString = "abc. ";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
    }
    
    // This case tests that a malformed volumeID with a dot but no prefix shall raise a ParseException
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError5() throws ParseException, PolicyViolationException {
        String rawString = ".abc";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
    }

    // This case tests that a malformed volumeID with a dot and a leading space but no prefix shall raise a ParseException
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError6() throws ParseException, PolicyViolationException {
        String rawString = " .abc";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
    }

    // This case tests that a malformed volumeID that is only a dot shall raise a ParseException
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError7() throws ParseException, PolicyViolationException {
        String rawString = ".";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
    }
    
    // This case tests that a malformed volumeID among well formed volumeIDs shall raise a ParseException
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError8() throws ParseException, PolicyViolationException {
        String rawString = "aaa.bcde|ac.10211| bad. | xyz.0334";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.VOLUME_ID, new NullPolicyCheckerRegistry());
        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
    }
    

    // This case tests that pageIDs separated by pipe should be properly parsed, and page sequences can be multiple separated by comma, or can be a single pageSequence without comma
    @Test
    public void testPageIDsParserParse1() throws ParseException, PolicyViolationException {
        String rawString = "loc.ark:/13960/t9q23z43f [1,2, 44, 100]|  miun.ajj3079.0001.001[4]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actualVolIDs = new String[2];

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
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
    
    // This case tests that multiple pipes and spaces can be tolerated and ignored by the parser as long as there is at least one valid pageID 
    @Test
    public void testPageIDsParserParse2() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f [1,2, 44, 100] | ||  ";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f"};
        String[] actualVolIDs = new String[1];

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
        actualVolIDs[0] = list.get(0).getVolumeID();
        
        Assert.assertArrayEquals(expectedVolIDs, actualVolIDs);
        
        String[] expectedPageSequences1 = new String[]{"00000001", "00000002", "00000044", "00000100"};
        String[] actualPageSequences1 = list.get(0).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences1.toString(), expectedPageSequences1, actualPageSequences1);
        
        
    }
    

    // This case tests that extra spaces in page sequence list can be tolerated and page sequences are correctly parsed
    @Test
    public void testPageIDsParserParse3() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f [1 ,2  , 44  , 100 ]  ";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f"};
        String[] actualVolIDs = new String[1];

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
        actualVolIDs[0] = list.get(0).getVolumeID();
        
        Assert.assertArrayEquals(expectedVolIDs, actualVolIDs);
        
        String[] expectedPageSequences1 = new String[]{"00000001", "00000002", "00000044", "00000100"};
        String[] actualPageSequences1 = list.get(0).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences1.toString(), expectedPageSequences1, actualPageSequences1);
        
        
    }

    // This case tests that multiple commas in page sequence list can be tolerated and page sequences are correctly parsed
    @Test
    public void testPageIDsParserParse4() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f [ , , , 1, ,, 2,,, ,  44,,, 100,, ,, ]  ";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f"};
        String[] actualVolIDs = new String[1];

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
        actualVolIDs[0] = list.get(0).getVolumeID();
        
        Assert.assertArrayEquals(expectedVolIDs, actualVolIDs);
        
        String[] expectedPageSequences1 = new String[]{"00000001", "00000002", "00000044", "00000100"};
        String[] actualPageSequences1 = list.get(0).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences1.toString(), expectedPageSequences1, actualPageSequences1);
        
        
    }

    
    // This case tests that redundant volume identifiers in pageIDs should be coalesced and redundant page sequences should be coalesced by the parser
    @Test
    public void testPageIDsParserParse6() throws ParseException, PolicyViolationException {

        String rawString = "loc.ark:/13960/t9q23z43f [1,2, 44, 100, 2, 44]|  miun.ajj3079.0001.001[4] | loc.ark:/13960/t9q23z43f [22, 12, 2, 17]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        String[] expectedVolIDs = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actualVolIDs = new String[2];

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
        actualVolIDs[0] = list.get(0).getVolumeID();
        actualVolIDs[1] = list.get(1).getVolumeID();
        
        Assert.assertArrayEquals(expectedVolIDs, actualVolIDs);
        
        String[] expectedPageSequences1 = new String[]{"00000001", "00000002", "00000012", "00000017", "00000022", "00000044", "00000100"};
        String[] actualPageSequences1 = list.get(0).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences1.toString(), expectedPageSequences1, actualPageSequences1);
        
        String[] expectedPageSequences2 = new String[]{"00000004"};
        String[] actualPageSequences2 = list.get(1).getPageSequences().toArray(new String[0]);
        
        Assert.assertArrayEquals(actualPageSequences2.toString(), expectedPageSequences2, actualPageSequences2);
        
    }
    

    // This case tests that a ParseException should be raised if a page sequence is not positive
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError1() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f [ 1, -1 ]  ";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }

    // This case tests that a ParseException should be raised if a page sequence is not numeric
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError2() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f [ one, two ]  ";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }


    // This case tests that a ParseException should be raised if page sequence list is malformed (missing closing angle bracket)
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError3() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f [  ";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }


    // This case tests that a ParseException should be raised if page sequence list is malformed (missing opening angle bracket)
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError4() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f 3,4,8,10]  ";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }

    // This case tests that a ParseException should be raised if the page sequence list does not have page sequences
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError5() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f[]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }
    
    // This case tests that a ParseException should be raised if the pageID does not have the volume identifier portion
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError6() throws ParseException, PolicyViolationException  {
        String rawString = "[1,2,3,4]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }

    
    // This case tests that a ParseException should be raised if the page ID does not have the page sequence list portion
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError7() throws ParseException, PolicyViolationException  {
        String rawString = "loc.ark:/13960/t9q23z43f";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }
    

    // This case tests that a ParseException should be raised if volume ID portion is malformed with only a prefix
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError8() throws ParseException, PolicyViolationException  {
        String rawString = "ord.[1,2,3]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }
    

    // This case tests that a ParseException should be raised if volume ID portion is malformed with no prefix
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError9() throws ParseException, PolicyViolationException  {
        String rawString = "ord[1,2,3]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }
    
    // This case tests that a ParseException should be raised if volume ID portion is malformed with only a dot but no prefix
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError10() throws ParseException, PolicyViolationException  {
        String rawString = ".ord[1,2,3]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }


    // This case tests that a ParseException should be raised if volume ID portion is malformed with only a dot and a leading space but no prefix
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError11() throws ParseException, PolicyViolationException  {
        String rawString = " .ord[1,2,3]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }

    // This case tests that a ParseException should be raised if volume ID portion is malformed with only a dot
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError12() throws ParseException, PolicyViolationException  {
        String rawString = ".[1,2,3]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }

    // This case tests that a ParseException should be raised if one of page IDs has a malformed volume ID portion
    @Test (expected = ParseException.class)
    public void testPageIDsParserParseError13() throws ParseException, PolicyViolationException  {
        String rawString = "ord.csa[1,2,3] | rpt.a123[43,11,19] | aadsf[12,4,2] | m.1[1,2,3]";
        ItemCoordinatesParserFactory.Parser parser = ItemCoordinatesParserFactory.getParser(IDTypeEnum.PAGE_ID, new NullPolicyCheckerRegistry());

        List<? extends RequestedItemCoordinates> list = parser.parse(rawString);
        
    }

}


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
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.IDTypeEnum;
import edu.indiana.d2i.htrc.access.id.HTRCItemIdentifierFactory.Parser;

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
    public void testVolumeIDsParserParse1() throws ParseException {
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
    public void testVolumeIDsParserParse2() throws ParseException {
        String rawString = "loc.ark:/13960/t9q23z43f";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID);
        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        String actual = list.get(0).getVolumeID();
        String expected = "loc.ark:/13960/t9q23z43f";
        
        Assert.assertEquals(expected, actual);
    }
    

    @Test
    public void testVolumeIDsParserParse3() throws ParseException {
        String rawString = "loc.ark:/13960/t9q23z43f ||  | miun.ajj3079.0001.001";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID);
        String[] expected = new String[] {"loc.ark:/13960/t9q23z43f", "miun.ajj3079.0001.001"};
        String[] actual = new String[2];

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
        actual[0] = list.get(0).getVolumeID();
        actual[1] = list.get(1).getVolumeID();
            
        Assert.assertArrayEquals(expected, actual);
    }
    
    @Test(expected = ParseException.class)
    public void testVolumeIDsParserParseError() throws ParseException {
        String rawString = " ||  |";
        HTRCItemIdentifierFactory.Parser parser = HTRCItemIdentifierFactory.getParser(IDTypeEnum.VOLUME_ID);

        List<? extends HTRCItemIdentifier> list = parser.parse(rawString);
    }

}


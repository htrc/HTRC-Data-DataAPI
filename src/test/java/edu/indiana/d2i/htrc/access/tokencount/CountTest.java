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
# File:  CountTest.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Yiming Sun
 *
 */
public class CountTest {
    // this case tests an instantiated Count object without increment holds the value of 0
    @Test
    public void testCountValueZero() {
        final int expected = 0;
        
        Count count = new Count();

        final int actual = count.value();
        
        Assert.assertEquals(expected, actual);
    }
    
    // this case tests the default increment is 1
    @Test
    public void testCountIncrementOne() {
        final int expected = 1;
        Count count = new Count();
        count.increment();
        final int actual = count.value();
        
        Assert.assertEquals(expected, actual);
    }

    // this case tests a sequence of increments with specified positive values
    @Test
    public void testCountIncrement1() {
        final int delta = 50;
        final int expected = 100;
        Count count = new Count();
        count.increment(delta);
        count.increment(delta);
        final int actual = count.value();
        
        Assert.assertEquals(expected, actual);
    }
    
    // this case tests a sequence of increments with specified values (both positive and negative) and default increment (by 1)
    @Test
    public void testCountIncrement2() {
        final int delta1 = 49;
        final int delta2 = -40;
        final int expected = 10;
        
        Count count = new Count();
        count.increment(delta1);
        count.increment();
        count.increment(delta2);
        
        final int actual = count.value();
        
        Assert.assertEquals(expected, actual);
    }
    
    // this case tests that two instantiated Count objects should compare to equal
    @Test
    public void testCompare1() {
        final int zero = 0;
        final Count count1 = new Count();
        final Count count2 = new Count();
        
        final int compare = count1.compareTo(count2);
        
        Assert.assertTrue(compare == zero);
    }
    
    // this case tests that compareTo returns a negative value if one is less than the other
    @Test
    public void testCompare2() {
        final int zero = 0;
        final Count count1 = new Count();
        final Count count2 = new Count();
        
        count1.increment();
        count2.increment(20);
        
        final int compare = count1.compareTo(count2);
        
        Assert.assertTrue(compare < zero);
    }

    // this case tests that a NullPointerException should be thrown if a null is being compared to
    @Test(expected = NullPointerException.class)
    public void testCompare3() {
        final Count count1 = new Count();
        count1.compareTo(null);
    }
    
    // this case tests that a compareTo returns a positive number of the second argument is less than the first one
    @Test
    public void testCompare4() {
        final int zero = 0;
        final Count count1 = new Count();
        final Count count2 = new Count();
        
        count2.increment(-12);
        
        final int compare = count1.compareTo(count2);
        
        Assert.assertTrue(compare > zero);
    }
    
    /// this case tests that compareTo with the object itself returns zero
    @Test
    public void testCompare5() {
        final int zero = 0;
        final Count count = new Count();
        count.increment(332);
        
        final int compare = count.compareTo(count);
        
        Assert.assertTrue(compare == zero);
    }
    
    
}


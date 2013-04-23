/*
    * copyright (c) 2013 google inc.
    *
    * licensed under the apache license, version 2.0 (the "license"); you may not
    * use this file except in compliance with the license. you may obtain a copy of
    * the license at
    *
    * http://www.apache.org/licenses/license-2.0
    *
    * unless required by applicable law or agreed to in writing, software
    * distributed under the license is distributed on an "as is" basis, without
    * warranties or conditions of any kind, either express or implied. see the
    * license for the specific language governing permissions and limitations under
    * the license.
*/

package com.google.appengine.tck.example;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Example test case.
 */
@RunWith(Arquillian.class)
public class SimpleExampleTest extends ExampleTestBase {

    private static long timeStamp = 0;

    @Before
    public void setUp() {
        timeStamp = System.currentTimeMillis();
    }

    /**
     * Simple test.
     */
    @Test
    public void testThatItWorks() {
        Assert.assertTrue("This should work.", timeStamp > 0);
    }
}

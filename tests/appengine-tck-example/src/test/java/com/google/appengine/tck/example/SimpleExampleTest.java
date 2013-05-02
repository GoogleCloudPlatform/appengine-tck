/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

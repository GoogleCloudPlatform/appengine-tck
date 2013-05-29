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

package com.google.appengine.sometests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * An example test illustrating the transformation into a TCK class that can run against the appspot
 * or sdk profiles.  Notice how it doesn't declare the @RunsWith annotation, and also does not
 * implement getDeployment().  It does however need to declare the setUp() and tearDown() methods
 * for the transformer to recognize it as a junit test.
 *
 * See the appengine-tck-transformers module with the corresponding ExampleJUnitTransformer class.
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */

public class ExistingTest {

    public void setUp() {
        // setUp() required by org.jboss.maven.plugins.transformer.TransformerUtils
    }

    public void tearDown() {
        // tearDown() required by org.jboss.maven.plugins.transformer.TransformerUtils
    }

    @Test
    public void testSomething() {
        assertTrue((1 + 2) == 3);
    }

    @Test
    public void testSomethingThatFails() {
        assertTrue((1 + 2) == 4);
    }
}

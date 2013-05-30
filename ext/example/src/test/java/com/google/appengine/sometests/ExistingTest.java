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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.Test;

/**
 * An example test illustrating the transformation of plain GAE JUnit test into a TCK class that can run against the existing TCK profiles.
 * Notice how it doesn't declare the @RunsWith annotation, and also does not implement getDeployment().
 * <p/>
 * See the appengine-tck-transformers module with the corresponding ExampleJUnitTransformer class.
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ExistingTest {
    private LocalServiceTestHelper helper;
    private DatastoreService service;

    public void setUp() {
        helper = new LocalServiceTestHelper().setEnvAppId("exampleAppId");
        helper.setUp();

        service = DatastoreServiceFactory.getDatastoreService();
    }

    public void tearDown() {
        if (helper != null) {
            helper.tearDown();
        }
    }

    @Test(expected = EntityNotFoundException.class)
    public void testSomething() throws Exception {
        service.get(KeyFactory.createKey("does-not-exist", 1L));
    }

}

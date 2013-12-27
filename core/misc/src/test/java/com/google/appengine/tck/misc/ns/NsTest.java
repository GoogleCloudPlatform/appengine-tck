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

package com.google.appengine.tck.misc.ns;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.ns.WithinNamespace;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class NsTest extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        return getTckDeployment();
    }

    @Test
    @WithinNamespace({ "", "Ns1" })
    public void testSmoke() throws Exception {
        final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Key key = ds.put(new Entity("NsTest"));
        try {
            Assert.assertNotNull(ds.get(key));
        } finally {
            ds.delete(key);
        }
    }
}

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

package com.google.appengine.tck.modules;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.protocol.modules.OperateOnModule;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class SmokeTest extends ModulesTestBase {
    protected static final String MODULES_KIND = "Modules";

    private DatastoreService ds;

    @Deployment
    public static EnterpriseArchive getDeployment() {
        WebArchive module1 = getTckSubDeployment(1);
        module1.addClass(SmokeTest.class);

        WebArchive module2 = getTckSubDeployment(2);
        module2.addClass(SmokeTest.class);

        return getEarDeployment(module1, module2);
    }

    @Before
    public void setUp() throws Exception {
        ds = DatastoreServiceFactory.getDatastoreService();
    }

    @Test
    @InSequence(1)
    public void testPut() throws Exception {
        Key key = ds.put(new Entity(MODULES_KIND, 1));
        Assert.assertNotNull(key);
    }

    @Test
    @InSequence(2)
    @OperateOnModule("m2")
    public void testGet() throws Exception {
        Key key = KeyFactory.createKey(MODULES_KIND, 1);
        Entity e = ds.get(key);
        Assert.assertNotNull(e);
    }

    @Test
    @InSequence(3)
    public void testDelete() throws Exception {
        Key key = KeyFactory.createKey(MODULES_KIND, 1);
        ds.delete(key);
    }

    @Test
    @InSequence(4)
    @OperateOnModule("m2")
    public void testQuery() throws Exception {
        Query query = new Query(MODULES_KIND).setKeysOnly();
        for (Entity e : ds.prepare(query).asIterable()) {
            Assert.fail("Should not be here: " + e);
        }
    }
}

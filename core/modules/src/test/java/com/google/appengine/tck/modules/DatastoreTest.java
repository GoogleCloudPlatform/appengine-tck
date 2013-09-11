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
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class DatastoreTest extends CrudTestBase {
    protected static final String MODULES_KIND = "Modules";

    private DatastoreService ds;

    @Deployment
    public static EnterpriseArchive getDeployment() {
        return getCrudDeployment(DatastoreTest.class);
    }

    private static Key getKey() {
        return KeyFactory.createKey(MODULES_KIND, 1);
    }

    @Before
    public void setUp() throws Exception {
        ds = DatastoreServiceFactory.getDatastoreService();
    }

    @Override
    protected void doTestCreate() {
        Entity entity = new Entity(MODULES_KIND, 1);
        entity.setProperty("x", "y");

        Key key = ds.put(entity);
        Assert.assertNotNull(key);
    }

    @Override
    protected void doTestRead() throws Exception {
        Key key = getKey();
        Entity e = ds.get(key);
        Assert.assertNotNull(e);
    }

    @Override
    protected void doTestUpdate() throws Exception {
        Key key = getKey();
        Entity e = ds.get(key);
        Assert.assertNotNull(e);
        e.setProperty("x", "z");
        ds.put(e);
    }

    @Override
    protected void doTestReRead() throws Exception {
        Key key = getKey();
        Entity e = ds.get(key);
        Assert.assertNotNull(e);
        Assert.assertEquals("z", e.getProperty("x"));
    }

    @Override
    protected void doTestDelete() {
        Key key = getKey();
        ds.delete(key);
    }

    @Override
    protected void doTestCheck() {
        Query query = new Query(MODULES_KIND).setKeysOnly();
        for (Entity e : ds.prepare(query).asIterable()) {
            Assert.fail("Should not be here: " + e);
        }
    }
}

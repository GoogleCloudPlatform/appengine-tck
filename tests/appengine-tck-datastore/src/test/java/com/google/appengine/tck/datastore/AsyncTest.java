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

package com.google.appengine.tck.datastore;

import java.util.concurrent.Future;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
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

public class AsyncTest extends AsyncTestBase {
    @Deployment
    public static WebArchive getDeployment() {
        return getAsynchDeployment();
    }

    @Test
    public void testCRUD() throws Exception {
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();

        Entity e1 = new Entity("ASYNC");
        e1.setProperty("foo", "bar");

        Future<Key> k1 = service.put(e1);
        Assert.assertNotNull(k1);

        Key key = k1.get();
        Assert.assertNotNull(key);
        Future<Entity> fe1 = service.get(key);
        Assert.assertNotNull(fe1);
        Assert.assertEquals(e1, fe1.get());

        Future<Void> fd1 = service.delete(key);
        Assert.assertNotNull(fd1);
        fd1.get();

        assertStoreDoesNotContain(key);
    }

    @Test
    public void testCRUDWithTx() throws Exception {
        final Entity e1 = new Entity("ASYNC");
        e1.setProperty("foo", "bar");

        Action<Future<Key>> put = new Action<Future<Key>>() {
            public Future<Key> run(AsyncDatastoreService ads) {
                Future<Key> k1 = ads.put(e1);
                Assert.assertNotNull(k1);
                return k1;
            }
        };
        final Future<Key> k1 = inTx(put);

        Action<Void> get = new Action<Void>() {
            public Void run(AsyncDatastoreService ads) throws Exception {
                Key key = k1.get();
                Assert.assertNotNull(key);
                Future<Entity> fe1 = ads.get(key);
                Assert.assertNotNull(fe1);
                Assert.assertEquals(e1, fe1.get());
                return null;
            }
        };
        inTx(get);

        Action<Void> delete = new Action<Void>() {
            public Void run(AsyncDatastoreService ads) throws Exception {
                Future<Void> fd1 = ads.delete(k1.get());
                Assert.assertNotNull(fd1);
                fd1.get();
                return null;
            }
        };
        inTx(delete);

        assertStoreDoesNotContain(k1.get());
    }
}

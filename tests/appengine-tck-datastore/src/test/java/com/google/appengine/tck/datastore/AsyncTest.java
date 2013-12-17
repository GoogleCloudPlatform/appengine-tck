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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
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

    @Test
    public void testBeginTx() throws Exception {
        final AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();

        Transaction tx = waitOnFuture(service.beginTransaction(TransactionOptions.Builder.withXG(true)));
        Key key, key2;
        try {
            key = waitOnFuture(service.put(tx, new Entity("AsyncTx")));
            key2 = waitOnFuture(service.put(tx, new Entity("AsyncTx")));
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }

        if (key != null && key2 != null) {
            tx = waitOnFuture(service.beginTransaction(TransactionOptions.Builder.withXG(true)));
            try {
                try {
                    try {
                        Assert.assertNotNull(waitOnFuture(service.get(tx, key)));
                        Assert.assertNotNull(waitOnFuture(service.get(tx, Collections.singleton(key2))));
                    } finally {
                        service.delete(tx, key2);
                    }
                } finally {
                    service.delete(tx, Collections.singleton(key));
                }
            } finally {
                tx.rollback();
            }
        }
    }

    @Test
    public void testCommitTx() throws Exception {
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();
        Future<Transaction> fTX = service.beginTransaction(TransactionOptions.Builder.withDefaults());
        Transaction tx = waitOnFuture(fTX);
        Key key;
        try {
            Future<Key> fKey = service.put(tx, new Entity("AsyncTx"));
            key = waitOnFuture(fKey);
            waitOnFuture(tx.commitAsync());
        } catch (Exception e) {
            waitOnFuture(tx.rollbackAsync());
            throw e;
        }

        if (key != null) {
            Assert.assertNotNull(getSingleEntity(service, key));
        }
    }

    @Test
    public void testRollbackTx() throws Exception {
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();
        Future<Transaction> fTX = service.beginTransaction(TransactionOptions.Builder.withDefaults());
        Transaction tx = waitOnFuture(fTX);
        Key key = null;
        try {
            Future<Key> fKey = service.put(tx, new Entity("AsyncTx"));
            key = waitOnFuture(fKey);
        } finally {
            waitOnFuture(tx.rollbackAsync());
        }

        if (key != null) {
            Assert.assertNull(getSingleEntity(service, key));
        }
    }

    @Test
    public void testTxIsActive() throws Exception {
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();
        Transaction tx = waitOnFuture(service.beginTransaction());
        try {
            Assert.assertTrue(tx.isActive());
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testMiscOps() throws Exception {
        AsyncDatastoreService service = DatastoreServiceFactory.getAsyncDatastoreService();

        DatastoreAttributes attributes = waitOnFuture(service.getDatastoreAttributes());
        Assert.assertNotNull(attributes);
        Assert.assertNotNull(attributes.getDatastoreType());

        Map<Index, Index.IndexState> indexes = waitOnFuture(service.getIndexes());
        Assert.assertNotNull(indexes);

        Transaction tx = waitOnFuture(service.beginTransaction());
        try {
            String txId = tx.getId();
            Assert.assertNotNull(txId);
            Assert.assertEquals(txId, tx.getId());

            String appId = tx.getApp();
            Assert.assertNotNull(appId);
            Assert.assertEquals(appId, tx.getApp());
        } finally {
            tx.rollback();
        }
    }
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.google.appengine.tck.datastore;

import java.util.concurrent.Future;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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

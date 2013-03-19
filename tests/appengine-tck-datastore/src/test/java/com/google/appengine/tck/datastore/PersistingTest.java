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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class PersistingTest extends SimpleTestBase {

    @Test
    public void putStoresEntity() throws Exception {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity client = new Entity("Client");
        client.setProperty("username", "alesj");
        client.setProperty("password", "password");
        final Key key = ds.put(client);
        try {
            Query query = new Query("Client");
            query.setFilter(new Query.FilterPredicate("username", Query.FilterOperator.EQUAL, "alesj"));
            PreparedQuery pq = ds.prepare(query);
            Entity result = pq.asSingleEntity();
            Assert.assertNotNull(result);
            Assert.assertEquals(key, result.getKey());
            Assert.assertEquals("alesj", result.getProperty("username"));
            Assert.assertEquals("password", result.getProperty("password"));
        } finally {
            ds.delete(key);
        }
    }

}

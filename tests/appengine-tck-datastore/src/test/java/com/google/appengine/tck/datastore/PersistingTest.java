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

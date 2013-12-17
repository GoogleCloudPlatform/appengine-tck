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

import java.util.List;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.QueryResultList;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Datastore querying results tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class QueryResultTest extends QueryTestBase {
    @Test
    public void testCursor() throws Exception {
        Entity parent = createTestEntityWithUniqueMethodNameKey("Person", "testKeysOnly");
        Key key = parent.getKey();

        Entity john = createEntity("Person", key)
            .withProperty("name", "John")
            .withProperty("surname", "Doe")
            .store();

        Query query = new Query("Person")
            .setAncestor(key)
            .setKeysOnly();

        PreparedQuery preparedQuery = service.prepare(query);
        QueryResultIterator<Entity> iter = preparedQuery.asQueryResultIterator();
        Assert.assertNotNull(iter.next());
        Cursor cursor = iter.getCursor();

        iter = service.prepare(query).asQueryResultIterator(FetchOptions.Builder.withStartCursor(cursor));
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void testIndexListFromIterator() throws Exception {
        Entity parent = createTestEntityWithUniqueMethodNameKey("Person", "testKeysOnly");
        Key key = parent.getKey();

        Entity tom = createEntity("Person", key)
            .withProperty("name", "Tom")
            .withProperty("surname", "Foe")
            .store();

        Query query = new Query("Person")
            .setAncestor(key)
            .setKeysOnly();

        PreparedQuery preparedQuery = service.prepare(query);
        QueryResultIterator<Entity> iter = preparedQuery.asQueryResultIterator();
        List<Index> indexes = iter.getIndexList();
        if (indexes != null) {
            // TODO -- something useful
            System.out.println("indexes = " + indexes);
        }
    }

    @Test
    public void testIndexListFromList() throws Exception {
        Entity parent = createTestEntityWithUniqueMethodNameKey("Person", "testKeysOnly");
        Key key = parent.getKey();

        Entity joe = createEntity("Person", key)
            .withProperty("name", "Joe")
            .withProperty("surname", "Moe")
            .store();

        Query query = new Query("Person")
            .setAncestor(key)
            .setKeysOnly();

        PreparedQuery preparedQuery = service.prepare(query);
        QueryResultList<Entity> list = preparedQuery.asQueryResultList(FetchOptions.Builder.withDefaults());
        List<Index> indexes = list.getIndexList();
        if (indexes != null) {
            // TODO -- something useful
            System.out.println("indexes = " + indexes);
        }
    }
}

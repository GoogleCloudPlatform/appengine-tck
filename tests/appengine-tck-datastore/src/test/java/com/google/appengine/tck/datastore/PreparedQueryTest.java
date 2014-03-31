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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.QueryResultList;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests all the PreparedQuery methods.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)

public class PreparedQueryTest extends QueryTestBase {
    private Entity john;
    private Entity johnsParent;
    private PreparedQuery preparedQuery;

    @Before
    public void setUp() {
        super.setUp();

        johnsParent = createTestEntityWithUniqueMethodNameKey("Person", "PreparedQueryTest");
        Key key = johnsParent.getKey();
        john = createEntity("Person", key)
            .withProperty("name", "John")
            .store();

        Query query = new Query("Person")
            .setAncestor(key)
            .setFilter(new Query.FilterPredicate("name", EQUAL, "John"));

        preparedQuery = service.prepare(query);
    }

    @Test
    public void testCountEntities() throws Exception {
        assertEquals("number of results", 1, preparedQuery.countEntities(withDefaults()));
    }

    @Test
    public void testCountEntitiesWithOptions() throws Exception {
        assertEquals("number of results", 1, preparedQuery.countEntities(withDefaults()));
    }

    @Test
    public void testAsIterator() throws Exception {
        Iterator<Entity> iterator = preparedQuery.asIterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertEquals(john, iterator.next());
    }

    @Test
    public void testAsIteratorWithOptions() throws Exception {
        Iterator<Entity> iterator = preparedQuery.asIterator(withDefaults());
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertEquals(john, iterator.next());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorThrowsNoSuchElementException() throws Exception {
        Iterator<Entity> iterator = preparedQuery.asIterator();
        iterator.next();
        assertFalse(iterator.hasNext());
        iterator.next();
    }

    @Test
    public void testAsIterable() throws Exception {
        Iterable<Entity> iterable = preparedQuery.asIterable();
        assertNotNull(iterable);
        assertNotNull(iterable.iterator());
        assertTrue(iterable.iterator().hasNext());
        assertEquals(john, iterable.iterator().next());
    }

    @Test
    public void testAsIterableWithOptions() throws Exception {
        Iterable<Entity> iterable = preparedQuery.asIterable(withDefaults());
        assertNotNull(iterable);
        assertNotNull(iterable.iterator());
        assertTrue(iterable.iterator().hasNext());
        assertEquals(john, iterable.iterator().next());
    }

    @Test
    public void testAsSingleEntity() throws Exception {
        Entity entity = preparedQuery.asSingleEntity();
        assertEquals(john, entity);
    }

    @Test
    public void testAsList() throws Exception {
        List<Entity> list = preparedQuery.asList(withDefaults());
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(john, list.get(0));
    }

    @Test
    public void testAsQueryResultIterable() throws Exception {
        QueryResultIterable<Entity> iterable = preparedQuery.asQueryResultIterable();
        assertNotNull(iterable);
        assertNotNull(iterable.iterator());
        assertTrue(iterable.iterator().hasNext());
        assertEquals(john, iterable.iterator().next());
    }

    @Test
    public void testAsQueryResultIterableWithOptions() throws Exception {
        QueryResultIterable<Entity> iterable = preparedQuery.asQueryResultIterable(withDefaults());
        assertNotNull(iterable);
        assertNotNull(iterable.iterator());
        assertTrue(iterable.iterator().hasNext());
        assertEquals(john, iterable.iterator().next());
    }

    @Test
    public void testAsQueryResultIterator() throws Exception {
        QueryResultIterator<Entity> iterator = preparedQuery.asQueryResultIterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertEquals(john, iterator.next());
    }

    @Test
    public void testAsQueryResultIteratorWithOptions() throws Exception {
        QueryResultIterator<Entity> iterator = preparedQuery.asQueryResultIterator(withDefaults());
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertEquals(john, iterator.next());
    }

    @Test
    public void testAsQueryResultList() throws Exception {
        QueryResultList<Entity> list = preparedQuery.asQueryResultList(withDefaults());
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(john, list.get(0));
    }
}

/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

    @Test
    public void testIteratorThrowsNoSuchElementException() throws Exception {
        Iterator<Entity> iterator = preparedQuery.asIterator();
        iterator.next();
        assertFalse(iterator.hasNext());

        thrown.expect(NoSuchElementException.class);
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

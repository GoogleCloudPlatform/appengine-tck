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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.IN;
import static com.google.appengine.api.datastore.Query.FilterOperator.NOT_EQUAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Datastore querying basic tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)

public class QueryBasicsTest extends QueryTestBase {

    @Test
    public void textQueryWithoutAnyConstraints() throws Exception {
        Entity person = new Entity(KeyFactory.createKey("Person", 1));
        service.put(person);

        Entity address = new Entity(KeyFactory.createKey("Address", 1));
        service.put(address);

        PreparedQuery preparedQuery = service.prepare(new Query());
        assertTrue(preparedQuery.countEntities(withDefaults()) >= 2);

        List<Entity> results = preparedQuery.asList(withDefaults());
        assertTrue(results.containsAll(Arrays.asList(person, address)));
    }

    @Test
    public void queryingByKindOnlyReturnsEntitiesOfRequestedKind() throws Exception {
        Entity person = new Entity(KeyFactory.createKey("Person", 1));
        service.put(person);

        Entity address = new Entity(KeyFactory.createKey("Address", 1));
        service.put(address);

        assertSingleResult(person, new Query("Person"));
    }

    @Test
    public void singleEntityThrowsTooManyResultsExceptionWhenMoreThanOneResult() throws Exception {
        createEntity("Person", 1).store();
        createEntity("Person", 2).store();

        PreparedQuery preparedQuery = service.prepare(new Query("Person"));
        try {
            preparedQuery.asSingleEntity();
            fail("Expected PreparedQuery.TooManyResultsException");
        } catch (PreparedQuery.TooManyResultsException e) {
            // pass
        }
    }

    @Test
    public void testMultipleFilters() throws Exception {
        Entity johnDoe = createEntity("Person", 1)
                .withProperty("name", "John")
                .withProperty("lastName", "Doe")
                .store();

        Entity johnBooks = createEntity("Person", 2)
                .withProperty("name", "John")
                .withProperty("lastName", "Books")
                .store();

        Entity janeDoe = createEntity("Person", 3)
                .withProperty("name", "Jane")
                .withProperty("lastName", "Doe")
                .store();

        Query query = new Query("Person")
                .setFilter(Query.CompositeFilterOperator.and(
                        new Query.FilterPredicate("name", EQUAL, "John"),
                        new Query.FilterPredicate("lastName", EQUAL, "Doe")));

        assertSingleResult(johnDoe, query);
    }

    @Test
    public void testNullPropertyValue() throws Exception {
        createEntity("Entry", 1)
                .withProperty("user", null)
                .store();

        Entity entity = service.prepare(new Query("Entry")).asSingleEntity();
        assertNull(entity.getProperty("user"));
    }

    @Test
    public void testFilteringWithNotEqualReturnsOnlyEntitiesContainingTheProperty() throws Exception {
        Entity e1 = createEntity("Entry", 1)
                .withProperty("foo", "aaa")
                .store();

        createEntity("Entry", 2)
                .withProperty("bar", "aaa")
                .store();

        Query query = new Query("Entry")
                .setFilter(new Query.FilterPredicate("foo", NOT_EQUAL, "bbb"));
        assertEquals(Collections.singletonList(e1), service.prepare(query).asList(withDefaults()));
    }

    @Test
    public void testFilterEqualNull() throws Exception {
        createEntity("Entry", 1)
                .withProperty("user", null)
                .store();

        Query query = new Query("Entry").setFilter(new Query.FilterPredicate("user", EQUAL, null));
        assertNotNull(service.prepare(query).asSingleEntity());
    }

    @Test
    public void testFilterNotEqualNull() throws Exception {
        createEntity("Entry", 1)
                .withProperty("user", "joe")
                .store();

        Query query = new Query("Entry").setFilter(new Query.FilterPredicate("user", NOT_EQUAL, null));
        assertNotNull(service.prepare(query).asSingleEntity());
    }

    @Test
    public void testFilterInNull() throws Exception {
        createEntity("Entry", 1)
                .withProperty("user", null)
                .store();

        Query query = new Query("Entry").setFilter(new Query.FilterPredicate("user", IN, Arrays.asList(null, "foo")));
        assertNotNull(service.prepare(query).asSingleEntity());
    }

    @Test
    public void testFilterOnMultiValuedProperty() throws Exception {
        createEntity("Entry", 1)
                .withProperty("letters", Arrays.asList("a", "b", "c"))
                .store();

        Query query = new Query("Entry").setFilter(new Query.FilterPredicate("letters", EQUAL, "a"));
        assertNotNull(service.prepare(query).asSingleEntity());
    }

    @Test
    public void testFilteringByKind() throws Exception {
        Entity foo = createEntity("foo", 1).store();
        Entity bar = createEntity("bar", 2).store();

        PreparedQuery preparedQuery = service.prepare(new Query("foo"));
        List<Entity> results = preparedQuery.asList(withDefaults());
        assertEquals(1, results.size());
        assertEquals(foo, results.get(0));
    }

    @Test
    public void testFilteringByAncestor() throws Exception {
        Key rootKey = KeyFactory.createKey("foo", "root");
        Entity root = createEntity(rootKey).store();

        Key barKey = KeyFactory.createKey(rootKey, "bar", 10);
        Entity bar = createEntity(barKey).store();

        Key fooKey = KeyFactory.createKey(barKey, "foo", 20);
        Entity foo = createEntity(fooKey).store();

        List<Entity> list = service.prepare(new Query("foo", rootKey)).asList(withDefaults());
        assertEquals(asSet(Arrays.asList(root, foo)), asSet(list));

        list = service.prepare(new Query(rootKey)).asList(withDefaults());
        assertEquals(asSet(Arrays.asList(root, foo, bar)), asSet(list));

        list = service.prepare(new Query("foo", barKey)).asList(withDefaults());
        assertEquals(asSet(Arrays.asList(foo)), asSet(list));
    }

    @Test
    public void testQueryWithInequalityFiltersOnMultiplePropertiesThrowsIllegalArgumentException() throws Exception {
        Query query = createQuery()
                .setFilter(Query.CompositeFilterOperator.and(
                        new Query.FilterPredicate("weight", GREATER_THAN, 3),
                        new Query.FilterPredicate("size", GREATER_THAN, 5)));

        assertIAEWhenAccessingResult(service.prepare(query));
    }

    @Test
    public void testQueryWithInequalityFilterAndFirstSortOnDifferentPropertyThrowsIllegalArgumentException() throws Exception {
        Query query = createQuery()
                .setFilter(new Query.FilterPredicate("foo", GREATER_THAN, 3))
                .addSort("bar");

        assertIAEWhenAccessingResult(service.prepare(query));
    }

    @Test
    public void testQueryWithInequalityFilterAndFirstSortOnSamePropertyIsAllowed() throws Exception {
        Query query = createQuery()
                .setFilter(new Query.FilterPredicate("foo", GREATER_THAN, 3))
                .addSort("foo")
                .addSort("bar");

        service.prepare(query).asList(withDefaults());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedFiltersAreSupported() throws Exception {
        Entity johnDoe = createEntity("Person", 1)
                .withProperty("name", "John")
                .withProperty("lastName", "Doe")
                .store();

        Entity johnBooks = createEntity("Person", 2)
                .withProperty("name", "John")
                .withProperty("lastName", "Books")
                .store();

        Entity janeDoe = createEntity("Person", 3)
                .withProperty("name", "Jane")
                .withProperty("lastName", "Doe")
                .store();

        Query query = new Query("Person")
                .addFilter("name", EQUAL, "John")
                .addFilter("lastName", EQUAL, "Doe");

        assertSingleResult(johnDoe, query);
    }

    private HashSet<Entity> asSet(List<Entity> collection) {
        return new HashSet<Entity>(collection);
    }

}

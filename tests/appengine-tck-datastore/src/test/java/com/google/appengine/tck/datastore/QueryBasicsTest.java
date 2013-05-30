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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
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

    private static final String QUERY_BASICS_ENTITY = "QueryBasicsTestEntity";

    private Key createQueryBasicsTestParent(String methodName) {
        Entity parent = createTestEntityWithUniqueMethodNameKey(QUERY_BASICS_ENTITY, methodName);
        return parent.getKey();
    }

    @Test
    public void testQueryWithoutAnyConstraints() throws Exception {
        Key parentKey = createQueryBasicsTestParent("testQueryWithoutAnyConstraints");

        Entity person = new Entity("Person", parentKey);
        service.put(person);

        Entity address = new Entity("Address", parentKey);
        service.put(address);

        PreparedQuery preparedQuery = service.prepare(new Query().setAncestor(parentKey));
        assertTrue(preparedQuery.countEntities(withDefaults()) >= 2);

        List<Entity> results = preparedQuery.asList(withDefaults());
        assertTrue(results.containsAll(Arrays.asList(person, address)));
    }

    @Test
    public void queryingByKindOnlyReturnsEntitiesOfRequestedKind() throws Exception {
        Key parentKey = createQueryBasicsTestParent("queryingByKindOnlyReturnsEntitiesOfRequestedKind");
        Entity person = new Entity(KeyFactory.createKey(parentKey, "Person", 1));
        service.put(person);

        Entity address = new Entity(KeyFactory.createKey(parentKey, "Address", 1));
        service.put(address);

        assertSingleResult(person, new Query("Person").setAncestor(parentKey));
    }

    @Test
    public void singleEntityThrowsTooManyResultsExceptionWhenMoreThanOneResult() throws Exception {
        String methodName = "singleEntityThrowsTooManyResultsExceptionWhenMoreThanOneResult";
        Key parentKey = createQueryBasicsTestParent(methodName);

        createEntity("Person", parentKey).store();
        createEntity("Person", parentKey).store();

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
        Key parentKey = createQueryBasicsTestParent("testMultipleFilters");
        Entity johnDoe = createEntity("Person", parentKey)
            .withProperty("name", "John")
            .withProperty("lastName", "Doe")
            .store();

        Entity johnBooks = createEntity("Person", parentKey)
            .withProperty("name", "John")
            .withProperty("lastName", "Books")
            .store();

        Entity janeDoe = createEntity("Person", parentKey)
            .withProperty("name", "Jane")
            .withProperty("lastName", "Doe")
            .store();

        Query query = new Query("Person")
            .setAncestor(parentKey)
            .setFilter(and(
                new Query.FilterPredicate("name", EQUAL, "John"),
                new Query.FilterPredicate("lastName", EQUAL, "Doe")));

        assertSingleResult(johnDoe, query);
    }

    @Test
    public void testNullPropertyValue() throws Exception {
        Key parentKey = createQueryBasicsTestParent("testNullPropertyValue");

        createEntity("Entry", parentKey)
            .withProperty("user", null)
            .store();

        Entity entity = service.prepare(new Query("Entry")
            .setAncestor(parentKey)).asSingleEntity();
        assertNull(entity.getProperty("user"));
    }

    @Test
    public void testFilteringWithNotEqualReturnsOnlyEntitiesContainingTheProperty() throws Exception {
        String methodName = "testFilteringWithNotEqualReturnsOnlyEntitiesContainingTheProperty";
        Key parentKey = createQueryBasicsTestParent(methodName);
        Entity e1 = createEntity("Entry", parentKey)
            .withProperty("foo", "aaa")
            .store();

        createEntity("Entry", parentKey)
            .withProperty("bar", "aaa")
            .store();

        Query query = new Query("Entry")
            .setAncestor(parentKey)
            .setFilter(new Query.FilterPredicate("foo", NOT_EQUAL, "bbb"));
        assertEquals(Collections.singletonList(e1), service.prepare(query).asList(withDefaults()));
    }

    @Test
    public void testFilterEqualNull() throws Exception {
        Key parentKey = createQueryBasicsTestParent("testFilterEqualNull");
        createEntity("Entry", parentKey)
            .withProperty("user", null)
            .store();

        Query query = new Query("Entry")
            .setAncestor(parentKey)
            .setFilter(new Query.FilterPredicate("user", EQUAL, null));
        assertNotNull(service.prepare(query).asSingleEntity());
    }

    @Test
    public void testFilterNotEqualNull() throws Exception {
        Key parentKey = createQueryBasicsTestParent("testFilterNotEqualNull");
        createEntity("Entry", parentKey)
            .withProperty("user", "joe")
            .store();

        Query query = new Query("Entry")
            .setAncestor(parentKey)
            .setFilter(new Query.FilterPredicate("user", NOT_EQUAL, null));
        assertNotNull(service.prepare(query).asSingleEntity());
    }

    @Test
    public void testFilterInNull() throws Exception {
        Key parentKey = createQueryBasicsTestParent("testFilterInNull");
        createEntity("Entry", parentKey)
            .withProperty("user", null)
            .store();

        Query query = new Query("Entry")
            .setAncestor(parentKey)
            .setFilter(new Query.FilterPredicate("user", IN, Arrays.asList(null, "foo")));
        assertNotNull(service.prepare(query).asSingleEntity());
    }

    @Test
    public void testFilterOnMultiValuedProperty() throws Exception {
        Key parentKey = createQueryBasicsTestParent("testFilterOnMultiValuedProperty");
        createEntity("Entry", parentKey)
            .withProperty("letters", Arrays.asList("a", "b", "c"))
            .store();

        Query query = new Query("Entry")
            .setAncestor(parentKey)
            .setFilter(new Query.FilterPredicate("letters", EQUAL, "a"));
        assertNotNull(service.prepare(query).asSingleEntity());
    }

    @Test
    public void testFilteringByKind() throws Exception {
        Key parentKey = createQueryBasicsTestParent("testFilteringByKind");
        Entity foo = createEntity("foo", parentKey).store();
        Entity bar = createEntity("bar", parentKey).store();

        PreparedQuery preparedQuery = service.prepare(new Query("foo").setAncestor(parentKey));
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
            .setFilter(and(
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

    @Ignore("According to the docs, ordering of query results is undefined when no sort order is specified. They are " +
        "currently ordered according to the index, but this may change in the future and so it shouldn't be tested by the TCK.")
    @Test
    public void testDefaultSortOrderIsDefinedByIndexDefinition() throws Exception {
        Key parentKey = createQueryBasicsTestParent("testDefaultSortOrderIsDefinedByIndexDefinition");
        Entity aaa = createEntity("Product", parentKey)
            .withProperty("name", "aaa")
            .withProperty("price", 10)
            .store();
        Entity ccc = createEntity("Product", parentKey)
            .withProperty("name", "ccc")
            .withProperty("price", 10)
            .store();
        Entity bbb = createEntity("Product", parentKey)
            .withProperty("name", "bbb")
            .withProperty("price", 10)
            .store();

        Query query = new Query("Product")
            .setAncestor(parentKey)
            .setFilter(and(
                new Query.FilterPredicate("name", GREATER_THAN_OR_EQUAL, "aaa"),
                new Query.FilterPredicate("price", EQUAL, 10)
            ));
        assertEquals(Arrays.asList(aaa, bbb, ccc), service.prepare(query).asList(withDefaults()));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedFiltersAreSupported() throws Exception {
        Key parentKey = createQueryBasicsTestParent("testDeprecatedFiltersAreSupported");
        Entity johnDoe = createEntity("Person", parentKey)
            .withProperty("name", "John")
            .withProperty("lastName", "Doe")
            .store();

        Entity johnBooks = createEntity("Person", parentKey)
            .withProperty("name", "John")
            .withProperty("lastName", "Books")
            .store();

        Entity janeDoe = createEntity("Person", parentKey)
            .withProperty("name", "Jane")
            .withProperty("lastName", "Doe")
            .store();

        Query query = new Query("Person")
            .setAncestor(parentKey)
            .addFilter("name", EQUAL, "John")
            .addFilter("lastName", EQUAL, "Doe");

        assertSingleResult(johnDoe, query);
    }

    private HashSet<Entity> asSet(List<Entity> collection) {
        return new HashSet<Entity>(collection);
    }

}

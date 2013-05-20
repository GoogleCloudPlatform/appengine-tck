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
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.RawValue;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.IN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Datastore querying optimizations tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)

public class QueryOptimizationsTest extends QueryTestBase {

    @Test
    public void testKeysOnly() throws Exception {
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

        Entity entity = preparedQuery.asSingleEntity();
        assertEquals(john.getKey(), entity.getKey());
        assertNull(entity.getProperty("name"));
        assertNull(entity.getProperty("surname"));
    }

    @Test
    public void testProjections() throws Exception {
        Entity parent = createTestEntityWithUniqueMethodNameKey("Product", "testProjections");
        Key key = parent.getKey();

        Entity e = createEntity("Product", key)
            .withProperty("price", 123L)
            .withProperty("percent", 0.123)
            .withProperty("x", -0.321)
            .withProperty("diff", -5L)
            .withProperty("weight", 10L)
            .store();

        Query query = new Query("Product")
            .setAncestor(key)
            .addProjection(new PropertyProjection("price", Long.class))
            .addProjection(new PropertyProjection("percent", Double.class))
            .addProjection(new PropertyProjection("x", Double.class))
            .addProjection(new PropertyProjection("diff", Long.class));

        PreparedQuery preparedQuery = service.prepare(query);
        Entity result = preparedQuery.asSingleEntity();
        assertEquals(e.getKey(), result.getKey());
        assertEquals(e.getProperty("price"), result.getProperty("price"));
        assertEquals(e.getProperty("percent"), result.getProperty("percent"));
        assertEquals(e.getProperty("x"), result.getProperty("x"));
        assertEquals(e.getProperty("diff"), result.getProperty("diff"));
        assertNull(result.getProperty("weight"));
    }

    @Test
    public void testProjectionQueryOnlyReturnsEntitiesContainingProjectedProperty() throws Exception {
        String methodName = "testProjectionQueryOnlyReturnsEntitiesContainingProjectedProperty";
        Entity parent = createTestEntityWithUniqueMethodNameKey("Kind", methodName);
        Key key = parent.getKey();

        Entity e1 = createEntity("Kind", key)
            .withProperty("foo", "foo")
            .store();

        Entity e2 = createEntity("Kind", key)
            .withProperty("bar", "bar")
            .store();

        Query query = new Query("Kind")
            .setAncestor(key)
            .addProjection(new PropertyProjection("foo", String.class));

        List<Entity> results = service.prepare(query).asList(withDefaults());
        assertEquals(Collections.singletonList(e1), results);
    }

    @Test
    public void testProjectionQueryReturnsEntitiesContainingProjectedPropertyEvenIfPropertyValueIsNull() throws Exception {
        String methodName = "testProjectionQueryOnlyReturnsEntitiesContainingProjectedPropertyEvenIfPropertyValueIsNull";
        Entity parent = createTestEntityWithUniqueMethodNameKey("Kind", methodName);
        Key key = parent.getKey();

        Entity e1 = createEntity("Kind", key)
            .withProperty("foo", null)
            .store();

        Query query = new Query("Kind")
            .setAncestor(key)
            .addProjection(new PropertyProjection("foo", String.class));

        List<Entity> results = service.prepare(query).asList(withDefaults());
        assertEquals(Collections.singletonList(e1), results);
    }

    @Test
    public void testProjectionQueryOnlyReturnsEntitiesContainingAllProjectedProperties() throws Exception {
        String methodName = "testProjectionQueryOnlyReturnsEntitiesContainingAllProjectedProperties";
        Entity parent = createTestEntityWithUniqueMethodNameKey("Kind", methodName);
        Key key = parent.getKey();

        Entity e1 = createEntity("Kind", key)
            .withProperty("foo", "foo")
            .withProperty("bar", "bar")
            .store();

        Entity e2 = createEntity("Kind", key)
            .withProperty("foo", "foo")
            .store();

        Entity e3 = createEntity("Kind", key)
            .withProperty("bar", "bar")
            .store();

        Entity e4 = createEntity("Kind", key)
            .withProperty("baz", "baz")
            .store();

        Query query = new Query("Kind")
            .setAncestor(key)
            .addProjection(new PropertyProjection("foo", String.class))
            .addProjection(new PropertyProjection("bar", String.class));

        List<Entity> results = service.prepare(query).asList(withDefaults());
        assertEquals(Collections.singletonList(e1), results);
    }

    @Test
    public void testProjectionsWithoutType() throws Exception {
        String methodName = "testProjectionsWithoutType";
        Entity parent = createTestEntityWithUniqueMethodNameKey("Product", methodName);
        Key key = parent.getKey();

        Entity e = createEntity("Product", key)
            .withProperty("long", 123L)
            .store();

        Query query = new Query("Product")
            .setAncestor(key)
            .addProjection(new PropertyProjection("long", null));

        PreparedQuery preparedQuery = service.prepare(query);
        Entity result = preparedQuery.asSingleEntity();
        assertEquals(e.getKey(), result.getKey());

        RawValue rawValue = (RawValue) result.getProperty("long");
        assertEquals(Long.valueOf(123L), rawValue.asType(Long.class));
        assertEquals(Long.valueOf(123L), rawValue.asStrictType(Long.class));
    }

    @Test
    public void testProjectionOfCollectionProperties() throws Exception {
        String methodName = "testProjectionOfCollectionProperties";
        Entity parent = createTestEntityWithUniqueMethodNameKey("test", methodName);
        Key key = parent.getKey();

        Entity e = createEntity("test", key)
            .withProperty("prop", Arrays.asList("bbb", "ccc", "aaa"))
            .store();

        Query query = new Query("test")
            .setAncestor(key)
            .addProjection(new PropertyProjection("prop", String.class))
            .addSort("prop");

        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> results = preparedQuery.asList(withDefaults());
        assertEquals(3, results.size());

        Entity firstResult = results.get(0);
        Entity secondResult = results.get(1);
        Entity thirdResult = results.get(2);

        assertEquals(e.getKey(), firstResult.getKey());
        assertEquals(e.getKey(), secondResult.getKey());
        assertEquals(e.getKey(), thirdResult.getKey());
        assertEquals("aaa", firstResult.getProperty("prop"));
        assertEquals("bbb", secondResult.getProperty("prop"));
        assertEquals("ccc", thirdResult.getProperty("prop"));
    }

    @Test
    public void testProjectionOfCollectionPropertyWithFilterOnCollectionProperty() throws Exception {
        String methodName = "testProjectionOfCollectionPropertyWithFilterOnCollectionProperty";
        Entity parent = createTestEntityWithUniqueMethodNameKey("Product", methodName);
        Key key = parent.getKey();

        Entity e = createEntity("Product", key)
            .withProperty("name", Arrays.asList("aaa", "bbb"))
            .withProperty("price", Arrays.asList(10L, 20L))
            .store();

        Query query = new Query("Product")
            .setAncestor(key)
            .addProjection(new PropertyProjection("name", String.class))
            .setFilter(new Query.FilterPredicate("price", GREATER_THAN, 0L))
            .addSort("price")
            .addSort("name");

        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> results = preparedQuery.asList(withDefaults());
        assertEquals(4, results.size());

        assertEquals(e.getKey(), results.get(0).getKey());
        assertEquals(e.getKey(), results.get(1).getKey());
        assertEquals(e.getKey(), results.get(2).getKey());
        assertEquals(e.getKey(), results.get(3).getKey());

        assertEquals("aaa", results.get(0).getProperty("name"));
        assertEquals("bbb", results.get(1).getProperty("name"));
        assertEquals("aaa", results.get(2).getProperty("name"));
        assertEquals("bbb", results.get(3).getProperty("name"));
    }

    @Test
    public void testProjectionQueriesHandleEntityModificationProperly() throws Exception {
        String methodName = "testProjectionAfterRemove";
        Entity parent = createTestEntityWithUniqueMethodNameKey("test", methodName);
        Key key = parent.getKey();

        Entity e = createEntity("test", key)
            .withProperty("prop", Arrays.asList("aaa", "bbb", "ccc"))
            .store();

        Query query = new Query("test")
            .setAncestor(key)
            .addProjection(new PropertyProjection("prop", String.class))
            .addSort("prop");

        assertEquals(3, service.prepare(query).asList(withDefaults()).size());

        e = createEntity(e.getKey())
            .withProperty("prop", Arrays.asList("aaa", "bbb"))
            .store();

        assertEquals(2, service.prepare(query).asList(withDefaults()).size());

        service.delete(e.getKey());

        assertEquals(0, service.prepare(query).asList(withDefaults()).size());
    }

    @Test
    public void testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatementWhenUsingProjections() throws Exception {
        String methodName = "testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatementWhenUsingProjections";
        Entity parent = createTestEntityWithUniqueMethodNameKey("Product", methodName);
        Key key = parent.getKey();

        Entity b = createEntity("Product", key)
            .withProperty("name", "b")
            .withProperty("price", 1L)
            .store();

        Entity a = createEntity("Product", key)
            .withProperty("name", "a")
            .withProperty("price", 2L)
            .store();

        Query query = new Query("Product")
            .setAncestor(key)
            .addProjection(new PropertyProjection("price", Long.class))
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        assertResultsInOrder(query, a, b);

        query = query.setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("b", "a")));
        assertResultsInOrder(query, b, a);
    }

    @Test
    public void testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatementWhenUsingKeysOnly() throws Exception {
        String methodName = "testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatementWhenUsingKeysOnly";
        Entity parent = createTestEntityWithUniqueMethodNameKey("Product", methodName);
        Key key = parent.getKey();

        Entity b = createEntity("Product", key)
            .withProperty("name", "b")
            .store();

        Entity a = createEntity("Product", key)
            .withProperty("name", "a")
            .store();

        Query query = new Query("Product")
            .setAncestor(key)
            .setKeysOnly()
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        assertResultsInOrder(query, a, b);

        query = query.setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("b", "a")));
        assertResultsInOrder(query, b, a);
    }

    @Test
    public void testEntityOnlyContainsProjectedProperties() throws Exception {
        String methodName = "testEntityOnlyContainsProjectedProperties";
        Entity parent = createTestEntityWithUniqueMethodNameKey("Product", methodName);
        Key key = parent.getKey();

        Entity b = createEntity("Product", key)
            .withProperty("name", "b")
            .withProperty("price", 1L)
            .store();

        Entity a = createEntity("Product", key)
            .withProperty("name", "a")
            .withProperty("price", 2L)
            .store();

        Query query = new Query("Product")
            .setAncestor(key)
            .addProjection(new PropertyProjection("price", Long.class))
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        Entity firstResult = service.prepare(query).asList(FetchOptions.Builder.withDefaults()).get(0);

        assertEquals(1, firstResult.getProperties().size());
        assertEquals("price", firstResult.getProperties().keySet().iterator().next());

        query = new Query("Product")
            .setKeysOnly()
            .setFilter(new Query.FilterPredicate("name", IN, Arrays.asList("a", "b")));
        firstResult = service.prepare(query).asList(FetchOptions.Builder.withDefaults()).get(0);

        assertEquals(0, firstResult.getProperties().size());
    }

    private void assertResultsInOrder(Query query, Entity first, Entity second) {
        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> results = preparedQuery.asList(FetchOptions.Builder.withDefaults());

        Entity firstResult = results.get(0);
        Entity secondResult = results.get(1);

        assertEquals(first.getKey(), firstResult.getKey());
        assertEquals(second.getKey(), secondResult.getKey());
    }
}

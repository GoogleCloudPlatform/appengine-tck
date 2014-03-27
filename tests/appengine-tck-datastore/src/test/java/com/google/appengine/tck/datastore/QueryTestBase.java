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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.NOT_EQUAL;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public abstract class QueryTestBase extends DatastoreHelperTestBase {

    protected static final String TEST_ENTITY_KIND = "test";
    protected static final String SINGLE_PROPERTY_NAME = "prop";

    private int idSequence;

    @Deployment
    public static WebArchive getDeployment() {
        return getHelperDeployment()
            .addAsWebInfResource("datastore-indexes.xml")
            .addClass(QueryTestBase.class);
    }

    protected static Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        //noinspection MagicConstant
        cal.set(year, month - 1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    protected void assertList(List<Entity> expected, List<Entity> actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

    protected void assertSet(Set<Entity> expected, Set<Entity> actual) {
        assertSet(null, expected, actual);
    }

    protected void assertSet(String message, Set<Entity> expected, Set<Entity> actual) {
        Assert.assertNotNull(message, actual);
        if (expected.size() != actual.size()) {
            log.warning("Expected Result Set:" + expected.toString());
            log.warning("Actual Result Set:" + actual.toString());
        }
        Assert.assertEquals(message, expected.size(), actual.size());
        Set<Entity> copy = new HashSet<>(expected);
        copy.removeAll(actual);
        Assert.assertTrue(message, copy.size() == 0);
    }

    protected void assertSingleResult(Entity expectedEntity, Query query) {
        PreparedQuery preparedQuery = service.prepare(query);
        assertEquals("number of results", 1, preparedQuery.countEntities(withDefaults()));

        Entity entityFromQuery = preparedQuery.asSingleEntity();
        assertEquals(expectedEntity, entityFromQuery);
    }

    protected void assertNoResults(Query query) {
        PreparedQuery preparedQuery = service.prepare(query);
        Assert.assertEquals("number of results", 0, preparedQuery.countEntities(withDefaults()));
    }

    protected TestEntityBuilder buildTestEntity() {
        return createEntity(TEST_ENTITY_KIND, ++idSequence);
    }

    protected TestEntityBuilder buildTestEntity(Key parentKey) {
        return createEntity(TEST_ENTITY_KIND, parentKey);
    }

    protected TestEntityBuilder createEntity(String kind, int id) {
        return new TestEntityBuilder(kind, id);
    }

    protected TestEntityBuilder createEntity(String kind, Key parent) {
        return new TestEntityBuilder(kind, parent);
    }

    protected TestEntityBuilder createEntity(Key key) {
        return new TestEntityBuilder(key);
    }

    protected Entity storeTestEntityWithSingleProperty(Object value) {
        return buildTestEntity()
            .withProperty(SINGLE_PROPERTY_NAME, value)
            .store();
    }

    protected Entity storeTestEntityWithSingleProperty(Key parent, Object value) {
        TestEntityBuilder testEntityBuilder = createEntity(TEST_ENTITY_KIND, parent);
        return testEntityBuilder
            .withProperty(SINGLE_PROPERTY_NAME, value)
            .store();
    }

    protected Query createQuery(Query.FilterOperator operator, Object value) {
        return createQuery(createFilter(operator, value));
    }

    private Query createQuery(Query.Filter filter) {
        return createQuery().setFilter(filter);
    }

    private Query createQuery(Query.Filter filter, Key parent) {
        return createQuery().setAncestor(parent).setFilter(filter);
    }

    private Query.FilterPredicate createFilter(Query.FilterOperator operator, Object value) {
        return new Query.FilterPredicate(SINGLE_PROPERTY_NAME, operator, value);
    }

    protected Query createQuery() {
        return new Query(TEST_ENTITY_KIND);
    }

    protected Set<Entity> queryReturnsNothing() {
        return queryReturns();
    }

    protected Set<Entity> queryReturns(Entity... entities) {
        return new HashSet<Entity>(Arrays.asList(entities));
    }

    protected List<Entity> containsResultsInOrder(Entity... entities) {
        return Arrays.asList(entities);
    }

    protected Set<Entity> whenFilteringBy(Query.FilterOperator operator, Object value) {
        return whenFilteringWith(createFilter(operator, value));
    }

    protected Set<Entity> whenFilteringBy(Query.FilterOperator operator, Object value, Key parent) {
        return whenFilteringWith(createFilter(operator, value), parent);
    }

    protected Set<Entity> whenFilteringWith(Query.Filter filter) {
        Query query = createQuery(filter);
        List<Entity> results = service.prepare(query).asList(withDefaults());
        return new HashSet<Entity>(results);
    }

    protected Set<Entity> whenFilteringWith(Query.Filter filter, Key parent) {
        Query query = createQuery(filter, parent);
        List<Entity> results = service.prepare(query).asList(withDefaults());
        return new HashSet<Entity>(results);
    }

    protected List<Entity> listReturnedWhenFilteringBy(Query.FilterOperator operator, Object value) {
        Query query = createQuery(operator, value);
        return service.prepare(query).asList(withDefaults());
    }

    /**
     * Tests if querying by GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN and LESS_THAN_OR_EQUAL returns
     * the correct results.
     *
     * @param lowValue  The lowest value among the three values.
     * @param midValue  The medium value among the three values.
     * @param highValue The highest value among the three values.
     */
    protected void testInequalityQueries(Object lowValue, Object midValue, Object highValue) {
        // we don't store the entities in a nice order (low, then mid, then high), because that could make the test
        // pass if the order in which the entities were stored was used for comparing.
        Entity parentEntity = createTestEntityWithUniqueMethodNameKey(TEST_ENTITY_KIND, "testInequalityQueries");
        Key key = parentEntity.getKey();

        Entity highEntity = storeTestEntityWithSingleProperty(key, highValue);
        Entity lowEntity = storeTestEntityWithSingleProperty(key, lowValue);
        Entity midEntity = storeTestEntityWithSingleProperty(key, midValue);
        Entity nullEntity = storeTestEntityWithSingleProperty(key, null);

        assertSet(queryReturns(midEntity, highEntity), whenFilteringBy(GREATER_THAN, lowValue, key));
        assertSet(queryReturns(midEntity, highEntity), whenFilteringBy(GREATER_THAN_OR_EQUAL, midValue, key));
        assertSet(queryReturns(nullEntity, midEntity, lowEntity), whenFilteringBy(LESS_THAN, highValue, key));
        assertSet(queryReturns(nullEntity, midEntity, lowEntity), whenFilteringBy(LESS_THAN_OR_EQUAL, midValue, key));

        clearData(TEST_ENTITY_KIND, key, 0);
    }

    /**
     * Tests whether given two entities with each having a single property, whose value is either foo or bar; when
     * querying by EQUAL foo, the query returns foo; and when querying by NOT_EQUAL foo, the query returns bar.
     *
     * @param foo property value for first entity
     * @param bar property value for second entity
     */
    protected void testEqualityQueries(Object foo, Object bar) {
        Entity parentEntity = createTestEntityWithUniqueMethodNameKey(TEST_ENTITY_KIND, "testEqualityQueries");
        Key key = parentEntity.getKey();


        Entity fooEntity = storeTestEntityWithSingleProperty(key, foo);
        Entity barEntity = storeTestEntityWithSingleProperty(key, bar);
        Entity noPropertyEntity = storeTestEntityWithoutProperties(key);

        assertSet(queryReturns(fooEntity), whenFilteringBy(EQUAL, foo, key));
        assertSet(queryReturns(barEntity), whenFilteringBy(NOT_EQUAL, foo, key));

        clearData(TEST_ENTITY_KIND, key, 0);
    }

    private Entity storeTestEntityWithoutProperties(Key parentKey) {
        return buildTestEntity(parentKey).store();
    }

    protected class TestEntityBuilder {

        private Entity entity;

        public TestEntityBuilder(String kind, int id) {
            entity = new Entity(kind, id);
        }

        public TestEntityBuilder(String kind, Key parent) {
            entity = new Entity(kind, parent);
        }

        public TestEntityBuilder(Key key) {
            entity = new Entity(key);
        }

        public TestEntityBuilder withProperty(String key, Object value) {
            entity.setProperty(key, value);
            return this;
        }

        public Entity store() {
            service.put(entity);
            return entity;
        }
    }

}

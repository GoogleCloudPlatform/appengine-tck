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

import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;

/**
 * Tests that check if certain queries throw DatastoreNeedIndexException when an explicit index is required for the query,
 * and don't throw the exception, when an explicit index is not required, because an automatic index is used for the query.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class RequiredIndexesTest extends QueryTestBase {

    @Test
    public void testKindlessQueryUsingOnlyAncestorAndKeyFiltersDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(new Query()
            .setAncestor(KeyFactory.createKey("Ancestor", 1))
            .setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, GREATER_THAN, KeyFactory.createKey("Kind", 1))));
    }

    @Test
    public void testQueryUsingOnlyEqualityFiltersDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(new Query("Unindexed")
            .setFilter(new Query.FilterPredicate("someProperty", EQUAL, "foo")));

        executeQuery(new Query("Unindexed")
            .setFilter(
                and(new Query.FilterPredicate("someProperty", EQUAL, "foo"),
                    new Query.FilterPredicate("otherProperty", EQUAL, "bar"))));
    }

    @Test
    public void testQueryUsingOnlyInequalityFiltersOnSinglePropertyDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(new Query("Unindexed")
            .setFilter(new Query.FilterPredicate("someProperty", GREATER_THAN, "foo")));

        executeQuery(new Query("Unindexed")
            .setFilter(
                and(new Query.FilterPredicate("someProperty", GREATER_THAN, "foo"),
                    new Query.FilterPredicate("someProperty", LESS_THAN, "bar"))));
    }

    @Test
    public void testQueryUsingOnlyAncestorAndEqualityFiltersDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(new Query("Unindexed")
            .setAncestor(KeyFactory.createKey("Ancestor", 1))
            .setFilter(new Query.FilterPredicate("someProperty", EQUAL, "foo")));

        executeQuery(new Query("Unindexed")
            .setAncestor(KeyFactory.createKey("Ancestor", 1))
            .setFilter(
                and(new Query.FilterPredicate("someProperty", EQUAL, "foo"),
                    new Query.FilterPredicate("otherProperty", EQUAL, "bar"))));
    }

    @Test(expected = DatastoreNeedIndexException.class)
    public void testQueryWithAncestorAndInequalityFiltersRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setAncestor(KeyFactory.createKey("Ancestor", 1))
                .setFilter(new Query.FilterPredicate("someProperty", GREATER_THAN, "a")));
    }

    @Test
    public void testQueryWithAncestorAndInequalityFilterOnKeyPropertyDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setAncestor(KeyFactory.createKey("Ancestor", 1))
                .setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, GREATER_THAN, KeyFactory.createKey("Unindexed", 1))));
    }

    @Test
    public void testQueryWithInequalityFilterOnKeyPropertyAndEqualityFilterOnOtherPropertyDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setFilter(
                    and(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, GREATER_THAN, KeyFactory.createKey("Unindexed", 1)),
                        new Query.FilterPredicate("otherProperty", EQUAL, "b"))));
    }

    @Test(expected = DatastoreNeedIndexException.class)
    public void testQueryWithEqualityFilterOnKeyPropertyAndInequalityFilterOnOtherPropertyRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setFilter(
                    and(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, EQUAL, KeyFactory.createKey("Unindexed", 1)),
                        new Query.FilterPredicate("someProperty", GREATER_THAN, "a"))));
    }

    @Test(expected = DatastoreNeedIndexException.class)
    public void testQueryWithInequalityFilterOnSomePropertyAndEqualityFilterOnOtherPropertyRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setFilter(
                    and(new Query.FilterPredicate("someProperty", GREATER_THAN, "a"),
                        new Query.FilterPredicate("otherProperty", EQUAL, "b"))));
    }

    @Ignore("Fails intermittently on appspot. Is an index needed in this case or not?")
    @Test(expected = DatastoreNeedIndexException.class)
    public void testQueryWithInequalityFilterOnSomePropertyAndEqualityFilterOnSamePropertyRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setFilter(
                    and(new Query.FilterPredicate("someProperty", EQUAL, "b"),
                        new Query.FilterPredicate("someProperty", GREATER_THAN, "a"))));
    }

    @Test(expected = DatastoreNeedIndexException.class)
    public void testAncestorQueryWithInequalityFilterOnSomePropertyAndEqualityFilterOnSamePropertyRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setAncestor(KeyFactory.createKey("Ancestor", 1))
                .setFilter(
                    and(new Query.FilterPredicate("someProperty", EQUAL, "b"),
                        new Query.FilterPredicate("someProperty", GREATER_THAN, "a"))));
    }

    @Test
    public void testQueryWithEqualityAndInequalityFiltersAndSortOnASinglePropertyDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setFilter(
                    and(new Query.FilterPredicate("someProperty", GREATER_THAN, "a"),
                        new Query.FilterPredicate("someProperty", EQUAL, "b")))
                .addSort("someProperty"));
    }

    @Test
    public void testAncestorQueryWithEqualityAndInequalityFiltersAndSortOnASinglePropertyDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setAncestor(KeyFactory.createKey("Ancestor", 1))
                .setFilter(
                    and(new Query.FilterPredicate("someProperty", GREATER_THAN, "a"),
                        new Query.FilterPredicate("someProperty", EQUAL, "b")))
                .addSort("someProperty"));
    }

    @Test
    public void testQueryUsingOnlyAncestorFiltersAndEqualityFiltersOnPropertiesAndInequalityFiltersOnKeysDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(new Query("Unindexed")
            .setAncestor(KeyFactory.createKey("Ancestor", 1))
            .setFilter(
                and(new Query.FilterPredicate("someProperty", EQUAL, "foo"),
                    new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, GREATER_THAN, KeyFactory.createKey("Unindexed", 1)))));
    }

    @Test
    public void testQueryWithoutFiltersAndOnlyOneSortOrderDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(new Query("Unindexed")
            .addSort("someProperty"));

        executeQuery(new Query("Unindexed")
            .addSort("someProperty", Query.SortDirection.DESCENDING));
    }

    @Test
    public void testQueryWithAncestorAndSortOrderOnKeyPropertyDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(new Query("Unindexed")
            .setAncestor(KeyFactory.createKey("Ancestor", 1))
            .addSort(Entity.KEY_RESERVED_PROPERTY));
    }

    @Test(expected = DatastoreNeedIndexException.class)
    public void testQueryWithAncestorAndSortOrderRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setAncestor(KeyFactory.createKey("Ancestor", 1))
                .addSort("someProperty"));
    }

    @Test
    public void testProjectionQueryOperatingOnlyOnASinglePropertyDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(new Query("Unindexed")
            .addProjection(new PropertyProjection("someProperty", null)));

        executeQuery(new Query("Unindexed")
            .addProjection(new PropertyProjection("someProperty", null))
            .setFilter(new Query.FilterPredicate("someProperty", GREATER_THAN, "a")));

        executeQuery(new Query("Unindexed")
            .addProjection(new PropertyProjection("someProperty", null))
            .setFilter(new Query.FilterPredicate("someProperty", GREATER_THAN, "a"))
            .addSort("someProperty"));

        executeQuery(new Query("Unindexed")
            .addProjection(new PropertyProjection("someProperty", null))
            .setFilter(new Query.FilterPredicate("someProperty", GREATER_THAN, "a"))
            .addSort("someProperty", Query.SortDirection.DESCENDING));
    }

    @Test(expected = DatastoreNeedIndexException.class)
    public void testQueryWithDescendingSortOrderOnKeysRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .addSort(Entity.KEY_RESERVED_PROPERTY, Query.SortDirection.DESCENDING));
    }

    @Test(expected = DatastoreNeedIndexException.class)
    public void testQueryWithMultipleSortOrdersRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .addSort("someProperty")
                .addSort("otherProperty"));
    }

    @Test(expected = DatastoreNeedIndexException.class)
    public void testQueryWithEqualityFilterAndSortOnAnotherPropertyRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setFilter(new Query.FilterPredicate("someProperty", EQUAL, "foo"))
                .addSort("otherProperty"));
    }

    @Test
    public void testQueryWithEqualityFilterAndSortOnKeyPropertyDoesNotRequireConfiguredIndex() throws Exception {
        executeQuery(new Query("Unindexed")
            .setFilter(new Query.FilterPredicate("someProperty", EQUAL, "foo"))
            .addSort(Entity.KEY_RESERVED_PROPERTY));
    }

    @Test(expected = DatastoreNeedIndexException.class)
    public void testQueryWithInequalityFilterAndSortOnAnotherPropertyRequiresConfiguredIndex() throws Exception {
        executeQuery(
            new Query("Unindexed")
                .setFilter(new Query.FilterPredicate("someProperty", GREATER_THAN, "foo"))
                .addSort("someProperty")
                .addSort("otherProperty"));
    }

    @Test
    public void testExceptionIsThrownOnlyWhenResultsAreAccessedAndNotEarlier() throws Exception {
        List<Entity> list = null;
        try {
            Query query = new Query("Unindexed")
                .addSort("someProperty")
                .addSort("otherProperty");

            PreparedQuery preparedQuery = service.prepare(query);
            list = preparedQuery.asList(withDefaults());
        } catch (DatastoreNeedIndexException ex) {
            Assert.fail("DatastoreNeedIndexException thrown too early");
        }

        try {
            list.size();
            Assert.fail("Expected DatastoreNeedIndexException");
        } catch (DatastoreNeedIndexException ex) {
            // pass
        }
    }

    private void executeQuery(Query query) {
        PreparedQuery preparedQuery = service.prepare(query);
        List<Entity> list = preparedQuery.asList(withDefaults());
        list.size();    // only here is the query actually executed
    }
}

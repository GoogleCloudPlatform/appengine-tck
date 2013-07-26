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
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.NOT_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)

public class QueryFilteringOnCollectionPropertyTest extends QueryTestBase {

    @After
    public void tearDown() {
        List<Entity> entities = service.prepare(new Query(TEST_ENTITY_KIND).setKeysOnly()).asList(withDefaults());
        for (Entity entity : entities) {
            service.delete(entity.getKey());
        }
    }

    @Test
    public void queryWithMultipleInequalityFiltersOnMultivaluedPropertyReturnsNothing() throws Exception {
        String testMethodName = "queryWithMultipleEqualityFiltersOnMultivaluedPropertyReturnsEntityIfAllFiltersMatch";
        Entity parentEntity = createTestEntityWithUniqueMethodNameKey(TEST_ENTITY_KIND, testMethodName);
        Key parentKey = parentEntity.getKey();

        storeTestEntityWithSingleProperty(parentKey, Arrays.asList(1, 2));
        assertSet(
            whenFilteringWith(and(
                new FilterPredicate(SINGLE_PROPERTY_NAME, GREATER_THAN, 1),
                new FilterPredicate(SINGLE_PROPERTY_NAME, LESS_THAN, 2)),
                parentKey),
            queryReturnsNothing());
    }


    @Test
    public void queryWithMultipleEqualityFiltersOnMultivaluedPropertyReturnsEntityIfAllFiltersMatch() throws Exception {
        String testMethodName = "queryWithMultipleInequalityFiltersOnMultivaluedPropertyReturnsNothing";
        Entity parentEntity = createTestEntityWithUniqueMethodNameKey(TEST_ENTITY_KIND, testMethodName);
        Key parentKey = parentEntity.getKey();

        Entity entity = storeTestEntityWithSingleProperty(parentKey, Arrays.asList(1, 2));


        assertSet(
            whenFilteringWith(and(
                new FilterPredicate(SINGLE_PROPERTY_NAME, EQUAL, 1),
                new FilterPredicate(SINGLE_PROPERTY_NAME, EQUAL, 2)),
                parentKey),
            queryReturns(entity));
    }

    @Test
    public void queryWithNotEqualFilter() throws Exception {
        String testMethodName = "queryWithNotEqualFilter";
        Entity parentEntity = createTestEntityWithUniqueMethodNameKey(TEST_ENTITY_KIND, testMethodName);
        Key parentKey = parentEntity.getKey();

        Entity entity12 = storeTestEntityWithSingleProperty(parentKey, Arrays.asList(1, 2));
        Entity entity123 = storeTestEntityWithSingleProperty(parentKey, Arrays.asList(1, 2, 3));

        // The NOT_EQUAL operator works as a "value is other than" test.
        assertSet(whenFilteringWith(new FilterPredicate(SINGLE_PROPERTY_NAME, NOT_EQUAL, 1), parentKey),
            queryReturns(entity12, entity123));

        assertSet(
            whenFilteringWith(and(
                new FilterPredicate(SINGLE_PROPERTY_NAME, NOT_EQUAL, 1),
                new FilterPredicate(SINGLE_PROPERTY_NAME, NOT_EQUAL, 2)),
                parentKey),
            queryReturns(entity123));    // NOTE: should only match entity123, but not entity12

    }

}

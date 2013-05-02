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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.SortDirection.ASCENDING;
import static com.google.appengine.api.datastore.Query.SortDirection.DESCENDING;
import static org.junit.Assert.assertTrue;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class QuerySortingTest extends QueryTestBase {

    private static String QUERY_SORTING_ENTITY = "QuerySortingEntity";

    @Test
    public void testSortingByStringProperty() throws Exception {
        String methodName = "testSortingByStringProperty";
        Entity parent = createTestEntityWithUniqueMethodNameKey(QUERY_SORTING_ENTITY, methodName);
        Key key = parent.getKey();

        // NOTE: intentionally storing entities in non-alphabetic order
        Entity bill = storeTestEntityWithSingleProperty(key, "Bill");
        Entity abraham = storeTestEntityWithSingleProperty(key, "Abraham");
        Entity carl = storeTestEntityWithSingleProperty(key, "Carl");

        assertList(whenSortingByTheSingleProperty(ASCENDING, key), containsResultsInOrder(abraham, bill, carl));
        assertList(whenSortingByTheSingleProperty(DESCENDING, key), containsResultsInOrder(carl, bill, abraham));

        service.delete(bill.getKey(), abraham.getKey(), carl.getKey());
    }

    @Test
    public void testSortingByIntegerProperty() throws Exception {
        String methodName = "testSortingByIntegerProperty";
        Entity parent = createTestEntityWithUniqueMethodNameKey(QUERY_SORTING_ENTITY, methodName);
        Key key = parent.getKey();

        Entity two = storeTestEntityWithSingleProperty(key, 2);
        Entity one = storeTestEntityWithSingleProperty(key, 1);
        Entity three = storeTestEntityWithSingleProperty(key, 3);

        assertList(whenSortingByTheSingleProperty(ASCENDING, key), containsResultsInOrder(one, two, three));
        assertList(whenSortingByTheSingleProperty(DESCENDING, key), containsResultsInOrder(three, two, one));

        service.delete(two.getKey(), one.getKey(), three.getKey());
    }

    @Test
    public void testSortingByFloatProperty() throws Exception {
        String methodName = "testSortingByFloatProperty";
        Entity parent = createTestEntityWithUniqueMethodNameKey(QUERY_SORTING_ENTITY, methodName);
        Key key = parent.getKey();

        Entity thirty = storeTestEntityWithSingleProperty(key, 30f);
        Entity two = storeTestEntityWithSingleProperty(key, 2f);
        Entity hundred = storeTestEntityWithSingleProperty(key, 100f);

        assertList(whenSortingByTheSingleProperty(ASCENDING, key), containsResultsInOrder(two, thirty, hundred));
        assertList(whenSortingByTheSingleProperty(DESCENDING, key), containsResultsInOrder(hundred, thirty, two));

        service.delete(thirty.getKey(), two.getKey(), hundred.getKey());
    }

    @Test
    public void testIntegerPropertySortingIsNotLexicographic() throws Exception {
        String methodName = "testIntegerPropertySortingIsNotLexicographic";
        Entity parent = createTestEntityWithUniqueMethodNameKey(QUERY_SORTING_ENTITY, methodName);
        Key key = parent.getKey();

        Entity ten = storeTestEntityWithSingleProperty(key, 10);
        Entity five = storeTestEntityWithSingleProperty(key, 5);

        Query query = createQuery().setAncestor(key).addSort(SINGLE_PROPERTY_NAME, ASCENDING);
        List<Entity> results = service.prepare(query).asList(withDefaults());

        assertTrue(results.indexOf(five) < results.indexOf(ten));   // if sorting were lexicographic, "10" would come before "5"
        service.delete(ten.getKey(), five.getKey());
    }

    @Test
    public void testSortingByDateProperty() throws Exception {
        String methodName = "testSortingByDateProperty";
        Entity parent = createTestEntityWithUniqueMethodNameKey(QUERY_SORTING_ENTITY, methodName);
        Key key = parent.getKey();

        Entity february2 = storeTestEntityWithSingleProperty(key, createDate(2011, 2, 2));
        Entity march3 = storeTestEntityWithSingleProperty(key, createDate(2011, 3, 3));
        Entity january1 = storeTestEntityWithSingleProperty(key, createDate(2011, 1, 1));

        assertList(whenSortingByTheSingleProperty(ASCENDING, key), containsResultsInOrder(january1, february2, march3));
        assertList(whenSortingByTheSingleProperty(DESCENDING, key), containsResultsInOrder(march3, february2, january1));

        service.delete(february2.getKey(), march3.getKey(), january1.getKey());
    }

    private List<Entity> whenSortingByTheSingleProperty(Query.SortDirection direction, Key parentKey) {
        Query query = createQuery().setAncestor(parentKey).addSort(SINGLE_PROPERTY_NAME, direction);
        return service.prepare(query).asList(withDefaults());
    }

}

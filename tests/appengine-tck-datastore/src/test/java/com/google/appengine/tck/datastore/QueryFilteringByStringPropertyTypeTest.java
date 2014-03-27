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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.IN;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)

public class QueryFilteringByStringPropertyTypeTest extends QueryTestBase {

    @Test
    public void queryByEqualReturnsEntityWithEqualPropertyValue() throws Exception {
        testEqualityQueries("foo", "bar");
        testInequalityQueries("aaa", "bbb", "ccc");
    }

    @Test
    public void queryDoesNotReturnResultIfFilterIsSubstringOfProperty() throws Exception {
        storeTestEntityWithSingleProperty("John Doe");
        Query query = createQuery(EQUAL, "John");
        assertNoResults(query);
    }

    @Test
    public void testQueryByIn() throws Exception {
        Entity john = storeTestEntityWithSingleProperty("John");
        Entity kate = storeTestEntityWithSingleProperty("Kate");
        Entity ashley = storeTestEntityWithSingleProperty("Ashley");

        assertSet(queryReturns(kate, ashley), whenFilteringBy(IN, Arrays.asList("Kate", "Ashley")));
    }

    @Test
    public void testOrderOfReturnedResultsIsSameAsOrderOfElementsInInStatement() throws Exception {
        Entity a = storeTestEntityWithSingleProperty("a");
        Entity b = storeTestEntityWithSingleProperty("b");
        Entity c = storeTestEntityWithSingleProperty("c");
        Entity n = storeTestEntityWithSingleProperty(null);

        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("a", "b")), containsResultsInOrder(a, b));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("b", "a")), containsResultsInOrder(b, a));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("c", "a", "b")), containsResultsInOrder(c, a, b));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("b", "c", "c", "b")), containsResultsInOrder(b, c));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList(null, "b")), containsResultsInOrder(n, b));
        assertList(listReturnedWhenFilteringBy(IN, Arrays.asList("b", null)), containsResultsInOrder(b, n));
    }

}

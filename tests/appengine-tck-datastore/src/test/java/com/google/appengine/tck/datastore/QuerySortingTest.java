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
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.SortDirection.ASCENDING;
import static com.google.appengine.api.datastore.Query.SortDirection.DESCENDING;
import static org.junit.Assert.assertThat;
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

        assertThat(whenSortingByTheSingleProperty(ASCENDING, key), queryReturnsList(abraham, bill, carl));
        assertThat(whenSortingByTheSingleProperty(DESCENDING, key), queryReturnsList(carl, bill, abraham));

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

        assertThat(whenSortingByTheSingleProperty(ASCENDING, key), queryReturnsList(one, two, three));
        assertThat(whenSortingByTheSingleProperty(DESCENDING, key), queryReturnsList(three, two, one));

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

        assertThat(whenSortingByTheSingleProperty(ASCENDING, key), queryReturnsList(two, thirty, hundred));
        assertThat(whenSortingByTheSingleProperty(DESCENDING, key), queryReturnsList(hundred, thirty, two));

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

        assertThat(whenSortingByTheSingleProperty(ASCENDING, key), queryReturnsList(january1, february2, march3));
        assertThat(whenSortingByTheSingleProperty(DESCENDING, key), queryReturnsList(march3, february2, january1));

        service.delete(february2.getKey(), march3.getKey(), january1.getKey());
    }


    private Matcher<List<Entity>> queryReturnsList(Entity... entities) {
        return new IsEqual<List<Entity>>(Arrays.asList(entities));
    }

    private List<Entity> whenSortingByTheSingleProperty(Query.SortDirection direction) {
        Query query = createQuery()
            .addSort(SINGLE_PROPERTY_NAME, direction);
        return service.prepare(query).asList(withDefaults());
    }

    private List<Entity> whenSortingByTheSingleProperty(Query.SortDirection direction, Key parentKey) {
        Query query = createQuery()
            .setAncestor(parentKey)
            .addSort(SINGLE_PROPERTY_NAME, direction);
        return service.prepare(query).asList(withDefaults());
    }

}

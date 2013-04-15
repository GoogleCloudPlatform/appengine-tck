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

import java.util.List;

import com.google.appengine.api.datastore.Entity;
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

    @Test
    public void testSortingByStringProperty() throws Exception {
        // NOTE: intentionally storing entities in non-alphabetic order
        Entity bill = storeTestEntityWithSingleProperty("Bill");
        Entity abraham = storeTestEntityWithSingleProperty("Abraham");
        Entity carl = storeTestEntityWithSingleProperty("Carl");

        assertList(whenSortingByTheSingleProperty(ASCENDING), containsResultsInOrder(abraham, bill, carl));
        assertList(whenSortingByTheSingleProperty(DESCENDING), containsResultsInOrder(carl, bill, abraham));
    }

    @Test
    public void testSortingByIntegerProperty() throws Exception {
        Entity two = storeTestEntityWithSingleProperty(2);
        Entity one = storeTestEntityWithSingleProperty(1);
        Entity three = storeTestEntityWithSingleProperty(3);

        assertList(whenSortingByTheSingleProperty(ASCENDING), containsResultsInOrder(one, two, three));
        assertList(whenSortingByTheSingleProperty(DESCENDING), containsResultsInOrder(three, two, one));
    }

    @Test
    public void testSortingByFloatProperty() throws Exception {
        Entity thirty = storeTestEntityWithSingleProperty(30f);
        Entity two = storeTestEntityWithSingleProperty(2f);
        Entity hundred = storeTestEntityWithSingleProperty(100f);

        assertList(whenSortingByTheSingleProperty(ASCENDING), containsResultsInOrder(two, thirty, hundred));
        assertList(whenSortingByTheSingleProperty(DESCENDING), containsResultsInOrder(hundred, thirty, two));
    }

    @Test
    public void testIntegerPropertySortingIsNotLexicographic() throws Exception {
        Entity ten = storeTestEntityWithSingleProperty(10);
        Entity five = storeTestEntityWithSingleProperty(5);

        Query query = createQuery().addSort(SINGLE_PROPERTY_NAME, ASCENDING);
        List<Entity> results = service.prepare(query).asList(withDefaults());

        assertTrue(results.indexOf(five) < results.indexOf(ten));   // if sorting were lexicographic, "10" would come before "5"
    }

    @Test
    public void testSortingByDateProperty() throws Exception {
        Entity february2 = storeTestEntityWithSingleProperty(createDate(2011, 2, 2));
        Entity march3 = storeTestEntityWithSingleProperty(createDate(2011, 3, 3));
        Entity january1 = storeTestEntityWithSingleProperty(createDate(2011, 1, 1));

        assertList(whenSortingByTheSingleProperty(ASCENDING), containsResultsInOrder(january1, february2, march3));
        assertList(whenSortingByTheSingleProperty(DESCENDING), containsResultsInOrder(march3, february2, january1));
    }

    private List<Entity> whenSortingByTheSingleProperty(Query.SortDirection direction) {
        Query query = createQuery().addSort(SINGLE_PROPERTY_NAME, direction);
        return service.prepare(query).asList(withDefaults());
    }

}

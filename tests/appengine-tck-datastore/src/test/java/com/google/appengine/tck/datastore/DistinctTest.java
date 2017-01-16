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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * datastore Queries test, Projection, setFilter.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class DistinctTest extends DatastoreTestBase {
    private FetchOptions fo = FetchOptions.Builder.withDefaults();
    private String kindName = "distincttest";
    private int count = 30;

    @Before
    public void createData() throws InterruptedException {
        Query query = new Query(kindName, rootKey);
        if (service.prepare(query).countEntities(fo) == 0) {
            int props = 15;
            List<Entity> elist = new ArrayList<Entity>();
            elist.clear();
            for (int i = 0; i < count; i++) {
                Entity newRec = new Entity(kindName, rootKey);
                if (i == 0) {
                    newRec.setProperty("stringData", null);
                } else if (i == 15) {
                    newRec.setProperty("stringData", "");
                } else {
                    newRec.setProperty("stringData", "string" + i / (props));
                }
                newRec.setProperty("timestamp", new Date());
                newRec.setProperty("intData", i / (props - 2));
                if (i == 4) {
                    newRec.setProperty("floatData", null);
                } else {
                    newRec.setProperty("floatData", new Double(12 + i / (props - 4) * 0.1));
                }
                if ((count % 2) == 0) {
                    newRec.setProperty("booleanData", true);
                } else {
                    newRec.setProperty("booleanData", false);
                }
                newRec.setProperty("geoptData",
                    new GeoPt((float) (i / (props - 9) + 12.0), (float) (i / (props - 9) + 90.0)));
                newRec.setProperty("intList",
                    Arrays.asList(i / (props - 11), 50 + i / (props - 11), 90 + i / (props - 11)));
                elist.add(newRec);
            }
            service.put(elist);
            sync(waitTime);
        }
    }

    @Test
    public void testDistinctStr() {
        Query query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("stringData", String.class));
        query.setDistinct(true);
        assertTrue(query.getDistinct());
        // distinct false
        query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("stringData", String.class));
        query.setDistinct(false);
        assertEquals(count, service.prepare(query).countEntities(fo));
        assertFalse(query.getDistinct());
    }

    @Test
    public void testDistinctNum() {
        Query query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("floatData", Float.class));
        query.setDistinct(true);
        assertEquals(4, service.prepare(query).countEntities(fo));
        assertTrue(query.getDistinct());
        query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("floatData", Float.class));
        query.setDistinct(false);
        assertEquals(count, service.prepare(query).countEntities(fo));
        assertFalse(query.getDistinct());
    }

    @Test
    public void testDistinctList() {
        Query query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("intList", Integer.class));
        query.setDistinct(true);
        assertEquals("Expecting 24 entities: " + service.prepare(query).asList(fo), 24, service.prepare(query).countEntities(fo));
        assertTrue(query.getDistinct());
        // distinct false
        query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("intList", Integer.class));
        query.setDistinct(false);
        // 3 cols * 30 rows
        assertEquals(count * 3, service.prepare(query).countEntities(fo));
        assertFalse(query.getDistinct());
    }

    @Test
    public void testDistinctMix() {
        Query query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("stringData", String.class));
        query.addProjection(new PropertyProjection("floatData", Float.class));
        query.setDistinct(true);
        assertEquals(7, service.prepare(query).countEntities(fo));
        assertTrue(query.getDistinct());
        // distinct false
        query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("stringData", String.class));
        query.addProjection(new PropertyProjection("floatData", Float.class));
        query.setDistinct(false);
        assertEquals(count, service.prepare(query).countEntities(fo));
        assertFalse(query.getDistinct());
    }

    @Test
    public void testDistinctSort() {
        Query query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("stringData", String.class));
        query.addProjection(new PropertyProjection("floatData", Float.class));
        query.addSort("stringData", Query.SortDirection.DESCENDING);
        query.setDistinct(true);
        assertEquals(7, service.prepare(query).countEntities(fo));
        assertTrue(query.getDistinct());
        query.addSort("floatData", Query.SortDirection.DESCENDING);
        assertEquals(7, service.prepare(query).countEntities(fo));
    }

    @Test
    public void testDistinctFilter() {
        Query query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("stringData", String.class));
        query.addProjection(new PropertyProjection("floatData", Float.class));
        query.setFilter(new FilterPredicate("stringData", Query.FilterOperator.NOT_EQUAL, "string1"));
        query.addSort("stringData", Query.SortDirection.DESCENDING);
        query.setDistinct(true);
        assertEquals(5, service.prepare(query).countEntities(fo));
        assertTrue(query.getDistinct());
    }

    @Test
    public void testDistinctFilter2() {
        Query query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("stringData", String.class));
        query.addProjection(new PropertyProjection("floatData", Float.class));
        query.setFilter(new FilterPredicate("stringData", Query.FilterOperator.GREATER_THAN,
            "string0"));
        query.addSort("stringData", Query.SortDirection.DESCENDING);
        query.addSort("floatData", Query.SortDirection.DESCENDING);
        query.setDistinct(true);
        assertEquals(2, service.prepare(query).countEntities(fo));
        assertTrue(query.getDistinct());
    }
}

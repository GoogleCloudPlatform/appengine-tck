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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * datastore Queries test, Projection, setFilter.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class QueryTest extends DatastoreTestBase {
    private String kindName = "querytest";
    private int count = 3;
    private FetchOptions fo = FetchOptions.Builder.withDefaults();

    @Before
    public void createData() throws InterruptedException {
        clearData(kindName);
        List<Entity> elist = new ArrayList<Entity>();
        elist.clear();
        for (int i = 0; i < count; i++) {
            Entity newRec = new Entity(kindName, rootKey);
            newRec.setProperty("stringData", "string data" + i);
            newRec.setProperty("timestamp", new Date());
            newRec.setProperty("shortBlobData", new ShortBlob(("shortBlobData" + i).getBytes()));
            newRec.setProperty("intData", 20 * i);
            newRec.setProperty("textData", new Text("textData" + i));
            newRec.setProperty("floatData", new Double(1234 + 0.1 * i));
            newRec.setProperty("booleanData", true);
            newRec.setProperty("urlData", new Link("http://www.google.com"));
            newRec.setProperty("emailData", new Email("somebody123" + i + "@google.com"));
            newRec.setProperty("phoneData", new PhoneNumber("408-123-000" + i));
            newRec.setProperty("adressData", new PostalAddress("123 st. CA 12345" + i));
            newRec.setProperty("ratingData", new Rating(10 * i));
            newRec.setProperty("geoptData", new GeoPt((float) (i + 0.12), (float) (i + 0.98)));
            newRec.setProperty("categoryData", new Category("category" + i));
            newRec.setProperty("intList", Arrays.asList(i, 50 + i, 90 + i));
            elist.add(newRec);
        }

        service.put(elist);
        sync(1000);
    }

    @Test
    public void testSetFilterString() {
        Query query = new Query(kindName, rootKey);
        Filter filter = Query.CompositeFilterOperator.or(
            Query.FilterOperator.GREATER_THAN.of("stringData", "string data2"),
            Query.FilterOperator.LESS_THAN.of("stringData", "string data1"));
        query.setFilter(filter);
        assertEquals(1, service.prepare(query).countEntities(fo));
    }

    @Test
    public void testSetFilterShortBlob() {
        Query query = new Query(kindName, rootKey);
        Filter filter1 = Query.FilterOperator.EQUAL.of("shortBlobData",
            new ShortBlob("shortBlobData0".getBytes()));
        Filter filter2 = Query.FilterOperator.LESS_THAN_OR_EQUAL.of("shortBlobData",
            new ShortBlob("shortBlobData1".getBytes()));
        query.setFilter(Query.CompositeFilterOperator.or(filter1, filter2));
        assertEquals(2, service.prepare(query).countEntities(fo));
    }

    @Test
    public void testSetFilterInt() {
        Query query = new Query(kindName, rootKey);
        List<Filter> filterList = new ArrayList<Filter>();
        filterList.add(Query.FilterOperator.EQUAL.of("intData", 20));
        filterList.add(Query.FilterOperator.GREATER_THAN.of("intData", 0));
        Filter filter = Query.CompositeFilterOperator.and(filterList);
        query.setFilter(filter);
        assertEquals(1, service.prepare(query).countEntities(fo));
    }

    @Test
    public void testSetFilterRating() {
        Query query = new Query(kindName, rootKey);
        List<Filter> filterList = new ArrayList<Filter>();
        filterList.add(Query.FilterOperator.LESS_THAN.of("ratingData", new Rating(30)));
        filterList.add(Query.FilterOperator.GREATER_THAN.of("ratingData", new Rating(0)));
        Filter filter1 = Query.CompositeFilterOperator.or(filterList);
        Filter filter2 = Query.FilterOperator.EQUAL.of("ratingData", new Rating(20));
        query.setFilter(Query.CompositeFilterOperator.and(filter1, filter2));
        assertEquals(1, service.prepare(query).countEntities(fo));
    }

    @Test
    public void testSetFilterList() {
        // [0,50,90], [1,51,91], [2,52,92]
        Query query = new Query(kindName, rootKey);
        List<Filter> filterList = new ArrayList<Filter>();
        filterList.add(Query.FilterOperator.LESS_THAN.of("intList", 5));
        filterList.add(Query.FilterOperator.GREATER_THAN.of("intList", 90));
        Filter filter1 = Query.CompositeFilterOperator.OR.of(filterList);
        Filter filter2 = Query.FilterOperator.EQUAL.of("intList", 52);
        query.setFilter(Query.CompositeFilterOperator.AND.of(filter1, filter2));
        assertEquals(1, service.prepare(query).countEntities(fo));
    }

    @Test
    public void testWithPropertyProjection() {
        Query query = new Query(kindName, rootKey);
        query.addProjection(new PropertyProjection("geoptData", GeoPt.class));
        Filter filter1 = Query.CompositeFilterOperator.or(
            Query.FilterOperator.LESS_THAN.of("intList", 5),
            Query.FilterOperator.GREATER_THAN.of("intList", 90));
        Filter filter2 = Query.FilterOperator.EQUAL.of("intList", 52);
        query.setFilter(Query.CompositeFilterOperator.and(filter1, filter2));
        // sql statement
        String sql = "SELECT geoptData FROM " + kindName;
        sql += " WHERE ((intList < 5 or intList > 90) AND intList = 52)";
        sql += " AND __ancestor__ is " + rootKey;
        assertEquals(sql.toLowerCase(), query.toString().toLowerCase());
        // check query result
        List<Entity> results = service.prepare(query).asList(fo);
        Assert.assertTrue(results.size() > 0);
        assertEquals(new GeoPt((float) (2.12), (float) (2.98)), results.get(0).getProperty("geoptData"));
        for (Entity e : results) {
            assertEquals(1, e.getProperties().size());
            assertTrue(e.getProperties().containsKey("geoptData"));
        }
    }

    @Test
    public void testFilterPredicate() {
        Query query = new Query(kindName, rootKey);
        query.setFilter(new FilterPredicate("intData", Query.FilterOperator.EQUAL, 20));
        Entity e = service.prepare(query).asSingleEntity();
        assertEquals("check query kind", kindName, query.getKind());
        assertEquals("check query ancesor", rootKey, query.getAncestor());
        Query.FilterPredicate fp = (Query.FilterPredicate) query.getFilter();
        assertEquals("check FilterPredicate name", "intData", fp.getPropertyName());
        assertEquals("check FilterPredicate operator", Query.FilterOperator.EQUAL,
            fp.getOperator());
        assertEquals("check FilterPredicate value", e.getProperty("intData").toString(),
            fp.getValue().toString());
    }

    @Test
    public void testCompositeFilter() {
        Query query = new Query(kindName, rootKey);
        Filter filter = Query.CompositeFilterOperator.and(
            Query.FilterOperator.LESS_THAN_OR_EQUAL.of("intData", 40),
            Query.FilterOperator.GREATER_THAN.of("intData", 0));
        query.setFilter(filter);
        query.addSort("intData", Query.SortDirection.DESCENDING);
        List<Entity> es = service.prepare(query).asList(fo);
        assertEquals("check return count", 2, es.size());
        assertEquals("check query filter", filter, query.getFilter());
        assertEquals("check query key only", false, query.isKeysOnly());

        Query.CompositeFilter cf = (Query.CompositeFilter) query.getFilter();
        assertEquals(2, cf.getSubFilters().size());
        assertEquals(Query.CompositeFilterOperator.AND, cf.getOperator());
    }

    @Test
    public void testSortPredicates() {
        Query query = new Query(kindName, rootKey);
        query.addSort("intData", Query.SortDirection.DESCENDING);
        List<Entity> es = service.prepare(query).asList(fo);
        assertEquals((long) 40, es.get(0).getProperty("intData"));
        List<Query.SortPredicate> qsp = query.getSortPredicates();
        assertEquals("check SortPredicate name", "intData", qsp.get(0).getPropertyName());
        assertEquals("check SortPredicate direction", Query.SortDirection.DESCENDING,
            qsp.get(0).getDirection());
    }
}

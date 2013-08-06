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
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * datastore List data type test.
 *
 * @author hchen@google.com (Hannah Chen)
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class ListTest extends DatastoreTestBase {
    private String kindName = "listData";
    private FetchOptions fo = FetchOptions.Builder.withDefaults();
    private Key rootKey;

    @Before
    public void createData() throws InterruptedException {
        Entity newRec;
        Entity parent = createTestEntityWithUniqueMethodNameKey(kindName, "listTest");
        rootKey = parent.getKey();
        clearData(kindName);
        List<Entity> elist = new ArrayList<Entity>();
        newRec = new Entity(kindName, rootKey);
        newRec.setProperty("stringData", Arrays.asList("abc", "xyz", "mno"));
        newRec.setProperty("intData1", Arrays.asList(0, 55, 99));
        elist.add(newRec);

        newRec = new Entity(kindName, rootKey);
        newRec.setProperty("stringData", Arrays.asList("ppp", "iii", "ddd"));
        newRec.setProperty("intData1", Arrays.asList(1, 10, null));
        elist.add(newRec);

        newRec = new Entity(kindName, rootKey);
        newRec.setProperty("stringData", Arrays.asList("hannah", "luoluo", "jia"));
        newRec.setProperty("intData1", Arrays.asList(28, 15, 23));
        elist.add(newRec);
        service.put(elist);
        sync(waitTime);
    }

    @Test
    public void testStrFilter() {
        Query q = new Query(kindName);
        q.setAncestor(rootKey);
        Query.Filter filter = Query.CompositeFilterOperator.and(
            new FilterPredicate("stringData", Query.FilterOperator.LESS_THAN, "qqq"),
            new FilterPredicate("stringData", Query.FilterOperator.GREATER_THAN, "mmm"));
        q.setFilter(filter);
        q.addSort("stringData", Query.SortDirection.ASCENDING);
        assertEquals(2, service.prepare(q).countEntities(fo));
        List<Entity> elist = service.prepare(q).asList(fo);
        assertEquals(Arrays.asList("abc", "xyz", "mno"), elist.get(0).getProperty("stringData"));
        assertEquals(Arrays.asList("ppp", "iii", "ddd"), elist.get(1).getProperty("stringData"));
    }

    /**
     * Google issueId:1458158
     */
    @Test
    public void testIntFilter() {
        Query q = new Query(kindName);
        Query.Filter filter = Query.CompositeFilterOperator.and(
            new FilterPredicate("intData1", Query.FilterOperator.LESS_THAN, 20),
            new FilterPredicate("intData1", Query.FilterOperator.GREATER_THAN, 1),
            new FilterPredicate("intData1", Query.FilterOperator.EQUAL, null));
        q.setFilter(filter);
        q.addSort("intData1", Query.SortDirection.ASCENDING);
        q.setAncestor(rootKey);
        assertEquals(1, service.prepare(q).countEntities(fo));
        List<Entity> elist = service.prepare(q).asList(fo);
        assertEquals(Arrays.asList(1L, 10L, null), elist.get(0).getProperty("intData1"));
    }

}

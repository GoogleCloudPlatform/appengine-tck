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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tck.base.TestBase;
import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.Decoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BaseNCodec;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Datastore test helper
 */
public abstract class DatastoreHelperTestBase extends TestBase {
    protected String rootKind = "root";
    protected Key rootKey = null;
    protected String propertyName = "propName";
    protected DatastoreService service;
    protected int waitTime = 2000;

    protected static WebArchive getHelperDeployment() {
        WebArchive war = getTckDeployment();
        war.addClass(DatastoreHelperTestBase.class)
            .addClasses(Base64.class, BaseNCodec.class)
            .addClasses(BinaryEncoder.class, Encoder.class)
            .addClasses(BinaryDecoder.class, Decoder.class)
            .addClasses(EncoderException.class, DecoderException.class);
        return war;
    }

    @Before
    public void setUp() {
        try {
            service = DatastoreServiceFactory.getDatastoreService();
            ensureRootEntityExists();
        } catch (InterruptedException ie) {
            throw new IllegalStateException(ie);
        }
    }

    @After
    public void tearDown() {
        service = null;
    }

    protected FetchOptions withDefaults() {
        return FetchOptions.Builder.withDefaults();
    }

    protected void clearData(String kind) {
        clearData(kind, rootKey, waitTime);
    }

    protected void clearData(String kind, Key parentKey, int waitMilliSec) {
        List<Key> eList = new ArrayList<Key>();
        Query query = new Query(kind, parentKey);
        for (Entity readRec : service.prepare(query).asIterable()) {
            eList.add(readRec.getKey());
        }
        if (eList.size() > 0) {
            service.delete(eList);
            sync(waitMilliSec);
        }
    }

    protected Object[] getResult(Query query, String pName) {
        int count = service.prepare(query).countEntities(FetchOptions.Builder.withDefaults());
        Object result[] = new Object[count];
        int pt = 0;
        for (Entity readRec : service.prepare(query).asIterable()) {
            result[pt++] = readRec.getProperty(pName);
        }
        return result;
    }

    protected void doAllFilters(String kind, String pName, Object expected) {
        doNonEqFilters(kind, pName, expected);
        doEqFilters(kind, pName, expected);
    }

    protected void doNonEqFilters(String kind, String pName, Object expected) {
        verifyFilter(kind, pName, expected, Query.FilterOperator.LESS_THAN, 1, false);
        verifyFilter(kind, pName, expected, Query.FilterOperator.GREATER_THAN, 1, false);
    }

    protected void doEqFilters(String kind, String pName, Object expected) {
        verifyFilter(kind, pName, expected, Query.FilterOperator.LESS_THAN_OR_EQUAL, 2, true);
        verifyFilter(kind, pName, expected, Query.FilterOperator.EQUAL, 1, true);
        verifyFilter(kind, pName, expected, Query.FilterOperator.GREATER_THAN_OR_EQUAL, 2, true);
    }

    protected void doEqOnlyFilter(String kind, String pName, Object expected) {
        verifyFilter(kind, pName, expected, Query.FilterOperator.EQUAL, 1, true);
    }

    /**
     * inChk
     * - true: check if fDat are in result and if result count is correct;
     * - false: only check if result count is correct
     */
    protected void verifyFilter(String kind, String pName, Object fDat,
                                Query.FilterOperator operator, int rCont, boolean inChk) {
        Query query = new Query(kind, rootKey);
        query.setFilter(new FilterPredicate(pName, operator, fDat));
        Object[] result = getResult(query, pName);
        assertEquals(rCont, result.length);
        if (inChk) {
            boolean find = false;
            for (Object data : result) {
                if (data.toString().equals(fDat.toString())) {
                    find = true;
                }
            }
            assertEquals(true, find);
        }
    }

    protected void doSort(String kind, String pName, Object fDat, Query.SortDirection direction) {
        Query query = new Query(kind, rootKey);
        query.addSort(pName, direction);
        Object[] result = getResult(query, pName);
        assertEquals(fDat.toString(), result[0].toString());
    }

//    protected void doSort(String kind, String pName, Object[] expDat, Query.SortDirection direction) {
//        Query query = new Query(kind, rootKey);
//        query.addSort(pName, direction);
//        Object[] result = getResult(query, pName);
//        assertEquals(expDat.length, result.length);
//        assertTrue(Arrays.equals(result, expDat));
//    }

    protected void doSort(String kind, String pName, int expDat, Query.SortDirection direction) {
        Query query = new Query(kind, rootKey);
        query.addSort(pName, direction);
        Object[] result = getResult(query, pName);
        assertEquals(expDat, result.length);
    }

    protected void ensureRootEntityExists() throws InterruptedException {
        Query query = new Query(rootKind);

        // Clean up previous run
        List<Entity> entities = service.prepare(query).asList(withDefaults());
        for (Entity entity : entities) {
            service.delete(entity.getKey());
        }

        Entity rootEntity = service.prepare(query).asSingleEntity();
        if (rootEntity == null) {
            rootEntity = new Entity(rootKind);
            rootEntity.setProperty("name", "junittests group");
            rootKey = service.put(rootEntity);
            sync(waitTime);
        } else {
            rootKey = rootEntity.getKey();
        }
    }

    protected Collection<Entity> createTestEntities() {
        return Arrays.asList(createTestEntity("One", 1), createTestEntity("Two", 2), createTestEntity("Three", 3));
    }

    protected void assertStoreContainsAll(Collection<Entity> entities) throws EntityNotFoundException {
        for (Entity entity : entities) {
            assertStoreContains(entity);
        }
    }

    protected void assertStoreContains(Entity entity) throws EntityNotFoundException {
        Entity lookup = service.get(entity.getKey());
        Assert.assertNotNull(lookup);
        Assert.assertEquals(entity, lookup);
    }

    protected void assertStoreDoesNotContain(Entity entity) throws EntityNotFoundException {
        assertStoreDoesNotContain(entity.getKey());
    }

    protected void assertStoreDoesNotContain(Collection<Key> keys) throws EntityNotFoundException {
        for (Key key : keys) {
            assertStoreDoesNotContain(key);
        }
    }

    protected void assertStoreDoesNotContain(Key key) throws EntityNotFoundException {
        try {
            Entity storedEntity = service.get(key);
            Assert.fail("expected the datastore not to contain anything under key " + key + ", but it contained the entity " + storedEntity);
        } catch (EntityNotFoundException e) {
            // pass
        }
    }

    protected void assertEntityNotInRange(Entity entity, KeyRange range) {
        // allocated key should not be re-used.
        Assert.assertTrue(entity.getKey().getId() > range.getEnd().getId() ||
            entity.getKey().getId() < range.getStart().getId());
    }

    protected Entity createTestEntity() {
        return createTestEntity("KIND");
    }

    protected Entity createTestEntity(String kind) {
        Entity entity = new Entity(kind);
        entity.setProperty("text", "Some text.");
        return entity;
    }

    protected Entity createTestEntity(String kind, long id) {
        Key key = KeyFactory.createKey(kind, id);
        Entity entity = new Entity(key);
        entity.setProperty("text", "Some text.");
        return entity;
    }

    protected Entity createTestEntity(String kind, Key key) {
        Entity entity = new Entity(kind, key);
        entity.setProperty("text", "Some text.");
        return entity;
    }

    protected Entity createTestEntityWithUniqueMethodNameKey(String kind, String testMethodName) {
        String key = testMethodName + "-" + System.currentTimeMillis();
        return new Entity(kind, key);
    }

    protected void assertIAEWhenAccessingResult(PreparedQuery preparedQuery) {
        assertIAEWhenAccessingList(preparedQuery);
        assertIAEWhenAccessingIterator(preparedQuery);
        assertIAEWhenAccessingIterable(preparedQuery);
        assertIAEWhenGettingSingleEntity(preparedQuery);
    }

    protected List<Entity> doQuery(String kind, String pName, Class<?> type, boolean indexed) {
        FetchOptions fo = FetchOptions.Builder.withDefaults();
        Query query = new Query(kind, rootKey);
        if (indexed) {
            query.addProjection(new PropertyProjection(pName, type));
            query.addSort(pName);
        }
        return service.prepare(query).asList(fo);
    }

    private void assertIAEWhenAccessingList(PreparedQuery preparedQuery) {
        List<Entity> list = preparedQuery.asList(withDefaults());
        try {
            list.size();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    private void assertIAEWhenAccessingIterator(PreparedQuery preparedQuery) {
        Iterator<Entity> iterator = preparedQuery.asIterator();
        try {
            iterator.hasNext();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    private void assertIAEWhenAccessingIterable(PreparedQuery preparedQuery) {
        Iterator<Entity> iterator2 = preparedQuery.asIterable().iterator();
        try {
            iterator2.hasNext();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    private void assertIAEWhenGettingSingleEntity(PreparedQuery preparedQuery) {
        try {
            preparedQuery.asSingleEntity();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }
}

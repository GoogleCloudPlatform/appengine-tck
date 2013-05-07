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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.MemcacheSerialization;
import com.google.appengine.api.utils.SystemProperty;

import org.apache.commons.codec.binary.Base64;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * datastore key data type test.
 */
@RunWith(Arquillian.class)
public class KeyTest extends DatastoreTestBase {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String kindName = "keyData";

    @Before
    public void createData() throws InterruptedException {
        Query q = new Query(kindName, rootKey);
        if (service.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
            Entity newRec;
            String[] locDat = {"ac", "ab", "ae", "aa", "ac"};
            List<Entity> elist = new ArrayList<Entity>();
            int[] popDat = {8008278, 279557, 1222, 0, 12345};
            for (int i = 0; i < locDat.length; i++) {
                newRec = new Entity(kindName, rootKey);
                newRec.setProperty("loc", locDat[i]);
                newRec.setProperty("pop", popDat[i]);
                elist.add(newRec);
            }
            service.put(elist);
            sync(waitTime);
        }
    }

    @Test
    public void testKeyOrder() {
        Query query = new Query(kindName, rootKey);
        query.addSort("__key__");
        List<Entity> ascRecs = service.prepare(query).asList(withLimit(5));

        query = new Query(kindName, rootKey);
        query.addSort("__key__", Query.SortDirection.DESCENDING);
        List<Entity> descRecs = service.prepare(query).asList(withLimit(5));

        int size = ascRecs.size();
        assertEquals(5, size);
        for (int i = 0; i < size; i++) {
            assertEquals(ascRecs.get(i).getProperty("pop").toString(),
                descRecs.get(size - i - 1).getProperty("pop").toString());
        }
    }

    @Test
    public void testWithIneqi() {
        Query query = new Query(kindName, rootKey);
        query.setFilter(new FilterPredicate("loc", Query.FilterOperator.EQUAL, "ae"));
        Key key = service.prepare(query).asSingleEntity().getKey();

        query = new Query(kindName, rootKey);
        query.setFilter(new FilterPredicate("__key__", Query.FilterOperator.GREATER_THAN, key));
        query.addSort("__key__");
        List<Entity> ascRecs = service.prepare(query).asList(withLimit(5));

        query = new Query(kindName, rootKey);
        query.setFilter(new FilterPredicate("__key__", Query.FilterOperator.GREATER_THAN, key));
        query.addSort("__key__", Query.SortDirection.DESCENDING);
        List<Entity> descRecs = service.prepare(query).asList(withLimit(5));

        int size = ascRecs.size();
        for (int i = 0; i < size; i++) {
            assertEquals(ascRecs.get(i).getProperty("pop").toString(),
                descRecs.get(size - i - 1).getProperty("pop").toString());
        }
    }

    @Test
    public void testWithIneqiAndFilter() {
        Query query = new Query(kindName, rootKey);
        query.setFilter(new FilterPredicate("loc", Query.FilterOperator.EQUAL, "ae"));
        Key key = service.prepare(query).asSingleEntity().getKey();

        query = new Query(kindName, rootKey);
        query.setFilter(new FilterPredicate("__key__", Query.FilterOperator.LESS_THAN, key));
        query.setFilter(new FilterPredicate("loc", Query.FilterOperator.EQUAL, "ac"));
        query.addSort("__key__");
        List<Entity> ascRecs = service.prepare(query).asList(withLimit(5));

        query = new Query(kindName, rootKey);
        query.setFilter(new FilterPredicate("__key__", Query.FilterOperator.LESS_THAN, key));
        query.setFilter(new FilterPredicate("loc", Query.FilterOperator.EQUAL, "ac"));
        query.addSort("__key__", Query.SortDirection.DESCENDING);
        List<Entity> descRecs = service.prepare(query).asList(withLimit(5));

        int size = ascRecs.size();
        for (int i = 0; i < size; i++) {
            assertEquals(ascRecs.get(i).getProperty("pop").toString(),
                descRecs.get(size - i - 1).getProperty("pop").toString());
        }
    }

    @Test
    public void testWithNamespce() {
        String[] namespaceDat = {"", "developer", "testing"};
        Entity entity;
        String kindTest = kindName + "-NS";
        List<Key> kList = new ArrayList<Key>();
        // create data and get key
        for (int i = 0; i < namespaceDat.length; i++) {
            NamespaceManager.set(namespaceDat[i]);
            Query q = new Query(kindTest);
            if (service.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
                entity = new Entity(kindTest);
                if (namespaceDat[i].equals("")) {
                    entity.setProperty("jobType", "google");
                } else {
                    entity.setProperty("jobType", namespaceDat[i]);
                }
                service.put(entity);
            } else {
                entity = service.prepare(q).asSingleEntity();
            }
            kList.add(entity.getKey());
        }
        // query in same namespace
        for (int i = 0; i < namespaceDat.length; i++) {
            NamespaceManager.set(namespaceDat[i]);
            Query q = new Query(kindTest);
            q.setFilter(new FilterPredicate("__key__", Query.FilterOperator.EQUAL, kList.get(i)));
            if (namespaceDat[i].equals("")) {
                assertEquals(service.prepare(q).asSingleEntity().getProperty("jobType"),
                    "google");
            } else {
                assertEquals(service.prepare(q).asSingleEntity().getProperty("jobType"),
                    namespaceDat[i]);
            }
        }
        // query in different namespace
        NamespaceManager.set(namespaceDat[1]);
        Query q = new Query(kindTest);
        q.setFilter(new FilterPredicate("__key__", Query.FilterOperator.EQUAL, kList.get(2)));

        thrown.expect(IllegalArgumentException.class);
        service.prepare(q).asSingleEntity();
    }

    // http://b/issue?id=2106725
    @Test
    public void testKeySerialization() throws EntityNotFoundException, IOException {
        Key parentKeyB = KeyFactory.createKey("family", "same");
        Key childKeyB = KeyFactory.createKey(parentKeyB, "children", "same");
        Entity entB1 = new Entity(childKeyB);
        service.put(entB1);

        Entity entB2 = service.get(childKeyB);
        assertEquals(new String(MemcacheSerialization.makePbKey(entB1.getKey())),
            new String(MemcacheSerialization.makePbKey(childKeyB)));
        assertEquals(new String(MemcacheSerialization.makePbKey(entB2.getKey())),
            new String(MemcacheSerialization.makePbKey(childKeyB)));
        service.delete(childKeyB);
        service.delete(parentKeyB);
    }

    @Test
    public void testKeyBuilder() throws InterruptedException {
        String kind = "familyKey";
        clearData(kind);
        Entity parent = new Entity(kind);
        parent.setProperty("role", "father");
        Key pKey = service.put(parent);
        assertEquals(kind, pKey.getKind());
        String appId = pKey.getAppId();
        if (appId.indexOf('~') > -1) {
            appId = appId.substring(appId.indexOf('~') + 1);
        }
        assertEquals(SystemProperty.applicationId.get(), appId);

        Key cKey1 = new KeyFactory.Builder(pKey).addChild(kind, 1).getKey();
        assertEquals(-1, pKey.compareTo(cKey1));
        Entity child1 = new Entity(cKey1);
        child1.setProperty("role", "child1-gril");
        service.put(child1);
        assertEquals(pKey, cKey1.getParent());

        Key cKey2 = new KeyFactory.Builder(kind, 1).addChild(kind, "cousin1").getKey();
        Entity child2 = new Entity(cKey2);
        child2.setProperty("role", "cousin-gril");
        service.put(child2);
        assertEquals(true, cKey2.isComplete());

        Key cKey3 = pKey.getChild(kind, 2);
        Entity child3 = new Entity(cKey3);
        child1.setProperty("role", "child2-boy");
        service.put(child3);
        assertEquals(true, cKey3.equals(child3.getKey()));

        Key cKey4 = pKey.getChild(kind, "cousin2");
        Entity child4 = new Entity(cKey4);
        child1.setProperty("role", "cousin-boy");
        service.put(child4);
        assertEquals(cKey4.hashCode(), cKey4.hashCode());
    }

    @Test
    public void testKeyString() throws InterruptedException {
        String kind = "familyKeyString";
        clearData(kind);
        Entity parent = new Entity(kind);
        parent.setProperty("role", "father");
        Key pKey = service.put(parent);

        String keyString = KeyFactory.createKeyString(pKey, kind, 1);
        assertTrue(Base64.isBase64(keyString.getBytes()));
        Entity entity = new Entity(KeyFactory.stringToKey(keyString));
        entity.setProperty("role", "keystring+id");
        service.put(entity);
        assertEquals(1, entity.getKey().getId());
        assertEquals(kind, entity.getKey().getKind());

        keyString = KeyFactory.createKeyString(pKey, kind, "2");
        assertTrue(Base64.isBase64(keyString.getBytes()));
        entity = new Entity(KeyFactory.stringToKey(keyString));
        entity.setProperty("role", "keystring+name");
        service.put(entity);
        assertEquals("2", entity.getKey().getName());
        assertEquals(pKey, entity.getParent());

        keyString = KeyFactory.createKeyString(kind, 3);
        assertTrue(Base64.isBase64(keyString.getBytes()));
        entity = new Entity(KeyFactory.stringToKey(keyString));
        entity.setProperty("role", "keystring+id");
        service.put(entity);
        assertEquals(3, entity.getKey().getId());

        keyString = KeyFactory.createKeyString(kind, "4");
        assertTrue(Base64.isBase64(keyString.getBytes()));
        entity = new Entity(KeyFactory.stringToKey(keyString));
        entity.setProperty("role", "keystring+name");
        service.put(entity);
        assertEquals("4", entity.getKey().getName());
    }
}

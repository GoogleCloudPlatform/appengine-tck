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
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class DatastoreMultitenancyTest extends SimpleTestBase {

    private String originalNamespace;

    @Before
    public void setUp() {
        super.setUp();
        originalNamespace = NamespaceManager.get();
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
        NamespaceManager.set(originalNamespace);
    }

    private void deleteEntityList(List<Entity> entities) {
        List<Key> keys = new ArrayList<Key>();
        for (Entity entity : entities) {
            keys.add(entity.getKey());
        }
        service.delete(keys);
    }

    private void deleteNsKinds(String namespace, String kind) {
        String originalNs = NamespaceManager.get();
        NamespaceManager.set(namespace);

        List<Entity> entities = service.prepare(new Query(kind)).asList(withDefaults());
        deleteEntityList(entities);
        NamespaceManager.set(originalNs);
    }

    @Test
    public void testKeysCreatedUnderDifferentNamespacesAreNotEqual() throws Exception {
        NamespaceManager.set("one");
        Key key1 = KeyFactory.createKey("Test", 1);

        NamespaceManager.set("two");
        Key key2 = KeyFactory.createKey("Test", 1);

        assertFalse(key1.equals(key2));
    }

    @Test
    public void testTwoEntitiesWithSameKeyButDifferentNamespaceDontOverwriteEachOther() throws EntityNotFoundException {
        NamespaceManager.set("one");
        Key key1 = KeyFactory.createKey("Test", 1);
        Entity entity1 = new Entity(key1);
        service.put(entity1);
        assertEquals(entity1, service.get(key1));

        NamespaceManager.set("two");
        Key key2 = KeyFactory.createKey("Test", 1);

        try {
            Entity entity = service.get(key2);
            fail("Expected no entity in namespace 'two'; but got: " + entity);
        } catch (EntityNotFoundException e) {
        }

        Entity entity2 = new Entity(key2);
        service.put(entity2);
        assertEquals(entity2, service.get(key2));

        NamespaceManager.set("one");
        assertEquals(entity1, service.get(key1));

        service.delete(key1);
        service.delete(key2);
    }

    @Test
    public void testQueriesOnlyReturnResultsInCurrentNamespace() {
        deleteNsKinds("one", "foo");
        deleteNsKinds("two", "foo");
        sync();

        NamespaceManager.set("one");
        Entity fooOne = new Entity("foo");
        service.put(fooOne);

        NamespaceManager.set("two");
        Entity fooTwo = new Entity("foo");
        service.put(fooTwo);
        sync();

        List<Entity> listTwo = service.prepare(new Query("foo").setAncestor(fooTwo.getKey())).asList(withDefaults());
        assertEquals(Collections.singletonList(fooTwo), listTwo);

        NamespaceManager.set("one");
        List<Entity> listOne = service.prepare(new Query("foo").setAncestor(fooOne.getKey())).asList(withDefaults());
        assertEquals(Collections.singletonList(fooOne), listOne);

        service.delete(fooOne.getKey());
        service.delete(fooTwo.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueriesByAncestorInOtherNamespaceThrowsIllegalArgumentException() {
        deleteNsKinds("one", "foo");
        deleteNsKinds("two", "foo");
        sync();

        NamespaceManager.set("one");
        Entity fooOne = new Entity("foo");
        service.put(fooOne);

        NamespaceManager.set("two");
        Entity fooTwo = new Entity("foo");
        service.put(fooTwo);
        sync();

        // java.lang.IllegalArgumentException: Namespace of ancestor key and query must match.
        service.prepare(new Query("foo").setAncestor(fooOne.getKey())).asList(withDefaults());
    }

    @Test
    public void testQueryConsidersCurrentNamespaceWhenCreatedNotWhenPreparedOrExecuted() {
        deleteNsKinds("one", "foo");
        deleteNsKinds("two", "foo");
        sync();

        NamespaceManager.set("one");
        Entity fooOne = new Entity("foo");
        service.put(fooOne);

        NamespaceManager.set("two");
        Entity fooTwo = new Entity("foo");
        service.put(fooTwo);
        sync();

        Query query = new Query("foo").setAncestor(fooTwo.getKey()); // query created in namespace "two"

        NamespaceManager.set("one");
        PreparedQuery preparedQuery = service.prepare(query);
        assertEquals(fooTwo, preparedQuery.asSingleEntity());

        service.delete(fooOne.getKey());
        service.delete(fooTwo.getKey());
    }

    @Test
    public void testQueryOnKeyReservedPropertyInDifferentNamespace() {
        NamespaceManager.set("one");
        Key keyInNamespaceOne = KeyFactory.createKey("kind", 1);

        NamespaceManager.set("two");
        Query query = new Query().setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, EQUAL, keyInNamespaceOne));

        NamespaceManager.set("one"); // to make sure that the query's namespace is used when checking, not the current namespace

        try {
            service.prepare(query).asSingleEntity();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ok) {
        }

        try {
            service.prepare(query).asIterator().next();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ok) {
        }

        try {
            service.prepare(query).asList(withDefaults()).size();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ok) {
        }
    }

    @Test
    public void testQueryOnSomePropertyWithKeyInDifferentNamespace() {
        NamespaceManager.set("one");
        Key keyInNamespaceOne = KeyFactory.createKey("kind", 1);

        NamespaceManager.set("two");
        Query query = new Query("kind").setFilter(new Query.FilterPredicate("someProperty", EQUAL, keyInNamespaceOne));
        PreparedQuery preparedQuery = service.prepare(query);
        preparedQuery.asSingleEntity();    // should not throw IllegalArgumentException as in previous test
        preparedQuery.asIterator().hasNext();    // should not throw IllegalArgumentException as in previous test
        preparedQuery.asList(withDefaults()).size();    // should not throw IllegalArgumentException as in previous test
    }
}

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

        List<Entity> listTwo = service.prepare(new Query("foo")).asList(withDefaults());
        assertEquals(Collections.singletonList(fooTwo), listTwo);

        NamespaceManager.set("one");
        List<Entity> listOne = service.prepare(new Query("foo")).asList(withDefaults());
        assertEquals(Collections.singletonList(fooOne), listOne);

        service.delete(fooOne.getKey());
        service.delete(fooTwo.getKey());
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

        Query query = new Query("foo"); // query created in namespace "two"

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

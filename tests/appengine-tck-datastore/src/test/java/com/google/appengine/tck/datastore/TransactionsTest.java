/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)

public class TransactionsTest extends DatastoreTestBase {

    private static final String TRANSACTION_TEST_ENTITY = "TransactionsTestEntity";

    @Test
    public void testBasicTxPut() throws Exception {
        Entity entity = createTestEntity(TRANSACTION_TEST_ENTITY, System.currentTimeMillis());
        Transaction tx = service.beginTransaction();
        try {
            service.put(tx, entity);
            assertStoreDoesNotContain(entity);
            tx.commit();
            assertStoreContains(entity);
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    @Test
    public void testBasicNoTxPut() throws Exception {
        Entity entity = createTestEntity("NO_TX_KIND", 1);
        service.put(null, entity);
        assertStoreContains(entity);
    }

    @Test
    public void testBasicTxDelete() throws Exception {
        Entity entity = createTestEntity();
        service.put(entity);
        Transaction tx = service.beginTransaction();
        try {
            service.delete(tx, entity.getKey());
            assertStoreContains(entity);
            tx.commit();
            assertStoreDoesNotContain(entity);
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    @Test
    public void testBasicNoTxDelete() throws Exception {
        Entity entity = createTestEntity("NO_TX_KIND", 2);
        service.put(null, entity);
        assertStoreContains(entity);
        service.delete(entity.getKey());
        assertStoreDoesNotContain(entity);
    }

    @Test
    public void testRollbackWhenPuttingEntity() throws Exception {
        Entity entity = createTestEntity("ROLLBACK", 1);
        Transaction tx = service.beginTransaction();
        service.put(tx, entity);
        tx.rollback();
        // should not be there due to rollback
        assertStoreDoesNotContain(entity);
    }

    @Test
    public void testRollbackWhenModifyingEntity() throws Exception {
        Entity entity = new Entity("test");
        entity.setProperty("name", "original");
        Key key = service.put(entity);

        Transaction tx = service.beginTransaction();
        Entity entity2 = service.get(key);
        entity2.setProperty("name", "modified");
        service.put(tx, entity2);
        tx.rollback();

        Entity entity3 = service.get(key);
        assertEquals("original", entity3.getProperty("name"));
    }

    @Test
    public void testRollbackWhenModifyingEntityObtainedThroughQuery() throws Exception {
        assertRollbackSucceedsWhenResultFetchedWith(new ResultFetcher() {
            public Entity fetchResult(PreparedQuery preparedQuery) {
                return preparedQuery.asSingleEntity();
            }
        });
        assertRollbackSucceedsWhenResultFetchedWith(new ResultFetcher() {
            public Entity fetchResult(PreparedQuery preparedQuery) {
                return preparedQuery.asIterator().next();
            }
        });
        assertRollbackSucceedsWhenResultFetchedWith(new ResultFetcher() {
            public Entity fetchResult(PreparedQuery preparedQuery) {
                return preparedQuery.asIterable().iterator().next();
            }
        });
        assertRollbackSucceedsWhenResultFetchedWith(new ResultFetcher() {
            public Entity fetchResult(PreparedQuery preparedQuery) {
                return preparedQuery.asList(withDefaults()).get(0);
            }
        });
        assertRollbackSucceedsWhenResultFetchedWith(new ResultFetcher() {
            public Entity fetchResult(PreparedQuery preparedQuery) {
                return preparedQuery.asQueryResultIterator().next();
            }
        });
        assertRollbackSucceedsWhenResultFetchedWith(new ResultFetcher() {
            public Entity fetchResult(PreparedQuery preparedQuery) {
                return preparedQuery.asQueryResultIterable().iterator().next();
            }
        });
        assertRollbackSucceedsWhenResultFetchedWith(new ResultFetcher() {
            public Entity fetchResult(PreparedQuery preparedQuery) {
                return preparedQuery.asQueryResultList(withDefaults()).get(0);
            }
        });
    }

    private void assertRollbackSucceedsWhenResultFetchedWith(ResultFetcher resultFetcher) throws EntityNotFoundException {
        String methodName = "assertRollbackSucceedsWhenResultFetchedWith";
        Entity entity = createTestEntityWithUniqueMethodNameKey(TRANSACTION_TEST_ENTITY, methodName);
        Key parentKey = entity.getKey();
        entity.setProperty("name", "original");
        Key key = service.put(entity);
        try {
            Transaction tx = service.beginTransaction();
            PreparedQuery preparedQuery = service.prepare(new Query(TRANSACTION_TEST_ENTITY).setAncestor(parentKey));
            Entity entity2 = resultFetcher.fetchResult(preparedQuery);
            entity2.setProperty("name", "modified");
            service.put(tx, entity2);
            tx.rollback();

            Entity entity3 = service.get(key);
            assertEquals("original", entity3.getProperty("name"));
        } finally {
            service.delete(entity.getKey());
        }
    }

    @Test
    public void testNoIdKey() throws Exception {
        Entity entity = new Entity("NO_ID");
        Key key = service.put(entity);
        assertTrue(key.isComplete());
    }

    @Test
    public void testNested() throws Exception {
        assertNoActiveTransactions();

        Entity e1;
        Entity e2;

        Transaction t1 = service.beginTransaction();
        try {
            e1 = createTestEntity("DUMMY_a", System.currentTimeMillis());
            service.put(t1, e1);
            assertStoreDoesNotContain(e1);

            assertActiveTransactions(t1);

            Transaction t2 = service.beginTransaction();
            try {
                e2 = createTestEntity("DUMMY_b", 2);
                service.put(e2);

                assertActiveTransactions(t1, t2);
                assertStoreDoesNotContain(e2);
            } finally {
                t2.rollback();
            }

            assertActiveTransactions(t1);
//            assertStoreDoesNotContain(e2);  // should not be there due to rollback
        } finally {
            t1.commit();
        }

        assertStoreContains(e1);
        assertStoreDoesNotContain(e2);  // should not be there due to rollback
        assertNoActiveTransactions();
    }

    @Test
    public void testMultipleEntityGroupsInSingleTransactionAreNotAllowed() {
        Transaction tx = service.beginTransaction();
        try {
            Entity person = new Entity("Person", "tom");
            service.put(person);

            try {
                Entity photoNotAChild = new Entity("Photo");
                photoNotAChild.setProperty("photoUrl", "http://domain.com/path/to/photo.jpg");
                service.put(photoNotAChild);
                fail("put should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException ex) {
                // pass
            }
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testAncestorIsMandatoryInQueriesInsideTransaction() {
        Transaction tx = service.beginTransaction();
        try {

            service.prepare(new Query("test"));         // no tx, ancestor not necessary
            service.prepare(null, new Query("test"));   // no tx, ancestor not necessary
            service.prepare(tx, new Query("test").setAncestor(KeyFactory.createKey("some_kind", "some_id"))); // tx + ancestor

            try {
                service.prepare(tx, new Query("test")); // tx, but no ancestor
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // pass
            }
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testGetWithDifferentAncestorsInsideSameTransactionAreNotAllowed() {
        service.put(new Entity("foo", "1"));
        service.put(new Entity("foo", "2"));

        Transaction tx = service.beginTransaction();
        try {
            service.get(Arrays.asList(KeyFactory.createKey("foo", "1")));

            try {
                service.get(Arrays.asList(KeyFactory.createKey("foo", "2")));
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // pass
            }
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testMultipleQueriesWithSameAncestorInsideSameTransactionAreAllowed() {
        Transaction tx = service.beginTransaction();
        try {
            Key ancestor = KeyFactory.createKey("ancestor", "1");
            prepareQueryWithAncestor(tx, ancestor).asIterator().hasNext();
            prepareQueryWithAncestor(tx, ancestor).asIterator().hasNext();
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testQueriesWithDifferentAncestorsInsideSameTransactionThrowIllegalArgumentException() {
        Transaction tx = service.beginTransaction();
        try {
            Key someAncestor = KeyFactory.createKey("ancestor", "1");
            prepareQueryWithAncestor(tx, someAncestor).asIterator().hasNext();

            Key otherAncestor = KeyFactory.createKey("ancestor", "2");
            assertIAEWhenAccessingResult(prepareQueryWithAncestor(tx, otherAncestor));
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testQueriesWithDifferentAncestorsInsideSameTransactionNoUsage() {
        Transaction tx = service.beginTransaction();
        try {
            Key a1 = KeyFactory.createKey("ancestor", "1");
            prepareQueryWithAncestor(tx, a1).asIterator();

            Key a2 = KeyFactory.createKey("ancestor", "2");
            prepareQueryWithAncestor(tx, a2).asList(FetchOptions.Builder.withDefaults());

            Key a3 = KeyFactory.createKey("ancestor", "3");
            prepareQueryWithAncestor(tx, a3).asIterable();

            Key a4 = KeyFactory.createKey("ancestor", "4");
            prepareQueryWithAncestor(tx, a4).asQueryResultIterable();

            Key a5 = KeyFactory.createKey("ancestor", "5");
            prepareQueryWithAncestor(tx, a5).asQueryResultIterator();

            Key a6 = KeyFactory.createKey("ancestor", "6");
            prepareQueryWithAncestor(tx, a6).asQueryResultList(FetchOptions.Builder.withDefaults());
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testXGTransaction() throws Exception {

        final int N = 5; // max XG entity groups

        List<Key> keys = new ArrayList<Key>();
        for (int i = 0; i < N + 1; i++) {
            keys.add(service.put(new Entity("XG")));
        }

        boolean ok = false;
        Transaction tx = service.beginTransaction(TransactionOptions.Builder.withXG(true));
        try {
            for (int i = 0; i < N; i++) {
                service.get(keys.get(i));
            }

            try {
                service.get(keys.get(N));
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // pass
            }
            ok = true;
        } finally {
            if (ok) {
                tx.commit();
            } else {
                tx.rollback();
            }
        }
    }

    @Test
    public void testOperatingOnClosedTransaction() throws Exception {
        Transaction tx = service.beginTransaction();
        Entity entity = createTestEntity();
        service.put(tx, entity);
        tx.commit();

        try {
            service.get(tx, entity.getKey());
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ok) {
        }

        try {
            service.delete(tx, entity.getKey());
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ok) {
        }

        try {
            service.put(tx, entity);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ok) {
        }
    }

    /**
     * Test ancestor query in transaction.
     */
    @Test
    public void testAncestorQueryInTxn() throws Exception {
        Entity parentA = new Entity("parentA");
        Entity parentB = new Entity("parentB");
        Entity childA = new Entity("child", parentA.getKey());
        Entity childB = new Entity("child", parentB.getKey());

        service.beginTransaction();
        service.put(parentA);
        service.getCurrentTransaction().commit();

        service.beginTransaction();
        service.put(parentB);
        service.getCurrentTransaction().commit();

        service.beginTransaction();
        service.put(childA);
        service.getCurrentTransaction().commit();

        service.beginTransaction();
        service.put(childB);
        service.getCurrentTransaction().commit();
        // query on ancestor, only childA should be returned
        service.beginTransaction();
        Query query = new Query("child", parentA.getKey());
        Transaction tx = service.getCurrentTransaction();
        int numRows = service.prepare(tx, query)
            .countEntities(FetchOptions.Builder.withDefaults());
        tx.commit();
        assertEquals(1, numRows);
        service.beginTransaction();
        tx = service.getCurrentTransaction();
        Entity result = service.prepare(tx, query).asSingleEntity();
        assertEquals(childA.getKey(), result.getKey());
        tx.commit();
    }

    private PreparedQuery prepareQueryWithAncestor(Transaction tx, Key someAncestor) {
        return service.prepare(tx, new Query("foo").setAncestor(someAncestor));
    }

    private void assertNoActiveTransactions() {
        assertActiveTransactions();
    }

    protected void assertActiveTransactions(Transaction... txs) {
        Collection<Transaction> transactions = service.getActiveTransactions();
        assertNotNull(txs);
        Set<Transaction> expected = new HashSet<Transaction>(transactions);
        Set<Transaction> existing = new HashSet<Transaction>(Arrays.asList(txs));
        assertEquals(expected, existing);

        for (Transaction tx : txs) {
            assertTrue(tx.isActive());
        }
    }

    private interface ResultFetcher {
        Entity fetchResult(PreparedQuery preparedQuery);
    }
}

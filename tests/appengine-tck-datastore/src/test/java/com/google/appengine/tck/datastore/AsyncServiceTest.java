

package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * datastore Async test.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class AsyncServiceTest extends DatastoreTestBase {
    private static final String ASYNC_ENTITY = "asyncData";
    private AsyncDatastoreService asyncService = DatastoreServiceFactory.getAsyncDatastoreService();
    
    private Query simpleQuery(Key parentKey) {
        return new Query(ASYNC_ENTITY).setAncestor(parentKey);
    }

    private void assertTaskIsDoneAndNotCancelled(Future<?> future) {
        assertTrue("Task not Done, unable to complete test.", future.isDone());
        assertFalse("Task cancelled, unable to complete test.", future.isCancelled());
    }

    @Before
    public void clearData() {
        List<Key> elist = new ArrayList<Key>();
        Query query = new Query(ASYNC_ENTITY);
        for (Entity readRec : service.prepare(query).asIterable()) {
            elist.add(readRec.getKey());
        }
        service.delete(elist);
    }

    @Test
    public void testDataPut() throws Exception {
        Entity parent = createTestEntityWithUniqueMethodNameKey(ASYNC_ENTITY, "testDataPut");
        Key key = parent.getKey();
        Entity newRec = new Entity(ASYNC_ENTITY, key);
        newRec.setProperty("count", 0);
        newRec.setProperty("timestamp", new Date());
        Future<Key> future = asyncService.put(newRec);
        future.get();
        assertTaskIsDoneAndNotCancelled(future);
        assertEquals(1, service.prepare(simpleQuery(key)).countEntities(withDefaults()));
    }

    @Test
    public void testDataMultiplePut() throws Exception {
        Entity parent = createTestEntityWithUniqueMethodNameKey(ASYNC_ENTITY, "testDataMultiplePut");
        Key key = parent.getKey();

        final int recordCount = 10;
        List<Entity> entityList = new ArrayList<Entity>();
        for (int i = 0; i < recordCount; i++) {
            Entity newRec = new Entity(ASYNC_ENTITY, key);
            newRec.setProperty("count", i);
            newRec.setProperty("timestamp", new Date());
            entityList.add(newRec);
        }
        Future<List<Key>> eKeys = asyncService.put(entityList);
        eKeys.get();
        assertTaskIsDoneAndNotCancelled(eKeys);
        assertEquals(recordCount, service.prepare(simpleQuery(key)).countEntities(withDefaults()));
    }

    @Test
    public void testDataDelete() throws Exception {
        Entity parent = createTestEntityWithUniqueMethodNameKey(ASYNC_ENTITY, "testDataDelete");
        Key key = parent.getKey();
        Entity newRec = new Entity(ASYNC_ENTITY);
        newRec.setProperty("count", 0);
        newRec.setProperty("timestamp", new Date());
        Key ekey = service.put(newRec);
        Future<Void> future = asyncService.delete(ekey);
        future.get();
        assertTaskIsDoneAndNotCancelled(future);
        assertEquals(0, service.prepare(simpleQuery(key)).countEntities(withDefaults()));
    }

    @Test
    public void testMultipleDataDelete() throws Exception {
        Entity parent = createTestEntityWithUniqueMethodNameKey(ASYNC_ENTITY, "testMultipleDelete");
        Key key = parent.getKey();
        List<Entity> lisRec = new ArrayList<Entity>();
        final int recordCount = 10;
        for (int i = 0; i < recordCount; i++) {
            Entity newRec = new Entity(ASYNC_ENTITY, key);
            newRec.setProperty("count", i);
            newRec.setProperty("timestamp", new Date());
            lisRec.add(newRec);
        }
        List<Key> eKeys = service.put(lisRec);
        assertEquals("Entities not available to test delete.",
            recordCount, service.prepare(simpleQuery(key)).countEntities(withDefaults()));

        Future<Void> future = asyncService.delete(eKeys);
        future.get();
        assertTaskIsDoneAndNotCancelled(future);
        assertEquals(0, service.prepare(simpleQuery(key)).countEntities(withDefaults()));
    }

    @Test
    public void testDataGet() throws Exception {
        long randomLong = new Random().nextLong();
        Entity newRec = new Entity(ASYNC_ENTITY);
        newRec.setProperty("count", randomLong);
        newRec.setProperty("timestamp", new Date());
        Key ekey = service.put(newRec);
        Future<Entity> futureE = asyncService.get(ekey);
        Entity e = futureE.get();
        if (e != null) {
            assertEquals(randomLong, e.getProperty("count"));
        }
    }

    /*
     * Test AsyncService get method by checking size of Future.
     */
    @Test
    public void testMultipleDataGet() throws Exception {
        Entity parent = createTestEntityWithUniqueMethodNameKey(ASYNC_ENTITY, "testMultipleDataGet");
        Key key = parent.getKey();

        final int recordCount = 10;
        List<Entity> lisRec = new ArrayList<Entity>();
        for (int i = 0; i < recordCount; i++) {
            Entity newRec = new Entity(ASYNC_ENTITY, key);
            newRec.setProperty("count", i);
            newRec.setProperty("timestamp", new Date());
            lisRec.add(newRec);
        }
        List<Key> eKeys = service.put(lisRec);

        Future<Map<Key, Entity>> futureKeyEntity = asyncService.get(eKeys);
        Map<Key, Entity> es = futureKeyEntity.get();
        assertTaskIsDoneAndNotCancelled(futureKeyEntity);

        assertEquals(recordCount, es.size());

        asyncService.delete(eKeys);
    }

    @Test
    public void testDataAllocate() throws Exception {
        final long allocateNum = 5;

        // Range default namespace
        Future<KeyRange> futureRange = asyncService.allocateIds(ASYNC_ENTITY, allocateNum);
        KeyRange range = futureRange.get();
        assertTaskIsDoneAndNotCancelled(futureRange);

        Entity noParent = createTestEntity(ASYNC_ENTITY);
        assertEntityNotInRange(noParent, range);

        // Range with specified parent
        Entity parent = new Entity(ASYNC_ENTITY);
        parent.setProperty("name", "parent" + new Date());
        Key parentKey = service.put(parent);
        Future<KeyRange> futureRange2 = asyncService.allocateIds(parentKey, ASYNC_ENTITY, allocateNum);
        KeyRange range2 = futureRange2.get();
        assertTaskIsDoneAndNotCancelled(futureRange2);

        Entity noParent2 = createTestEntity(ASYNC_ENTITY, parentKey);
        assertEntityNotInRange(noParent2, range2);

        // In Range entity should have same parent
        Entity child = new Entity(range2.getStart());
        child.setProperty("name", "second" + new Date());
        Key childKey = service.put(child);
        // child with allocated key should have correct parent.
        assertEquals(parentKey, childKey.getParent());
    }

    @Test
    public void testFutureCancel() {
        clearData();
        AsyncDatastoreService asyncService = DatastoreServiceFactory.getAsyncDatastoreService();
        Entity newRec = new Entity(ASYNC_ENTITY);
        newRec.setProperty("count", -1);
        newRec.setProperty("timestamp", new Date());
        Future<Key> future = asyncService.put(newRec);
        future.cancel(true);

        // The actual call may already succeeded, so just verify that cancel has been called.
        assertTrue(future.isCancelled());
    }

    @Test
    public void testInTrans() throws Exception {
        clearData();
        Entity newRec = new Entity(ASYNC_ENTITY);
        newRec.setProperty("timestamp", new Date());
        Transaction trans = asyncService.beginTransaction().get();
        Future<Key> firstE = asyncService.put(trans, newRec);
        trans.rollback();
        assertEquals(0, service.prepare(new Query(ASYNC_ENTITY)).countEntities(withDefaults()));

        // Add a parent
        newRec = new Entity(ASYNC_ENTITY);
        newRec.setProperty("count", 0);
        newRec.setProperty("timestamp", new Date());
        Key parent = service.put(newRec);
        // Add children
        List<Entity> lisRec = new ArrayList<Entity>();
        for (int i = 0; i < 10; i++) {
            newRec = new Entity(ASYNC_ENTITY, parent);
            newRec.setProperty("count", i + 1);
            newRec.setProperty("timestamp", new Date());
            lisRec.add(newRec);
        }
        trans = asyncService.beginTransaction().get();
        Future<List<Key>> eKeys = asyncService.put(trans, lisRec);
        trans.commit();
        List<Key> realKey = eKeys.get();
        Query q = new Query(ASYNC_ENTITY).setAncestor(parent);
        if (eKeys.isCancelled()) {
            assertEquals(0, realKey.size());
            assertEquals(1, service.prepare(q).countEntities(withDefaults()));
        } else {
            assertEquals(10, realKey.size());
            assertEquals(11, service.prepare(q).countEntities(withDefaults()));
        }
    }
}

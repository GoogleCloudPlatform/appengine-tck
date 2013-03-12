

package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * datastore Async test.
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class AsyncServiceTest extends AbstractDatastoreTest {
  private static final String kindName = "asyncData";
  private long allocateNum = 5;
  private FetchOptions fo = FetchOptions.Builder.withDefaults();
  private AsyncDatastoreService asyncService = DatastoreServiceFactory.getAsyncDatastoreService();

  @Before
  public void clearData() {
    List<Key> elist = new ArrayList<Key>();
    Query query = new Query(kindName);
    for (Entity readRec : datastoreService.prepare(query).asIterable()) {
      elist.add(readRec.getKey());
    }
    datastoreService.delete(elist);
  }
  
  @Test
  public void testDataPut() throws Exception {
    Entity newRec = new Entity(kindName);
    newRec.setProperty("count", 0);
    newRec.setProperty("timestamp", new Date());
    Future<Key> firstE = asyncService.put(newRec);
    firstE.get();
    if (firstE.isCancelled()) {
      assertEquals(0, datastoreService.prepare(new Query(kindName)).countEntities(fo));
    } else {
      assertEquals(1, datastoreService.prepare(new Query(kindName)).countEntities(fo));
    }
  }
  
  @Test
  public void testDataMultiplePut() throws Exception {
    List<Entity> lisRec = new ArrayList<Entity>();
    for (int i = 0; i < 10; i++) {
      Entity newRec = new Entity(kindName);
      newRec = new Entity(kindName);
      newRec.setProperty("count", i);
      newRec.setProperty("timestamp", new Date());
      lisRec.add(newRec);
    }
    Future<List<Key>> eKeys = asyncService.put(lisRec);
    eKeys.get();
    if (eKeys.isCancelled()) {
      assertEquals(0, datastoreService.prepare(new Query(kindName)).countEntities(fo));
    } else {
      assertEquals(10, datastoreService.prepare(new Query(kindName)).countEntities(fo));
    }
  }
  
  @Test
  public void testDataDelete() throws Exception {
    Entity newRec = new Entity(kindName);
    newRec.setProperty("count", 0);
    newRec.setProperty("timestamp", new Date());
    Key ekey = datastoreService.put(newRec);
    Future<Void> future = asyncService.delete(ekey);
    future.get();
    if (future.isCancelled()) {
      assertEquals(1, datastoreService.prepare(new Query(kindName)).countEntities(fo));
    } else {
      assertEquals(0, datastoreService.prepare(new Query(kindName)).countEntities(fo));
    }
  }
  
  @Test
  public void testMultipleDataDelete() throws Exception {
    List<Entity> lisRec = new ArrayList<Entity>();
    for (int i = 0; i < 10; i++) {
      Entity newRec = new Entity(kindName);
      newRec = new Entity(kindName);
      newRec.setProperty("count", i);
      newRec.setProperty("timestamp", new Date());
      lisRec.add(newRec);
    }
    List<Key> eKeys = datastoreService.put(lisRec);
    Future<Void> future = asyncService.delete(eKeys);
    future.get();
    if (future.isCancelled()) {
      assertEquals(10, datastoreService.prepare(new Query(kindName)).countEntities(fo));
    } else {
      assertEquals(0, datastoreService.prepare(new Query(kindName)).countEntities(fo));
    }
  }
  
  @Test
  public void testDataGet() throws Exception {
    long randomLong = new Random().nextLong();
    Entity newRec = new Entity(kindName);
    newRec.setProperty("count", randomLong);
    newRec.setProperty("timestamp", new Date());
    Key ekey = datastoreService.put(newRec);
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
    List<Entity> lisRec = new ArrayList<Entity>();
    for (int i = 0; i < 10; i++) {
      Entity newRec = new Entity(kindName);
      newRec = new Entity(kindName);
      newRec.setProperty("count", i);
      newRec.setProperty("timestamp", new Date());
      lisRec.add(newRec);
    }
    List<Key> eKeys = datastoreService.put(lisRec);
    Future<Map<Key, Entity>> futureEs = asyncService.get(eKeys);
    Map<Key, Entity> es = futureEs.get();
    if (es != null) {
      assertEquals(10, es.size());
    }
  }
  
  @Test
  public void testDataAllocate()  throws Exception {
    Future<KeyRange> futureRange = asyncService.allocateIds(kindName, allocateNum);
    KeyRange range = futureRange.get();
    if (!futureRange.isCancelled()) {
      check(kindName, range);
    }
    
    Entity parent = new Entity(kindName);
    parent.setProperty("name", "parent" + new Date());
    Key pKey = datastoreService.put(parent);
    futureRange = asyncService.allocateIds(pKey, kindName, allocateNum);
    range = futureRange.get();
    if (!futureRange.isCancelled()) {
      check(kindName, range);
    }
    Entity child = new Entity(range.getStart());
    child.setProperty("name", "second" + new Date());
    Key ckey = datastoreService.put(child);
    // child with allocated key should have correct parent.
    assertEquals(pKey, ckey.getParent());
  }

  private void check(String kind, KeyRange range) {
    Entity entity = new Entity(kind);
    entity.setProperty("name", "first" + new Date());
    Key key = datastoreService.put(entity);
    // allocated key should not be re-used.
    assertTrue(key.getId() > range.getEnd().getId() || key.getId() < range.getStart().getId());
  }
  
  @Test
  public void testFutureCancel() {
    clearData();
    AsyncDatastoreService asyncService = DatastoreServiceFactory.getAsyncDatastoreService();
    Entity newRec = new Entity(kindName);
    newRec.setProperty("count", -1);
    newRec.setProperty("timestamp", new Date());
    Future<Key> firstE = asyncService.put(newRec);
    firstE.cancel(true);  
/* Do not run this test since there is no good way to check if the cancel is success now.
 *  Max's notes: Successfully canceling an RPC does not necessarily mean that the datastore
 *  call didn't succeed. In short, a successful cancel() doesn't really tell you anything 
 *  about what happened on the server side.
    if (firstE.cancel(true)) {
      assertEquals(0, datastoreService.prepare(new Query(kindName)).countEntities(fo));
    }
*/
    
    clearData();
    List<Entity> lisRec = new ArrayList<Entity>();
    for (int i = 0; i < 10; i++) {
      newRec = new Entity(kindName);
      newRec.setProperty("count", i);
      newRec.setProperty("timestamp", new Date());
      lisRec.add(newRec);
    }
    Future<List<Key>> eKeys = asyncService.put(lisRec);
    eKeys.cancel(true);
  }
  
  @Test
  public void testInTrans() throws Exception {
    clearData();
    Entity newRec = new Entity(kindName);
    newRec.setProperty("timestamp", new Date());
    Transaction trans = asyncService.beginTransaction().get();
    Future<Key> firstE = asyncService.put(trans, newRec);
    trans.rollback();
    assertEquals(0, datastoreService.prepare(new Query(kindName)).countEntities(fo));

    // Add a parent
    newRec = new Entity(kindName);
    newRec.setProperty("count", 0);
    newRec.setProperty("timestamp", new Date());
    Key parent = datastoreService.put(newRec);
    // Add children
    List<Entity> lisRec = new ArrayList<Entity>();
    for (int i = 0; i < 10; i++) {
      newRec = new Entity(kindName, parent);
      newRec.setProperty("count", i + 1);
      newRec.setProperty("timestamp", new Date());
      lisRec.add(newRec);
    }
    trans = asyncService.beginTransaction().get();
    Future<List<Key>> eKeys = asyncService.put(trans, lisRec);
    trans.commit();
    List<Key> realKey = eKeys.get();
    Query q = new Query(kindName).setAncestor(parent);
    if (eKeys.isCancelled()) {
      assertEquals(0, realKey.size());
      assertEquals(1, datastoreService.prepare(q).countEntities(fo));
    } else {
      assertEquals(10, realKey.size());
      assertEquals(11, datastoreService.prepare(q).countEntities(fo));
    }
  }
}

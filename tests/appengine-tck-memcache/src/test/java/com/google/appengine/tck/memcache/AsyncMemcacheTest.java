// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.tck.memcache;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.CasValues;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Stats;
import com.google.appengine.api.utils.SystemProperty;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Tests Async Memcache.
 *
 * @author hchen@google.com (Hannah Chen)
 * @author smithd@google.com (Dave Smith)
 */
@RunWith(Arquillian.class)
public class AsyncMemcacheTest extends CacheTestBase {
  private MemcacheService memcache;
  private AsyncMemcacheService asyncMemcache;
  private int overhead = 1024;   // space for key value
  private String str1mb = getBigString(1024 * 1024 - overhead);
  private String str1K = getBigString(1024);

  @Before
  public void setUp() {
    memcache = MemcacheServiceFactory.getMemcacheService();
    asyncMemcache = MemcacheServiceFactory.getAsyncMemcacheService();
    memcache.clearAll();
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Tests single put/get.
   */
  @Test
  public void testPutAndGet_basic() {
    verifyAsyncGet(KEY1, VALUE1);
  }

  /**
   * Tests different data type put/get.
   */
  @Test
  public void testPutAndGet_types() {
    for (Object key : TEST_DATA) {
      assertNull(memcache.get(key));
      for (Object value : TEST_DATA) {
        verifyAsyncGet(key, value);
      }
    }
  }

  @Test
  public void testGet_array1() {
    Future<Void> future = asyncMemcache.put(KEY1, ARRAY1);
    waitOnFuture(future);
    assertArrayEquals(ARRAY1, (int[]) memcache.get(KEY1));
  }

  @Test
  public void testGet_array2() {
    Future<Void> future = asyncMemcache.put(KEY1, ARRAY2);
    waitOnFuture(future);
    assertArrayEquals(ARRAY2, (Object[]) memcache.get(KEY1));
  }

  @Test
  public void testDelete() {
    memcache.put(KEY1, VALUE1);
    Future<Boolean> future = asyncMemcache.delete(KEY1);
    waitOnFuture(future);
    assertNull("key1 should hold null", memcache.get(KEY1));
  }

  @Test
  public void testPutAll() {
    Future<Void> future = asyncMemcache.putAll(getSmallBatchData()); // getBatchDat());
    waitOnFuture(future);

    String value1 = (String) memcache.get("bkey1");
    assertNotNull("value1 should be non-null", value1);
    assertTrue("bkey1 should retain its value", str1K.equals(value1));

    String value30 = (String) memcache.get("bkey30");
    assertNotNull("value30 should be non-null", value30);
    assertTrue("bkey30 should retain its value", str1K.equals(value30));
  }

  @Test
  public void testDeleteAll() {
    Map<Object, Object> cacheDat = getSmallBatchData(); // getBatchDat();
    memcache.putAll(cacheDat);
    Future<Set<Object>> future = asyncMemcache.deleteAll(cacheDat.keySet());
    waitOnFuture(future);
    assertNull("bkey1 should have null data", memcache.get("bkey1"));
    assertNull("bkey30 should have null data", memcache.get("bkey30"));
  }

  /**
   * Tests cancel for dev_appserver.
   */
  @Test
  public void testCancel_devAppServer() throws Exception {
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
      verifyCancel();
    }
  }

  private void verifyCancel() {
    Future<Void> future = asyncMemcache.putAll(getBatchDat());
    boolean cancelled = future.cancel(true);
    // N.B., there's no guarantee that the cancel succeeded.
    if (cancelled) {
      assertNull("bkey1 should have null value after successful cancel", memcache.get("bkey1"));
      assertNull("bkey30 should have null value after successful cancel", memcache.get("bkey30"));
    } else {
      Object cached = memcache.get("bkey1");
      assertNotNull("bkey1 should have cached data", cached);
      assertEquals(str1mb, cached);
      cached = memcache.get("bkey30");
      assertNotNull("bkey30 should have cached data", cached);
      assertEquals(str1mb, cached);
    }
    waitOnFuture(future);
  }

  /**
   * Tests increments.
   * @throws InterruptedException
   */
  @Test
  public void testBatchIncr() throws InterruptedException {
    Map<Object, Object> cacheDat = getBatchIntDat();
    memcache.putAll(cacheDat);
    Future<Map<Object, Long>> future = asyncMemcache.incrementAll(cacheDat.keySet(), 10);
    waitOnFuture(future);
    // Sample the key/values we set
    assertEquals("expected ikey1 to be 11", 11, memcache.get("ikey1"));
    assertEquals("expected ikey30 to be 40", 40, memcache.get("ikey30"));
  }

  /**
   * Tests GetIdentifiables.
   * @throws InterruptedException
   * @throws ExecutionException
   */
  @Test
  public void testGetIdentifiables() throws InterruptedException, ExecutionException {
    memcache.put(KEY1, VALUE1);

    Future<IdentifiableValue> future = asyncMemcache.getIdentifiable(KEY1);
    waitOnFuture(future);
    assertEquals(VALUE1, future.get().getValue());

    // batch versions
    for (Object key : TEST_DATA) {
      asyncMemcache.put(key, key);
    }
    Future<Map<Object, IdentifiableValue>> futureMap =
                                   asyncMemcache.getIdentifiables(Arrays.asList(TEST_DATA));
    waitOnFuture(futureMap);
    Map<Object, IdentifiableValue> ivMap = futureMap.get();
    for (Object key : ivMap.keySet()) {
      assertEquals(key, ivMap.get(key).getValue());
    }
  }

  /**
   * Tests PutIfUntouched. 
   * @throws InterruptedException 
   * @throws ExecutionException 
   */
  @Test
  public void testPutIfUntouched() throws InterruptedException, ExecutionException {
    memcache.put(KEY1, VALUE1);
    IdentifiableValue iv = memcache.getIdentifiable(KEY1);
    Future<Boolean> future = asyncMemcache.putIfUntouched(KEY1, iv, "new" + VALUE1);
    waitOnFuture(future);
    assertEquals(true, future.get());
    assertEquals("new" + VALUE1, memcache.get(KEY1));

    future = asyncMemcache.putIfUntouched(KEY1, iv, "another" + VALUE1);
    waitOnFuture(future);
    assertEquals(false, future.get());

    // batch versions
    Object[] testDat = {1, VALUE1};
    Map<Object, Object> inputDat = new HashMap<Object, Object> ();
    for (Object key : testDat) {
      inputDat.put(key, key);
    }
    memcache.putAll(inputDat);
    Map<Object, CasValues> updateDat = new HashMap<Object, CasValues> ();
    for (Object key : testDat) {
      updateDat.put(key, new CasValues(memcache.getIdentifiable(key), "new value"));
    }
    Future<Set<Object>> futureSet = asyncMemcache.putIfUntouched(updateDat);
    waitOnFuture(futureSet);
    assertEquals(2, futureSet.get().size());
    assertEquals("new value", memcache.get(testDat[0]));

    futureSet = asyncMemcache.putIfUntouched(updateDat);
    waitOnFuture(futureSet);
    assertEquals(0, futureSet.get().size());
  }

  private static String getBigString(int len) {
    char[] chars = new char[len];
    for (int i = 0; i < len; i++) {
      chars[i] = 'x';
    }
    return new String(chars);
  }

  private Map<Object, Object> getBatchDat() {
    Map<Object, Object> cacheDat = new HashMap<Object, Object>();
    for (int i = 0; i < 31; i++) {
      cacheDat.put("bkey" + i, str1mb);
    }
    return cacheDat;
  }

  private Map<Object, Object> getSmallBatchData() {
    Map<Object, Object> cacheDat = new HashMap<Object, Object>();
    for (int i = 0; i < 31; i++) {
      cacheDat.put("bkey" + i, str1K);
    }
    return cacheDat;
  }

  private Map<Object, Object> getBatchIntDat() {
    Map<Object, Object> cacheDat = new HashMap<Object, Object> ();
    for (int i = 0; i < 31; i++) {
      cacheDat.put("ikey" + i, i);
    }
    return cacheDat;
  }

  private void verifyAsyncGet(Object key, Object value) {
    Future<Void> putFuture = asyncMemcache.put(key, value);
    waitOnFuture(putFuture);
    Future<Object> getFuture = asyncMemcache.get(key);
    waitOnFuture(getFuture);
    assertFalse("future shouldn't be cancelled", getFuture.isCancelled());
    assertEquals(value, memcache.get(key));
  }

  private void waitOnFuture(Future<?> future) {
    while (!future.isDone()) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        // Nothing to do but try again
      }
    }
  }

  // A debugging aid to show cache stats along with a failure message
  @SuppressWarnings("unused")
  private String failure(String msg) {
    Stats statistics = memcache.getStatistics();
    StringBuilder sb = new StringBuilder();
    sb
      .append(msg)
      .append(" (")
      .append(statistics.getItemCount())
      .append("/")
      .append(statistics.getHitCount())
      .append("/")
      .append(statistics.getMissCount())
      .append(")");
    return sb.toString();
  }
}

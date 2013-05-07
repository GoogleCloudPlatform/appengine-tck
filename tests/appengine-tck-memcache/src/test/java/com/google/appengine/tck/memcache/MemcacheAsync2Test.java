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

package com.google.appengine.tck.memcache;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.CasValues;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Stats;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests Async Memcache.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class MemcacheAsync2Test extends CacheTestBase {
    private MemcacheService memcache;
    private AsyncMemcacheService asyncMemcache;

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
    public void testPutAndGetBasic() {
        verifyAsyncGet(KEY1, STR_VALUE);
    }

    /**
     * Tests different data type put/get.
     */
    @Test
    public void testPutAndGetTypes() {
        for (Object key : TEST_DATA) {
            assertNull(memcache.get(key));
            for (Object value : TEST_DATA) {
                verifyAsyncGet(key, value);
            }
        }
    }

    @Test
    public void testGetArray1() {
        Future<Void> future = asyncMemcache.put(KEY1, ARRAY1);
        waitOnFuture(future);
        assertArrayEquals(ARRAY1, (int[]) memcache.get(KEY1));
    }

    @Test
    public void testGetArray2() {
        Future<Void> future = asyncMemcache.put(KEY1, ARRAY2);
        waitOnFuture(future);
        assertArrayEquals(ARRAY2, (Object[]) memcache.get(KEY1));
    }

    @Test
    public void testDelete() {
        memcache.put(KEY1, STR_VALUE);
        Future<Boolean> future = asyncMemcache.delete(KEY1);
        waitOnFuture(future);
        assertNull("key1 should hold null", memcache.get(KEY1));
    }

    @Test
    public void testDeleteNoReAddTime() {
        String key = createTimeStampKey("testDeleteNoReAddTime");
        memcache.put(key, STR_VALUE);
        assertNotNull(memcache.get(key));

        // delete and do not allow re-add for another 10 seconds.
        Future<Boolean> future = asyncMemcache.delete(key, 10 * 1000);
        waitOnFuture(future);
        assertNull("key should be null", memcache.get(KEY1));

        // re-add should fail since within 10 seconds.
        assertFalse(memcache.put(key, STR_VALUE, null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT));
        assertNull("key should be null because of policy.", memcache.get(KEY1));
    }

    @Test
    public void testPutAll() {
        Map<Object, Object> data = createSmallBatchData();
        Future<Void> future = asyncMemcache.putAll(data);
        waitOnFuture(future);

        Map<Object, Object> fetched = memcache.getAll(data.keySet());
        assertEquals("All the keys inserted should exist.", data, fetched);
    }

    @Test
    public void testDeleteAll() {
        Map<Object, Object> data = createSmallBatchData();
        memcache.putAll(data);

        Future<Set<Object>> future = asyncMemcache.deleteAll(data.keySet());
        Set<Object> deletedKeys = waitOnFuture(future);

        assertEquals(data.keySet(), deletedKeys);
    }

    @Test
    public void testDeleteAllNoReAddTime() {
        Map<Object, Object> cacheDat = createSmallBatchData();
        memcache.putAll(cacheDat);
        Future<Set<Object>> future = asyncMemcache.deleteAll(cacheDat.keySet(), 10 * 1000);
        waitOnFuture(future);
        Map<Object, Object> retValues = memcache.getAll(cacheDat.keySet());
        assertEquals("All keys should be deleted.", 0, retValues.size());

        memcache.putAll(cacheDat, null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
        Map<Object, Object> retValuesAfter = memcache.getAll(cacheDat.keySet());
        assertEquals("No keys should be added because of policy.", 0, retValuesAfter.size());
    }

    @Test
    public void testCancel() {
        Map<Object, Object> data = createSmallBatchData();
        Future<Void> future = asyncMemcache.putAll(data);
        boolean cancelled = future.cancel(true);

        // There's no guarantee that the cancel succeeds on the backend, so just verify that the
        // fundamental future api works.
        assertTrue(future.isDone());  // Should always be true once cancel() returns.

        if (cancelled) {
            assertTrue(future.isCancelled());
        }
    }

    @Test
    public void testIncrementAll() {
        Map<Object, Long> map = createLongBatchData();
        memcache.putAll(map);

        long delta = 10;
        Future<Map<Object, Long>> future = asyncMemcache.incrementAll(map.keySet(), delta);
        Map<Object, Long> returned = waitOnFuture(future);

        Map<Object, Object> expected = new HashMap<Object, Object>();
        for (Map.Entry<Object, Long> entry : map.entrySet()) {
            expected.put(entry.getKey(), entry.getValue() + delta);
        }
        assertEquals(expected, returned);

        Map<Object, Object> fetched = memcache.getAll(map.keySet());
        assertEquals(expected, fetched);
    }

    @Test
    public void testIncrementAllInitValue() {
        Map<Object, Long> map = createLongBatchData();
        memcache.putAll(map);

        long delta = 10;
        Long initVal = -111L;
        Map<Object, Long> expected = copyMapIncrementLongValue(map, delta);

        // Add the key which doesn't exist yet.
        String nonExistentKey = "testIncrementAllInitValue-" + System.currentTimeMillis();
        expected.put(nonExistentKey, initVal + delta);

        Future<Map<Object, Long>> future = asyncMemcache.incrementAll(expected.keySet(), delta, initVal);
        Map<Object, Long> returned = waitOnFuture(future);
        assertEquals(expected, returned);

        Map<Object, Object> fetched = memcache.getAll(expected.keySet());
        assertEquals(expected, fetched);
    }

    @Test
    public void testIncrementAllFromMap() {
        Map<Object, Long> map = createLongBatchData();
        memcache.putAll(map);

        Map<Object, Long> incMap = createRandomIncrementMap(map);

        Future<Map<Object, Long>> future = asyncMemcache.incrementAll(incMap);
        Map<Object, Long> returned = waitOnFuture(future);

        Map<Object, Long> expected = createMapFromIncrementMap(map, incMap);
        assertEquals(expected, returned);

        Map<Object, Object> fetched = memcache.getAll(map.keySet());
        assertEquals(expected, fetched);
    }

    @Test
    public void testIncrementAllFromMapInitValue() {
        Map<Object, Long> map = createLongBatchData();
        memcache.putAll(map);

        Map<Object, Long> incMap = createRandomIncrementMap(map);

        Map<Object, Long> expected = createMapFromIncrementMap(map, incMap);

        Long initVal = -111L;
        // Add the key which doesn't exist yet.
        String nonExistentKey = "testIncrementAllFromMapInitValue-" + System.currentTimeMillis();
        incMap.put(nonExistentKey, 123L);
        expected.put(nonExistentKey, initVal + 123L);

        Future<Map<Object, Long>> future = asyncMemcache.incrementAll(incMap, initVal);
        Map<Object, Long> returned = waitOnFuture(future);
        assertEquals(expected, returned);

        Map<Object, Object> fetched = memcache.getAll(incMap.keySet());
        assertEquals(expected, fetched);
    }

    @Test
    public void testGetIdentifiables() {
        memcache.put(KEY1, STR_VALUE);

        Future<IdentifiableValue> future = asyncMemcache.getIdentifiable(KEY1);
        IdentifiableValue identVal = waitOnFuture(future);
        assertEquals(STR_VALUE, identVal.getValue());

        // batch versions
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Object key : TEST_DATA) {
            map.put(key, key);
        }
        waitOnFuture(asyncMemcache.putAll(map));

        Future<Map<Object, IdentifiableValue>> futureMap = asyncMemcache.getIdentifiables(Arrays.asList(TEST_DATA));
        Map<Object, IdentifiableValue> ivMap = waitOnFuture(futureMap);
        for (Object key : ivMap.keySet()) {
            assertEquals(key, ivMap.get(key).getValue());
        }
    }

    @Test
    public void testPutIfUntouched() {
        final String TS_KEY = createTimeStampKey("testPutIfUntouched");

        memcache.put(TS_KEY, STR_VALUE);
        IdentifiableValue oldOriginalIdValue = memcache.getIdentifiable(TS_KEY);
        final String NEW_VALUE = "new-" + STR_VALUE;
        Future<Boolean> future;
        Boolean valueWasStored;

        // Store NEW_VALUE if no other value stored since oldOriginalIdValue was retrieved.
        // memcache.get() has not been called, so this put should succeed.
        future = asyncMemcache.putIfUntouched(TS_KEY, oldOriginalIdValue, NEW_VALUE);
        valueWasStored = waitOnFuture(future);
        assertEquals(true, valueWasStored);
        assertEquals(NEW_VALUE, memcache.get(TS_KEY));

        // NEW_VALUE was stored so this put should not happen.
        final String ANOTHER_VALUE = "another-" + STR_VALUE;
        future = asyncMemcache.putIfUntouched(TS_KEY, oldOriginalIdValue, ANOTHER_VALUE);
        valueWasStored = waitOnFuture(future);
        assertEquals(false, valueWasStored);

        assertNotSame(ANOTHER_VALUE, memcache.get(TS_KEY));
        assertEquals(NEW_VALUE, memcache.get(TS_KEY));
    }

    @Test
    public void testPutIfUntouchedMap() {
        // batch versions
        Object[] testDat = {1, STR_VALUE};
        Map<Object, Object> inputDat = new HashMap<Object, Object>();
        for (Object key : testDat) {
            inputDat.put(key, key);
        }
        memcache.putAll(inputDat);
        Set<Object> set;

        Map<Object, CasValues> updateDat = new HashMap<Object, CasValues>();
        for (Object key : testDat) {
            updateDat.put(key, new CasValues(memcache.getIdentifiable(key), "new value"));
        }
        Future<Set<Object>> futureSet = asyncMemcache.putIfUntouched(updateDat);
        set = waitOnFuture(futureSet);
        assertEquals(2, set.size());
        assertEquals("new value", memcache.get(testDat[0]));

        futureSet = asyncMemcache.putIfUntouched(updateDat);
        set = waitOnFuture(futureSet);
        assertEquals(0, set.size());
    }

    @Test
    public void testPutIfUntouchedExpire() {
        final String TS_KEY = createTimeStampKey("testPutIfUntouched");

        memcache.put(TS_KEY, STR_VALUE);
        IdentifiableValue oldOriginalIdValue = memcache.getIdentifiable(TS_KEY);
        final String NEW_VALUE = "new-" + STR_VALUE;
        Future<Boolean> future;
        Boolean valueWasStored;

        // Store NEW_VALUE if no other value stored since oldOriginalIdValue was retrieved.
        // memcache.get() has not been called, so this put should succeed.
        future = asyncMemcache.putIfUntouched(TS_KEY, oldOriginalIdValue, NEW_VALUE, Expiration.byDeltaSeconds(1));
        valueWasStored = waitOnFuture(future);
        assertEquals(true, valueWasStored);
        assertEquals(NEW_VALUE, memcache.get(TS_KEY));

        // Value should not be stored after expiration period.
        sync(3000);
        assertNull(memcache.get(TS_KEY));
    }

    @Test
    public void testPutIfUntouchedMapExpire() {
        // batch versions
        Object[] testDat = {1, STR_VALUE};
        Map<Object, Object> inputDat = new HashMap<Object, Object>();
        for (Object key : testDat) {
            inputDat.put(key, key);
        }
        memcache.putAll(inputDat);
        Set<Object> set;

        Map<Object, CasValues> updateDat = new HashMap<Object, CasValues>();
        for (Object key : testDat) {
            updateDat.put(key, new CasValues(memcache.getIdentifiable(key), "new value", Expiration.byDeltaSeconds(60 * 60)));
        }
        Future<Set<Object>> futureSet = asyncMemcache.putIfUntouched(updateDat, Expiration.byDeltaMillis(1000));
        set = waitOnFuture(futureSet);
        assertEquals(2, set.size());
        assertEquals("new value", memcache.get(testDat[0]));

        sync(3000);
        assertNull(memcache.get(testDat));
    }

    @Test
    public void testStatistics() {
        Future<Stats> future = asyncMemcache.getStatistics();
        Stats stats = waitOnFuture(future);
        assertNotNull("Stats should never be null.", stats);

        // Only verify that all stats are non-negative.
        assertTrue(stats.getBytesReturnedForHits() > -1L);
        assertTrue(stats.getHitCount() > -1L);
        assertTrue(stats.getItemCount() > -1L);
        assertTrue(stats.getMaxTimeWithoutAccess() > -1);
        assertTrue(stats.getMissCount() > -1L);
        assertTrue(stats.getTotalItemBytes() > -1L);
    }

    private void verifyAsyncGet(Object key, Object value) {
        Future<Void> putFuture = asyncMemcache.put(key, value);
        waitOnFuture(putFuture);
        Future<Object> getFuture = asyncMemcache.get(key);
        waitOnFuture(getFuture);
        assertFalse("future shouldn't be cancelled", getFuture.isCancelled());
        assertEquals(value, memcache.get(key));
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

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

package com.google.appengine.tck.memcache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class MemcacheTest extends CacheTestBase {

    private MemcacheService memcache;

    @Before
    public void setUp() {
        memcache = MemcacheServiceFactory.getMemcacheService();
        memcache.clearAll();
    }

    @After
    public void tearDown() {
        memcache.clearAll();
    }

    @Test
    public void testClearAll() {
        memcache.put("key1", "value1");
        memcache.put("key2", "value2");
        memcache.put("key3", "value3");
        memcache.clearAll();
        assertFalse(memcache.contains("key1"));
        assertFalse(memcache.contains("key2"));
        assertFalse(memcache.contains("key3"));
    }

    @Test
    public void testPut() {
        memcache.put("key", "value");
        assertTrue(memcache.contains("key"));
        assertEquals("value", memcache.get("key"));
    }

    @Test
    public void testPutReplaceOnlyIfPresent() {
        assertFalse(memcache.contains("key"));
        memcache.put("key", "value", null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);
        assertFalse(memcache.contains("key"));
    }

    @Test
    public void testPutAddOnlyIfNotPresent() {
        memcache.put("key", "firstValue");
        memcache.put("key", "secondValue", null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
        assertEquals("firstValue", memcache.get("key"));
    }

    @Test
    public void testPutAll() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        memcache.putAll(map);

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            assertEquals(entry.getValue(), memcache.get(entry.getKey()));
        }
    }

    @Test
    public void testPutAllReplaceOnlyIfPresent() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        memcache.putAll(map, null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            assertFalse(memcache.contains(entry.getKey()));
        }
    }

    @Test
    public void testPutAllAddOnlyIfNotPresent() {
        HashMap<Object, Object> firstValues = new HashMap<Object, Object>();
        firstValues.put("key1", "firstValue1");
        firstValues.put("key2", "firstValue2");

        HashMap<Object, Object> secondValues = new HashMap<Object, Object>();
        secondValues.put("key1", "secondValue1");
        secondValues.put("key2", "secondValue2");

        memcache.putAll(firstValues);
        memcache.putAll(secondValues, null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);

        for (Map.Entry<Object, Object> entry : firstValues.entrySet()) {
            assertEquals(entry.getValue(), memcache.get(entry.getKey()));
        }
    }

    @Test
    public void testGetIdentifiable() {
        memcache.put("key", "value");
        MemcacheService.IdentifiableValue identifiable = memcache.getIdentifiable("key");
        assertEquals("value", identifiable.getValue());
    }

    @Test
    public void testGetIdentifiables() {
        memcache.put("key1", "value1");
        memcache.put("key2", "value2");
        Map<String, MemcacheService.IdentifiableValue> identifiables = memcache.getIdentifiables(Arrays.asList("key1", "key2"));

        assertEquals(2, identifiables.size());

        assertNotNull(identifiables.get("key1"));
        assertEquals("value1", identifiables.get("key1").getValue());

        assertNotNull(identifiables.get("key2"));
        assertEquals("value2", identifiables.get("key2").getValue());
    }

    @Test
    public void testPutIfUntouched() {
        memcache.put("key", "value");

        MemcacheService.IdentifiableValue identifiable = memcache.getIdentifiable("key");

        boolean valueWasStored = memcache.putIfUntouched("key", identifiable, "newValue");
        assertTrue(valueWasStored);
        assertEquals("newValue", memcache.get("key"));

        boolean valueWasStored2 = memcache.putIfUntouched("key", identifiable, "newestValue");
        assertFalse(valueWasStored2);
        assertEquals("newValue", memcache.get("key"));
    }

    @Test
    public void testPutIfUntouchedMulti() {
        memcache.put("key1", "value1");
        memcache.put("key2", "value2");

        MemcacheService.IdentifiableValue identifiable1 = memcache.getIdentifiable("key1");
        MemcacheService.IdentifiableValue identifiable2 = memcache.getIdentifiable("key2");


        HashMap<Object, MemcacheService.CasValues> map = new HashMap<Object, MemcacheService.CasValues>();
        map.put("key1", new MemcacheService.CasValues(identifiable1, "newValue1"));
        map.put("key2", new MemcacheService.CasValues(identifiable2, "newValue2"));

        Set<Object> storedKeys = memcache.putIfUntouched(map);
        assertEquals(2, storedKeys.size());
        assertTrue(storedKeys.contains("key1"));
        assertTrue(storedKeys.contains("key2"));
        assertEquals("newValue1", memcache.get("key1"));
        assertEquals("newValue2", memcache.get("key2"));


        map = new HashMap<Object, MemcacheService.CasValues>();
        map.put("key1", new MemcacheService.CasValues(identifiable1, "newestValue1"));
        map.put("key2", new MemcacheService.CasValues(identifiable1, "newestValue2"));

        storedKeys = memcache.putIfUntouched(map);
        assertTrue(storedKeys.isEmpty());
        assertEquals("newValue1", memcache.get("key1"));
        assertEquals("newValue2", memcache.get("key2"));
    }
    @Test
    public void testPutIfUntouchedExpire() {
        final String TS_KEY = createTimeStampKey("testPutIfUntouched");

        memcache.put(TS_KEY, STR_VALUE);
        MemcacheService.IdentifiableValue oldOriginalIdValue = memcache.getIdentifiable(TS_KEY);
        final String NEW_VALUE = "new-" + STR_VALUE;

        // Store NEW_VALUE if no other value stored since oldOriginalIdValue was retrieved.
        // memcache.get() has not been called, so this put should succeed.
        Boolean valueWasStored = memcache.putIfUntouched(TS_KEY, oldOriginalIdValue, NEW_VALUE, Expiration.byDeltaSeconds(1));
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

        Map<Object, MemcacheService.CasValues> updateDat = new HashMap<Object, MemcacheService.CasValues>();
        for (Object key : testDat) {
            updateDat.put(key, new MemcacheService.CasValues(memcache.getIdentifiable(key), "new value"));
        }
        Set<Object> set = memcache.putIfUntouched(updateDat, Expiration.byDeltaMillis(1000));
        assertEquals(2, set.size());
        assertEquals("new value", memcache.get(testDat[0]));

        sync(3000);
        assertNull(memcache.get(testDat));
    }

    @Test
    public void testGetAll() {
        memcache.put("key1", "value1");
        memcache.put("key2", "value2");
        memcache.put("key3", "value3");

        Map<String, Object> map = memcache.getAll(Arrays.asList("key1", "key2"));
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    public void testDelete() {
        memcache.put("key", "value");
        memcache.delete("key");
        assertFalse(memcache.contains("key"));
    }

    @Test
    public void testDeleteNoReAddTime() {
        String key = createTimeStampKey("testDeleteNoReAddTime");
        memcache.put(key, STR_VALUE);
        assertNotNull(memcache.get(key));

        // delete and do not allow re-add for another 10 seconds.
        memcache.delete(key, 10 * 1000);
        assertNull("key should be null", memcache.get(KEY1));

        // re-add should fail since within 10 seconds.
        assertFalse(memcache.put(key, STR_VALUE, null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT));
        assertNull("key should be null because of policy.", memcache.get(KEY1));
    }

    @Test
    public void testDeleteAll() {
        memcache.put("key1", "value1");
        memcache.put("key2", "value2");
        memcache.put("key3", "value3");
        memcache.deleteAll(Arrays.asList("key1", "key2"));
        assertFalse(memcache.contains("key1"));
        assertFalse(memcache.contains("key2"));
        assertTrue(memcache.contains("key3"));
    }
    @Test
    public void testDeleteAllNoReAddTime() {
        Map<Object, Object> cacheDat = createSmallBatchData();
        memcache.putAll(cacheDat);
        memcache.deleteAll(cacheDat.keySet(), 10 * 1000);
        Map<Object, Object> retValues = memcache.getAll(cacheDat.keySet());
        assertEquals("All keys should be deleted.", 0, retValues.size());

        memcache.putAll(cacheDat, null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
        Map<Object, Object> retValuesAfter = memcache.getAll(cacheDat.keySet());
        assertEquals("No keys should be added because of policy.", 0, retValuesAfter.size());
    }

    @Test
    public void testPutExpiration() {
        memcache.put("key", "value", Expiration.byDeltaMillis(1000));
        assertTrue(memcache.contains("key"));
        sync();
        assertFalse(memcache.contains("key"));
    }

    @Test
    public void testPutAllExpiration() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        memcache.putAll(map, Expiration.byDeltaMillis(1000));
        assertTrue(memcache.contains("key1"));
        assertTrue(memcache.contains("key2"));
        sync();
        assertFalse(memcache.contains("key1"));
        assertFalse(memcache.contains("key2"));
    }

    @Test
    public void testIncrement() {
        long x = memcache.increment("increment-key", 5, 0L);
        assertEquals(5L, x);
        assertEquals(5L, memcache.get("increment-key"));

        x = memcache.increment("increment-key", 15);
        assertEquals(20L, x);
        assertEquals(20L, memcache.get("increment-key"));

        x = memcache.increment("increment-key", 6);
        assertEquals(26L, x);
        assertEquals(26L, memcache.get("increment-key"));
    }

    @Test
    public void testNegativeIncrementNeverGoesBelowZero() {
        memcache.put("negative-increment-key", 3L);
        long x = memcache.increment("negative-increment-key", -5);
        assertEquals(0L, x);
        assertEquals(0L, memcache.get("negative-increment-key"));
    }

    @Test
    public void testIncrementRetainsValueType() {
        memcache.put("string-key", "15");
        long x = memcache.increment("string-key", 5);
        assertEquals(20L, x);
        assertEquals("20", memcache.get("string-key"));

        memcache.put("byte-key", (byte)15);
        x = memcache.increment("byte-key", 5);
        assertEquals(20L, x);
        assertEquals((byte)20, memcache.get("byte-key"));

        memcache.put("short-key", (short)15);
        x = memcache.increment("short-key", 5);
        assertEquals(20L, x);
        assertEquals((short)20, memcache.get("short-key"));

        memcache.put("integer-key", 15);
        x = memcache.increment("integer-key", 5);
        assertEquals(20L, x);
        assertEquals(20, memcache.get("integer-key"));

        memcache.put("long-key", 15L);
        x = memcache.increment("long-key", 5);
        assertEquals(20L, x);
        assertEquals(20L, memcache.get("long-key"));
    }

    @Test
    public void testIncrementAll() {
        Map<Object, Long> map = createLongBatchData();
        memcache.putAll(map);

        long delta = 10;
        Map<Object, Long> returned = memcache.incrementAll(map.keySet(), delta);

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

        Map<Object, Long> returned = memcache.incrementAll(expected.keySet(), delta, initVal);
        assertEquals(expected, returned);

        Map<Object, Object> fetched = memcache.getAll(expected.keySet());
        assertEquals(expected, fetched);
    }

    @Test
    public void testIncrementAllFromMap() {
        Map<Object, Long> map = createLongBatchData();
        memcache.putAll(map);

        Map<Object, Long> incMap = createRandomIncrementMap(map);

        Map<Object, Long> returned = memcache.incrementAll(incMap);

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

        Map<Object, Long> returned = memcache.incrementAll(incMap, initVal);
        assertEquals(expected, returned);

        Map<Object, Object> fetched = memcache.getAll(incMap.keySet());
        assertEquals(expected, fetched);
    }

    @Test
    public void testNamespace() {
        // memcache.setNamespace() is deprecated.
        MemcacheService otherMemcache = MemcacheServiceFactory.getMemcacheService("other");
        otherMemcache.clearAll();
        memcache.clearAll();

        String key = createTimeStampKey("testNamespace");
        otherMemcache.put(key, "default");
        assertNull("This key should not exist in the default namespace.", memcache.get(key));
        assertNotNull(otherMemcache.get(key));

        String key2 = createTimeStampKey("testNamespace2");
        memcache.put(key2, "default2");
        assertNull("This key should not exist in the other namespace.", otherMemcache.get(key2));
        assertNotNull(memcache.get(key2));
    }
}

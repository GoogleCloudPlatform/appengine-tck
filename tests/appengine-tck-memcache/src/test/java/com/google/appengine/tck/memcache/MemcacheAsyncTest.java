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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class MemcacheAsyncTest extends TestBase {

    protected AsyncMemcacheService service;

    @Deployment
    public static Archive getDeployment() {
        return getTckDeployment();
    }

    @Before
    public void setUp() {
        service = MemcacheServiceFactory.getAsyncMemcacheService();
    }

    @After
    public void tearDown() {
        service.clearAll();
    }

    protected <T> T unwrap(Future<T> f) {
        try {
            return f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testClearAll() {
        unwrap(service.put("key1", "value1"));
        unwrap(service.put("key2", "value2"));
        unwrap(service.put("key3", "value3"));

        service.clearAll();
        sync();

        assertFalse(unwrap(service.contains("key1")));
        assertFalse(unwrap(service.contains("key2")));
        assertFalse(unwrap(service.contains("key3")));
    }

    @Test
    public void testPut() {
        unwrap(service.put("key", "value"));
        assertTrue(unwrap(service.contains("key")));
        assertEquals("value", unwrap(service.get("key")));
    }

    @Test
    public void testPutReplaceOnlyIfPresent() {
        assertFalse(unwrap(service.contains("key")));
        unwrap(service.put("key", "value", null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT));
        assertFalse(unwrap(service.contains("key")));
    }

    @Test
    public void testPutAddOnlyIfNotPresent() {
        unwrap(service.put("key", "firstValue"));
        unwrap(service.put("key", "secondValue", null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT));
        assertEquals("firstValue", unwrap(service.get("key")));
    }

    @Test
    public void testPutAll() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        unwrap(service.putAll(map));

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            assertEquals(entry.getValue(), unwrap(service.get(entry.getKey())));
        }
    }

    @Test
    public void testPutAllReplaceOnlyIfPresent() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        unwrap(service.putAll(map, null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT));

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            assertFalse(unwrap(service.contains(entry.getKey())));
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

        unwrap(service.putAll(firstValues));
        unwrap(service.putAll(secondValues, null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT));

        for (Map.Entry<Object, Object> entry : firstValues.entrySet()) {
            assertEquals(entry.getValue(), unwrap(service.get(entry.getKey())));
        }
    }

    @Test
    public void testGetIdentifiable() {
        unwrap(service.put("key", "value"));
        MemcacheService.IdentifiableValue identifiable = unwrap(service.getIdentifiable("key"));
        assertEquals("value", identifiable.getValue());
    }

    @Test
    public void testGetIdentifiables() {
        unwrap(service.put("key1", "value1"));
        unwrap(service.put("key2", "value2"));
        Map<String, MemcacheService.IdentifiableValue> identifiables = unwrap(service.getIdentifiables(Arrays.asList("key1", "key2")));

        assertEquals(2, identifiables.size());

        assertNotNull(identifiables.get("key1"));
        assertEquals("value1", identifiables.get("key1").getValue());

        assertNotNull(identifiables.get("key2"));
        assertEquals("value2", identifiables.get("key2").getValue());
    }

    @Test
    public void testPutIfUntouched() {
        unwrap(service.put("key", "value"));

        MemcacheService.IdentifiableValue identifiable = unwrap(service.getIdentifiable("key"));

        boolean valueWasStored = unwrap(service.putIfUntouched("key", identifiable, "newValue"));
        assertTrue(valueWasStored);
        assertEquals("newValue", unwrap(service.get("key")));

        boolean valueWasStored2 = unwrap(service.putIfUntouched("key", identifiable, "newestValue"));
        assertFalse(valueWasStored2);
        assertEquals("newValue", unwrap(service.get("key")));
    }

    @Test
    public void testPutIfUntouchedMulti() {
        unwrap(service.put("key1", "value1"));
        unwrap(service.put("key2", "value2"));

        MemcacheService.IdentifiableValue identifiable1 = unwrap(service.getIdentifiable("key1"));
        MemcacheService.IdentifiableValue identifiable2 = unwrap(service.getIdentifiable("key2"));


        HashMap<Object, MemcacheService.CasValues> map = new HashMap<Object, MemcacheService.CasValues>();
        map.put("key1", new MemcacheService.CasValues(identifiable1, "newValue1"));
        map.put("key2", new MemcacheService.CasValues(identifiable2, "newValue2"));

        Set<Object> storedKeys = unwrap(service.putIfUntouched(map));
        assertEquals(2, storedKeys.size());
        assertTrue(storedKeys.contains("key1"));
        assertTrue(storedKeys.contains("key2"));
        assertEquals("newValue1", unwrap(service.get("key1")));
        assertEquals("newValue2", unwrap(service.get("key2")));


        map = new HashMap<Object, MemcacheService.CasValues>();
        map.put("key1", new MemcacheService.CasValues(identifiable1, "newestValue1"));
        map.put("key2", new MemcacheService.CasValues(identifiable1, "newestValue2"));

        storedKeys = unwrap(service.putIfUntouched(map));
        assertTrue(storedKeys.isEmpty());
        assertEquals("newValue1", unwrap(service.get("key1")));
        assertEquals("newValue2", unwrap(service.get("key2")));
    }

    @Test
    public void testGetAll() {
        unwrap(service.put("key1", "value1"));
        unwrap(service.put("key2", "value2"));
        unwrap(service.put("key3", "value3"));

        Map<String, Object> map = unwrap(service.getAll(Arrays.asList("key1", "key2")));
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    public void testDelete() {
        unwrap(service.put("key", "value"));
        unwrap(service.delete("key"));
        assertFalse(unwrap(service.contains("key")));
    }

    @Test
    public void testDeleteAll() {
        unwrap(service.put("key1", "value1"));
        unwrap(service.put("key2", "value2"));
        unwrap(service.put("key3", "value3"));
        unwrap(service.deleteAll(Arrays.asList("key1", "key2")));
        assertFalse(unwrap(service.contains("key1")));
        assertFalse(unwrap(service.contains("key2")));
        assertTrue(unwrap(service.contains("key3")));
    }

    @Test
    public void testPutExpiration() {
        unwrap(service.put("key", "value", Expiration.byDeltaMillis(1000)));
        assertTrue(unwrap(service.contains("key")));
        sync();
        assertFalse(unwrap(service.contains("key")));
    }

    @Test
    public void testPutAllExpiration() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        unwrap(service.putAll(map, Expiration.byDeltaMillis(1000)));
        assertTrue(unwrap(service.contains("key1")));
        assertTrue(unwrap(service.contains("key2")));
        sync();
        assertFalse(unwrap(service.contains("key1")));
        assertFalse(unwrap(service.contains("key2")));
    }

    @Test
    public void testIncrement() {
        long x = unwrap(service.increment("increment-key", 5, 0L));
        assertEquals(5L, x);
        assertEquals(5L, unwrap(service.get("increment-key")));

        x = unwrap(service.increment("increment-key", 15));
        assertEquals(20L, x);
        assertEquals(20L, unwrap(service.get("increment-key")));

        x = unwrap(service.increment("increment-key", 6));
        assertEquals(26L, x);
        assertEquals(26L, unwrap(service.get("increment-key")));
    }

    @Test
    public void testNegativeIncrementNeverGoesBelowZero() {
        unwrap(service.put("negative-increment-key", 3L));
        long x = unwrap(service.increment("negative-increment-key", -5));
        assertEquals(0L, x);
        assertEquals(0L, unwrap(service.get("negative-increment-key")));
    }

    @Test
    public void testIncrementRetainsValueType() {
        unwrap(service.put("string-key", "15"));
        long x = unwrap(service.increment("string-key", 5));
        assertEquals(20L, x);
        assertEquals("20", unwrap(service.get("string-key")));

        unwrap(service.put("byte-key", (byte) 15));
        x = unwrap(service.increment("byte-key", 5));
        assertEquals(20L, x);
        assertEquals((byte) 20, unwrap(service.get("byte-key")));

        unwrap(service.put("short-key", (short) 15));
        x = unwrap(service.increment("short-key", 5));
        assertEquals(20L, x);
        assertEquals((short) 20, unwrap(service.get("short-key")));

        unwrap(service.put("integer-key", 15));
        x = unwrap(service.increment("integer-key", 5));
        assertEquals(20L, x);
        assertEquals(20, unwrap(service.get("integer-key")));

        unwrap(service.put("long-key", 15L));
        x = unwrap(service.increment("long-key", 5));
        assertEquals(20L, x);
        assertEquals(20L, unwrap(service.get("long-key")));
    }
}

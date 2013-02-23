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
public class MemcacheTest extends TestBase {

    protected MemcacheService service;

    @Deployment
    public static Archive getDeployment() {
        return getTckDeployment();
    }

    @Before
    public void setUp() {
        service = MemcacheServiceFactory.getMemcacheService();
    }

    @After
    public void tearDown() {
        service.clearAll();
    }

    @Test
    public void testClearAll() {
        service.put("key1", "value1");
        service.put("key2", "value2");
        service.put("key3", "value3");
        service.clearAll();
        assertFalse(service.contains("key1"));
        assertFalse(service.contains("key2"));
        assertFalse(service.contains("key3"));
    }

    @Test
    public void testPut() {
        service.put("key", "value");
        assertTrue(service.contains("key"));
        assertEquals("value", service.get("key"));
    }

    @Test
    public void testPutReplaceOnlyIfPresent() {
        assertFalse(service.contains("key"));
        service.put("key", "value", null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);
        assertFalse(service.contains("key"));
    }

    @Test
    public void testPutAddOnlyIfNotPresent() {
        service.put("key", "firstValue");
        service.put("key", "secondValue", null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
        assertEquals("firstValue", service.get("key"));
    }

    @Test
    public void testPutAll() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        service.putAll(map);

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            assertEquals(entry.getValue(), service.get(entry.getKey()));
        }
    }

    @Test
    public void testPutAllReplaceOnlyIfPresent() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        service.putAll(map, null, MemcacheService.SetPolicy.REPLACE_ONLY_IF_PRESENT);

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            assertFalse(service.contains(entry.getKey()));
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

        service.putAll(firstValues);
        service.putAll(secondValues, null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT);

        for (Map.Entry<Object, Object> entry : firstValues.entrySet()) {
            assertEquals(entry.getValue(), service.get(entry.getKey()));
        }
    }

    @Test
    public void testGetIdentifiable() {
        service.put("key", "value");
        MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key");
        assertEquals("value", identifiable.getValue());
    }

    @Test
    public void testGetIdentifiables() {
        service.put("key1", "value1");
        service.put("key2", "value2");
        Map<String, MemcacheService.IdentifiableValue> identifiables = service.getIdentifiables(Arrays.asList("key1", "key2"));

        assertEquals(2, identifiables.size());

        assertNotNull(identifiables.get("key1"));
        assertEquals("value1", identifiables.get("key1").getValue());

        assertNotNull(identifiables.get("key2"));
        assertEquals("value2", identifiables.get("key2").getValue());
    }

    @Test
    public void testPutIfUntouched() {
        service.put("key", "value");

        MemcacheService.IdentifiableValue identifiable = service.getIdentifiable("key");

        boolean valueWasStored = service.putIfUntouched("key", identifiable, "newValue");
        assertTrue(valueWasStored);
        assertEquals("newValue", service.get("key"));

        boolean valueWasStored2 = service.putIfUntouched("key", identifiable, "newestValue");
        assertFalse(valueWasStored2);
        assertEquals("newValue", service.get("key"));
    }

    @Test
    public void testPutIfUntouchedMulti() {
        service.put("key1", "value1");
        service.put("key2", "value2");

        MemcacheService.IdentifiableValue identifiable1 = service.getIdentifiable("key1");
        MemcacheService.IdentifiableValue identifiable2 = service.getIdentifiable("key2");


        HashMap<Object, MemcacheService.CasValues> map = new HashMap<Object, MemcacheService.CasValues>();
        map.put("key1", new MemcacheService.CasValues(identifiable1, "newValue1"));
        map.put("key2", new MemcacheService.CasValues(identifiable2, "newValue2"));

        Set<Object> storedKeys = service.putIfUntouched(map);
        assertEquals(2, storedKeys.size());
        assertTrue(storedKeys.contains("key1"));
        assertTrue(storedKeys.contains("key2"));
        assertEquals("newValue1", service.get("key1"));
        assertEquals("newValue2", service.get("key2"));


        map = new HashMap<Object, MemcacheService.CasValues>();
        map.put("key1", new MemcacheService.CasValues(identifiable1, "newestValue1"));
        map.put("key2", new MemcacheService.CasValues(identifiable1, "newestValue2"));

        storedKeys = service.putIfUntouched(map);
        assertTrue(storedKeys.isEmpty());
        assertEquals("newValue1", service.get("key1"));
        assertEquals("newValue2", service.get("key2"));
    }

    @Test
    public void testGetAll() {
        service.put("key1", "value1");
        service.put("key2", "value2");
        service.put("key3", "value3");

        Map<String, Object> map = service.getAll(Arrays.asList("key1", "key2"));
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    public void testDelete() {
        service.put("key", "value");
        service.delete("key");
        assertFalse(service.contains("key"));
    }

    @Test
    public void testDeleteAll() {
        service.put("key1", "value1");
        service.put("key2", "value2");
        service.put("key3", "value3");
        service.deleteAll(Arrays.asList("key1", "key2"));
        assertFalse(service.contains("key1"));
        assertFalse(service.contains("key2"));
        assertTrue(service.contains("key3"));
    }

    @Test
    public void testPutExpiration() {
        service.put("key", "value", Expiration.byDeltaMillis(1000));
        assertTrue(service.contains("key"));
        sync();
        assertFalse(service.contains("key"));
    }

    @Test
    public void testPutAllExpiration() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        service.putAll(map, Expiration.byDeltaMillis(1000));
        assertTrue(service.contains("key1"));
        assertTrue(service.contains("key2"));
        sync();
        assertFalse(service.contains("key1"));
        assertFalse(service.contains("key2"));
    }

    @Test
    public void testIncrement() {
        long x = service.increment("increment-key", 5, 0L);
        assertEquals(5L, x);
        assertEquals(5L, service.get("increment-key"));

        x = service.increment("increment-key", 15);
        assertEquals(20L, x);
        assertEquals(20L, service.get("increment-key"));

        x = service.increment("increment-key", 6);
        assertEquals(26L, x);
        assertEquals(26L, service.get("increment-key"));
    }

    @Test
    public void testNegativeIncrementNeverGoesBelowZero() {
        service.put("negative-increment-key", 3L);
        long x = service.increment("negative-increment-key", -5);
        assertEquals(0L, x);
        assertEquals(0L, service.get("negative-increment-key"));
    }

    @Test
    public void testIncrementRetainsValueType() {
        service.put("string-key", "15");
        long x = service.increment("string-key", 5);
        assertEquals(20L, x);
        assertEquals("20", service.get("string-key"));

        service.put("byte-key", (byte)15);
        x = service.increment("byte-key", 5);
        assertEquals(20L, x);
        assertEquals((byte)20, service.get("byte-key"));

        service.put("short-key", (short)15);
        x = service.increment("short-key", 5);
        assertEquals(20L, x);
        assertEquals((short)20, service.get("short-key"));

        service.put("integer-key", 15);
        x = service.increment("integer-key", 5);
        assertEquals(20L, x);
        assertEquals(20, service.get("integer-key"));

        service.put("long-key", 15L);
        x = service.increment("long-key", 5);
        assertEquals(20L, x);
        assertEquals(20L, service.get("long-key"));
    }
}

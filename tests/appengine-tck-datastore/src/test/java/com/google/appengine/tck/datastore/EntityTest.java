// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * datastore key data type test.
 *
 * @author hchen@google.com (Hannah Chen)
 * @author mluksa@redhat.com (Marko Luksa)
 */
@RunWith(Arquillian.class)
public class EntityTest extends DatastoreTestBase {
    private String kindName = "EntityData";

    @Test
    public void testKeyName() {
        clearData(kindName);
        Entity newRec = new Entity(kindName, "test4116132", null);
        newRec.setProperty("stamp", new Date());
        Key key = service.put(newRec);
        assertEquals("test4116132", key.getName());
        assertEquals(null, key.getParent());

        newRec = new Entity(kindName, "test4116132-child", key);
        newRec.setProperty("stamp", new Date());
        Key ckey = service.put(newRec);
        assertEquals("test4116132-child", ckey.getName());
        assertEquals(key, ckey.getParent());
    }

    @Test
    public void testKeyId() {
        clearData(kindName);
        Entity newRec = new Entity(kindName, 4116132, null);
        newRec.setProperty("stamp", new Date());
        Key key = service.put(newRec);
        assertEquals(4116132, key.getId());
        assertEquals(null, key.getParent());

        newRec = new Entity(kindName, 41161320, key);
        newRec.setProperty("stamp", new Date());
        Key ckey = service.put(newRec);
        assertEquals(41161320, ckey.getId());
        assertEquals(key, ckey.getParent());
    }

    @Test
    public void testKey() {
        clearData(kindName);
        Entity newRec = new Entity(kindName);
        newRec.setProperty("stamp", new Date());
        Key key = service.put(newRec);

        Entity newRec2 = new Entity(key);
        Date newDate = new Date();
        newRec2.setProperty("stamp", newDate);
        service.put(newRec2);
        assertEquals(newRec, newRec2);
        assertEquals(newDate, newRec2.getProperty("stamp"));
    }

    @Test
    public void testEntityGainsNoAdditionalPropertiesWhenStored() throws Exception {
      clearData(kindName);
      Entity entity = new Entity(kindName);
      Key key = service.put(entity);
      entity = service.get(key);
      assertEquals(Collections.<String, Object>emptyMap(), entity.getProperties());
    }
}

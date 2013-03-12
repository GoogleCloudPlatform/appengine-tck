// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * datastore key data type test.
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class EntityTest extends DatastoreTestBase {
  private String kindName = "EntityData";

  @Test 
  public void  testKeyName() throws InterruptedException {
    clearData(kindName);
    Entity newRec =  new Entity(kindName, "test4116132", null);
    newRec.setProperty("stamp", new Date());
    Key key = datastoreService.put(newRec);
    assertEquals("test4116132", key.getName());
    assertEquals(null, key.getParent());
    
    newRec =  new Entity(kindName, "test4116132-child", key);
    newRec.setProperty("stamp", new Date());
    Key ckey = datastoreService.put(newRec);
    assertEquals("test4116132-child", ckey.getName());
    assertEquals(key, ckey.getParent());
  }
  
  @Test 
  public void  testKeyId() throws InterruptedException {
    clearData(kindName);
    Entity newRec =  new Entity(kindName, 4116132, null);
    newRec.setProperty("stamp", new Date());
    Key key = datastoreService.put(newRec);
    assertEquals(4116132, key.getId());
    assertEquals(null, key.getParent());
    
    newRec =  new Entity(kindName, 41161320, key);
    newRec.setProperty("stamp", new Date());
    Key ckey = datastoreService.put(newRec);
    assertEquals(41161320, ckey.getId());
    assertEquals(key, ckey.getParent());
  }
  
  @Test 
  public void  testKey() throws InterruptedException {
    clearData(kindName);
    Entity newRec =  new Entity(kindName);
    newRec.setProperty("stamp", new Date());
    Key key = datastoreService.put(newRec);
    
    Entity newRec2 =  new Entity(key);
    Date newDate = new Date();
    newRec2.setProperty("stamp", newDate);
    datastoreService.put(newRec2);
    assertEquals(newRec, newRec2);
    assertEquals(newDate, newRec2.getProperty("stamp"));
  }

  @Override
  public void clearData(String kind) throws InterruptedException {
    List<Key> eList = new ArrayList<Key>();
    Query query = new Query(kind);
    for (Entity readRec : datastoreService.prepare(query).asIterable()) {
      eList.add(readRec.getKey());
    }
    if (eList.size() > 0) {
      datastoreService.delete(eList);
      Thread.sleep(waitTime);
    }
  }
}

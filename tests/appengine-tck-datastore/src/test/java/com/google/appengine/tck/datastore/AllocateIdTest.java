package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.DatastoreService.KeyRangeState;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Query;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Iterator;

/**
 * datastore allocating test.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class AllocateIdTest extends AbstractDatastoreTest {
  private String pKind = "parent";
  private String cKind = "child";
  private long allocateNum = 5;

  @Test
  public void testAllocateParent() {
    KeyRange range = datastoreService.allocateIds(pKind, allocateNum);
    check(pKind, range);
  }

  @Test
  public void testAllocateChild() {
    Entity parent = new Entity(pKind);
    parent.setProperty("name", "parent" + new Date());
    Key pKey = datastoreService.put(parent);
    KeyRange range = datastoreService.allocateIds(pKey, cKind, allocateNum);
    check(cKind, range);
    Entity child = new Entity(range.getStart());
    child.setProperty("name", "second" + new Date());
    Key ckey = datastoreService.put(child);
    // child with allocated key should have correct parent.
    assertEquals(pKey, ckey.getParent());
  }

  @Test
  public void testAlocateRange() {
    Query query = new Query(cKind);
    for (Entity readRec : datastoreService.prepare(query).asIterable()) {
      datastoreService.delete(readRec.getKey());
    }
    Entity entity = new Entity(cKind);
    entity.setProperty("name", "exist" + new Date());
    Key key = datastoreService.put(entity);
    // entities with keys inside range already exist
    KeyRange keyRange = new KeyRange(null, cKind, key.getId(), key.getId());
    assertEquals(KeyRangeState.COLLISION, datastoreService.allocateIdRange(keyRange));
    // entities with keys inside range is empty.
    keyRange = new KeyRange(null, cKind, key.getId() + 1, key.getId() + allocateNum);
    KeyRangeState retStatus = datastoreService.allocateIdRange(keyRange);
    assertTrue((retStatus == KeyRangeState.CONTENTION) || (retStatus == KeyRangeState.EMPTY));
    assertEquals(KeyRangeState.CONTENTION, datastoreService.allocateIdRange(keyRange));
    // request again with same range
    assertEquals(KeyRangeState.CONTENTION, datastoreService.allocateIdRange(keyRange));
    // entities with keys inside range already exist
    entity = new Entity(KeyFactory.createKey(cKind, key.getId() + 1));
    entity.setProperty("name", "exist" + new Date());
    datastoreService.put(entity);
    assertEquals(KeyRangeState.COLLISION, datastoreService.allocateIdRange(keyRange));
  }

  private void check(String kind, KeyRange range) {
    Entity entity = new Entity(kind);
    entity.setProperty("name", "first" + new Date());
    Key key = datastoreService.put(entity);
    // allocated key should not be re-used.
    assertFalse(key.equals(range.getStart()));
    assertFalse(key.equals(range.getEnd()));
    assertEquals(allocateNum, range.getSize());
    Iterator<Key> keys = range.iterator();
    while (keys.hasNext()) {
      assertFalse(key.equals(keys.next()));
    }
  }
}

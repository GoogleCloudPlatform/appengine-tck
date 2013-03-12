package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * XG. Cross group test.  This feature is only for HRD app. 
 * To use it in dev_appserver, need following flag to start dev_appserver
 * --jvm_flag=-Ddatastore.default_high_rep_job_policy_unapplied_job_pct=20 
 * 
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class TransactionTest extends AbstractDatastoreTest {
  private String kindName = "TrData";
  private String otherkind = "OtData";
  private int sleepTime = 3000;

  // single entity
  @Test
  public void  testSingleDefaut() throws EntityNotFoundException, InterruptedException {
    clearData(kindName);
    Transaction tx = datastoreService.beginTransaction();
    Entity newRec =  new Entity(kindName);
    newRec.setProperty("check", "4100331");
    newRec.setProperty("step", "added");
    Key key = datastoreService.put(tx, newRec);
    tx.commit();
    Entity qRec = datastoreService.get(key);
    assertEquals("4100331", qRec.getProperty("check"));

    tx = datastoreService.beginTransaction();
    qRec = datastoreService.get(key);
    qRec.setUnindexedProperty("step", "update");
    datastoreService.put(tx, newRec);
    tx.rollback();
    qRec = datastoreService.get(key);
    assertEquals("added", qRec.getProperty("step"));
  }

  // multiple entities in same group with default transaction setting
  @Test
  public void  testMultipleSameGroupDefault() throws InterruptedException {
    clearData(kindName);
    List<Entity> es = new ArrayList<Entity>();
    Transaction tx = datastoreService.beginTransaction();
    Entity parent =  new Entity(kindName);
    parent.setProperty("check", "parent");
    parent.setProperty("stamp", new Date());
    Key pKey = datastoreService.put(tx, parent);

    Entity child =  new Entity(kindName, pKey);
    child.setProperty("check", "other");
    child.setProperty("stamp", new Date());
    Key cKey = datastoreService.put(tx, child);
    tx.commit();
    Thread.sleep(sleepTime);

    Query q = new Query(kindName);
    int count = datastoreService.prepare(q).countEntities(FetchOptions.Builder.withDefaults());
    assertEquals(2, count);
    for (Entity readRec : datastoreService.prepare(q).asIterable()) {
      if (readRec.getProperty("check").equals("parent")) {
        pKey = readRec.getKey();
      } else {
        child = readRec;
      }
    }
    assertEquals(pKey, child.getParent());
  }

  // multiple entities in different group with default transaction setting
  @Test(expected = IllegalArgumentException.class)
  public void  testMultipleNotSameGroupDefault() throws InterruptedException {
    clearData(kindName);
    writeMultipleGroup(false);
  }

  // use closed transaction
  @Test(expected = IllegalStateException.class)
  public void  testClosedTx() throws InterruptedException {
    clearData(kindName);
    Transaction tx = datastoreService.beginTransaction();
    Entity newRec =  new Entity(kindName);
    newRec.setProperty("check", "4100331");
    newRec.setProperty("stamp", new Date());
    Key key = datastoreService.put(newRec);
    tx.commit();
    datastoreService.put(tx, new Entity(kindName));
  }

  // transactionOptions setting
  @Test
  public void testTransactionOptions() {
    TransactionOptions tos =  TransactionOptions.Builder.withXG(true);
    assertEquals(true, tos.isXG());
    tos.clearXG();
    assertEquals(false, tos.isXG());
  }

  // multiple entities in different group with true setting on allowsMultipleEntityGroups
  @Test
  public void testAllowMultipleGroupTrue() throws InterruptedException {
    clearData(kindName);
    clearData(otherkind);
    writeMultipleGroup(true);

    Query q = new Query(kindName);
    Entity e = datastoreService.prepare(q).asSingleEntity();
    assertEquals("parent", e.getProperty("check"));
    q = new Query(otherkind);
    e = datastoreService.prepare(q).asSingleEntity();
    assertEquals("other", e.getProperty("check"));
  }

  // multiple entities in different group with true setting on allowsMultipleEntityGroups
  @Test
  public void testAllowMultipleGroupTrueWithList() throws InterruptedException {
    clearData(kindName);
    clearData(otherkind);
    writeMultipleInList(true);
    
    List<Entity> es = readMultipleGroup();
    assertEquals("parent", es.get(0).getProperty("check"));
    assertEquals("other", es.get(1).getProperty("check"));
  }

  // rollback transaction with true setting for allowsMultipleEntityGroups
  @Test
  public void testTransactionRollback() throws InterruptedException {
    clearData(kindName);
    clearData(otherkind);
    writeMultipleGroup(true);
    List<Entity> es = readMultipleGroup();

    TransactionOptions tos =  TransactionOptions.Builder.withXG(true);
    Transaction tx = datastoreService.beginTransaction(tos);
    es.get(0).setProperty("check", "parent-update");
    es.get(1).setProperty("check", "other-update");
    datastoreService.put(tx, es);
    tx.rollback();
    es = readMultipleGroup();
    assertEquals("parent", es.get(0).getProperty("check"));
    assertEquals("other", es.get(1).getProperty("check"));
  }

  // multiple entities in different group with false setting on allowsMultipleEntityGroups
  @Test(expected = IllegalArgumentException.class)
  public void testAllowMultipleGroupFalse() throws InterruptedException {
    clearData(kindName);
    clearData(otherkind);
    writeMultipleInList(false);
  }

  // false on allowsMultipleEntityGroups + namespaces
  @Test(expected = IllegalArgumentException.class)
  public void testAllowMultipleGroupFalseWithNs() throws InterruptedException{
    NamespaceManager.set("");
    clearData(kindName);
    NamespaceManager.set("trns");
    clearData(kindName);
    TransactionOptions tos =  TransactionOptions.Builder.withXG(false);
    Transaction tx = datastoreService.beginTransaction(tos);

    List<Entity> es = new ArrayList<Entity>();
    NamespaceManager.set("");
    Entity ens1 =  new Entity(kindName);
    ens1.setProperty("check", "entity-nons");
    ens1.setProperty("stamp", new Date());
    es.add(ens1);

    NamespaceManager.set("trns");
    Entity ens2 =  new Entity(kindName);
    ens2.setProperty("check", "entity-trns");
    ens2.setProperty("stamp", new Date());
    es.add(ens2);
    datastoreService.put(tx, es);
    tx.commit();
    NamespaceManager.set("");
  }

  // true on allowsMultipleEntityGroups + namespaces
  @Test
  public void testAllowMultipleGroupTrueWithNs() throws InterruptedException {
    NamespaceManager.set("");
    clearData(kindName);
    NamespaceManager.set("trns");
    clearData(kindName);
    List<Entity> es = new ArrayList<Entity>();
    TransactionOptions tos =  TransactionOptions.Builder.withXG(true);
    Transaction tx = datastoreService.beginTransaction(tos);

    NamespaceManager.set("");
    Entity ens1 =  new Entity(kindName);
    ens1.setProperty("check", "entity-nons");
    ens1.setProperty("stamp", new Date());
    es.add(ens1);

    NamespaceManager.set("trns");
    Entity ens2 =  new Entity(kindName);
    ens2.setProperty("check", "entity-trns");
    ens2.setProperty("stamp", new Date());
    es.add(ens2);
    datastoreService.put(tx, es);
    tx.commit();
    Thread.sleep(sleepTime);

    NamespaceManager.set("");
    Query q = new Query(kindName);
    Entity e = datastoreService.prepare(q).asSingleEntity();
    assertEquals("entity-nons", e.getProperty("check"));
    NamespaceManager.set("trns");
    q = new Query(kindName);
    e = datastoreService.prepare(q).asSingleEntity();
    assertEquals("entity-trns", e.getProperty("check"));
    NamespaceManager.set("");
  }

  private void writeMultipleGroup(boolean allow) throws InterruptedException {
    TransactionOptions tos =  TransactionOptions.Builder.withXG(allow);
    Transaction tx = datastoreService.beginTransaction(tos);
    Entity parent =  new Entity(kindName);
    parent.setProperty("check", "parent");
    parent.setProperty("stamp", new Date());
    Key pKey = datastoreService.put(tx, parent);

    Entity other =  new Entity(otherkind);
    other.setProperty("check", "other");
    other.setProperty("stamp", new Date());
    Key cKey = datastoreService.put(tx, other);
    tx.commit();
    Thread.sleep(sleepTime);
  }

  private void writeMultipleInList(boolean allow) throws InterruptedException {
    List<Entity> es = new ArrayList<Entity>();
    TransactionOptions tos =  TransactionOptions.Builder.withXG(allow);
    Transaction tx = datastoreService.beginTransaction(tos);
    Entity parent =  new Entity(kindName);
    parent.setProperty("check", "parent");
    parent.setProperty("stamp", new Date());
    es.add(parent);

    Entity other =  new Entity(otherkind);
    other.setProperty("check", "other");
    other.setProperty("stamp", new Date());
    es.add(other);
    datastoreService.put(tx, es);
    tx.commit();
    Thread.sleep(sleepTime);
  }

  private List<Entity> readMultipleGroup() {
    List<Entity> es = new ArrayList<Entity>();
    es.clear();
    Query q = new Query(kindName);
    es.add(datastoreService.prepare(q).asSingleEntity());
    q = new Query(otherkind);
    es.add(datastoreService.prepare(q).asSingleEntity());
    return es;
  }

  @Override
  public void clearData(String kind) throws InterruptedException {
    List<Key> elist = new ArrayList<Key>();
    Query query = new Query(kind);
    for (Entity readRec : datastoreService.prepare(query).asIterable()) {
      elist.add(readRec.getKey());
    }
    datastoreService.delete(elist);
    Thread.sleep(sleepTime);
  }
}

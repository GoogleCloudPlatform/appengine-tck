package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import static org.junit.Assert.assertEquals;

/**
 * XG. Cross group test.  This feature is only for HRD app.
 * To use it in dev_appserver, need following flag to start dev_appserver
 * --jvm_flag=-Ddatastore.default_high_rep_job_policy_unapplied_job_pct=20
 *
 * @author hchen@google.com (Hannah Chen)
 * @author ales.justin@jboss.org
 */
@RunWith(Arquillian.class)
public class TransactionTest extends DatastoreTestBase {
    private String kindName = "TrData";
    private String otherkind = "OtData";
    private int sleepTime = 3000;

    // single entity
    @Test
    public void testSingleDefaut() throws EntityNotFoundException, InterruptedException {
        clearData(kindName);
        Transaction tx = service.beginTransaction();
        Entity newRec = new Entity(kindName);
        newRec.setProperty("check", "4100331");
        newRec.setProperty("step", "added");
        Key key = service.put(tx, newRec);
        tx.commit();
        Entity qRec = service.get(key);
        assertEquals("4100331", qRec.getProperty("check"));

        tx = service.beginTransaction();
        qRec = service.get(key);
        qRec.setUnindexedProperty("step", "update");
        service.put(tx, newRec);
        tx.rollback();
        qRec = service.get(key);
        assertEquals("added", qRec.getProperty("step"));
    }

    // multiple entities in same group with default transaction setting
    @Test
    public void testMultipleSameGroupDefault() throws InterruptedException {
        clearData(kindName);
        List<Key> keys = new ArrayList<Key>();
        Transaction tx = service.beginTransaction();
        Entity parent = new Entity(kindName);
        parent.setProperty("check", "parent");
        parent.setProperty("stamp", new Date());
        Key pKey = service.put(tx, parent);
        keys.add(pKey);

        Entity child = new Entity(kindName, pKey);
        child.setProperty("check", "other");
        child.setProperty("stamp", new Date());
        Key cKey = service.put(tx, child);
        keys.add(cKey);
        tx.commit();
        sync(sleepTime);

        Query q = new Query(kindName);
        int count = service.prepare(q).countEntities(FetchOptions.Builder.withDefaults());
        assertEquals(2, count);

        Map<Key, Entity> es;
        tx = service.beginTransaction();
        es = service.get(tx, keys);
        for (Entity readRec : es.values()) {
            if (readRec.getProperty("check").equals("parent")) {
                pKey = readRec.getKey();
            } else {
                child = readRec;
            }
        }
        assertEquals(pKey, child.getParent());

        service.delete(tx, keys);
        tx.commit();
        sync(sleepTime);
        es = service.get(keys);
        assertEquals(0, es.size());
    }

    // multiple entities in different group with default transaction setting
    @Test(expected = IllegalArgumentException.class)
    public void testMultipleNotSameGroupDefault() throws Exception {
        clearData(kindName);
        writeMultipleGroup(false);
    }

    // use closed transaction
    @Test(expected = IllegalStateException.class)
    public void testClosedTx() throws InterruptedException {
        clearData(kindName);
        Transaction tx = service.beginTransaction();
        Entity newRec = new Entity(kindName);
        newRec.setProperty("check", "4100331");
        newRec.setProperty("stamp", new Date());
        Key key = service.put(newRec);
        tx.commit();
        service.put(tx, new Entity(kindName));
    }

    // transactionOptions setting
    @Test
    public void testTransactionOptions() {
        TransactionOptions tos = TransactionOptions.Builder.withXG(true);
        assertEquals(true, tos.isXG());
        tos.clearXG();
        assertEquals(false, tos.isXG());
    }

    // multiple entities in different group with true setting on allowsMultipleEntityGroups
    @Test
    public void testAllowMultipleGroupTrue() throws Exception {
        clearData(kindName);
        clearData(otherkind);
        writeMultipleGroup(true);

        Query q = new Query(kindName);
        Entity e = service.prepare(q).asSingleEntity();
        assertEquals("parent", e.getProperty("check"));
        q = new Query(otherkind);
        e = service.prepare(q).asSingleEntity();
        assertEquals("other", e.getProperty("check"));
    }

    // multiple entities in different group with true setting on allowsMultipleEntityGroups
    @Test
    public void testAllowMultipleGroupTrueWithList() throws Exception {
        clearData(kindName);
        clearData(otherkind);
        writeMultipleInList(true);

        List<Entity> es = readMultipleGroup();
        assertEquals("parent", es.get(0).getProperty("check"));
        assertEquals("other", es.get(1).getProperty("check"));
    }

    // rollback transaction with true setting for allowsMultipleEntityGroups
    @Test
    public void testTransactionRollback() throws Exception {
        clearData(kindName);
        clearData(otherkind);
        writeMultipleGroup(true);
        List<Entity> es = readMultipleGroup();

        TransactionOptions tos = TransactionOptions.Builder.withXG(true);
        Transaction tx = service.beginTransaction(tos);
        es.get(0).setProperty("check", "parent-update");
        es.get(1).setProperty("check", "other-update");
        service.put(tx, es);
        tx.rollback();
        es = readMultipleGroup();
        assertEquals("parent", es.get(0).getProperty("check"));
        assertEquals("other", es.get(1).getProperty("check"));
    }

    // multiple entities in different group with false setting on allowsMultipleEntityGroups
    @Test(expected = IllegalArgumentException.class)
    public void testAllowMultipleGroupFalse() throws Exception {
        clearData(kindName);
        clearData(otherkind);
        writeMultipleInList(false);
    }

    // false on allowsMultipleEntityGroups + namespaces
    @Test(expected = IllegalArgumentException.class)
    public void testAllowMultipleGroupFalseWithNs() throws Exception {
        NamespaceManager.set("");
        clearData(kindName);
        NamespaceManager.set("trns");
        try {
            clearData(kindName);
            TransactionOptions tos = TransactionOptions.Builder.withXG(false);
            Transaction tx = service.beginTransaction(tos);
            try {
                List<Entity> es = new ArrayList<Entity>();
                NamespaceManager.set("");
                Entity ens1 = new Entity(kindName);
                ens1.setProperty("check", "entity-nons");
                ens1.setProperty("stamp", new Date());
                es.add(ens1);

                NamespaceManager.set("trns");
                Entity ens2 = new Entity(kindName);
                ens2.setProperty("check", "entity-trns");
                ens2.setProperty("stamp", new Date());
                es.add(ens2);
                service.put(tx, es);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        } finally {
            NamespaceManager.set("");
        }
    }

    // true on allowsMultipleEntityGroups + namespaces
    @Test
    public void testAllowMultipleGroupTrueWithNs() throws InterruptedException {
        NamespaceManager.set("");
        clearData(kindName);
        NamespaceManager.set("trns");
        clearData(kindName);
        List<Entity> es = new ArrayList<Entity>();
        TransactionOptions tos = TransactionOptions.Builder.withXG(true);
        Transaction tx = service.beginTransaction(tos);

        NamespaceManager.set("");
        Entity ens1 = new Entity(kindName);
        ens1.setProperty("check", "entity-nons");
        ens1.setProperty("stamp", new Date());
        es.add(ens1);

        NamespaceManager.set("trns");
        Entity ens2 = new Entity(kindName);
        ens2.setProperty("check", "entity-trns");
        ens2.setProperty("stamp", new Date());
        es.add(ens2);
        service.put(tx, es);
        tx.commit();

        sync(sleepTime);

        NamespaceManager.set("");
        Query q = new Query(kindName);
        Entity e = service.prepare(q).asSingleEntity();
        assertEquals("entity-nons", e.getProperty("check"));
        NamespaceManager.set("trns");
        q = new Query(kindName);
        e = service.prepare(q).asSingleEntity();
        assertEquals("entity-trns", e.getProperty("check"));
        NamespaceManager.set("");
    }

    private void writeMultipleGroup(boolean allow) throws Exception {
        TransactionOptions tos = TransactionOptions.Builder.withXG(allow);
        Transaction tx = service.beginTransaction(tos);
        try {
            Entity parent = new Entity(kindName);
            parent.setProperty("check", "parent");
            parent.setProperty("stamp", new Date());
            Key pKey = service.put(tx, parent);

            Entity other = new Entity(otherkind);
            other.setProperty("check", "other");
            other.setProperty("stamp", new Date());
            Key cKey = service.put(tx, other);
            tx.commit();

            sync(sleepTime);
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        sync(sleepTime);
    }

    private void writeMultipleInList(boolean allow) throws Exception {
        List<Entity> es = new ArrayList<Entity>();
        TransactionOptions tos = TransactionOptions.Builder.withXG(allow);
        Transaction tx = service.beginTransaction(tos);
        try {
            Entity parent = new Entity(kindName);
            parent.setProperty("check", "parent");
            parent.setProperty("stamp", new Date());
            es.add(parent);

            Entity other = new Entity(otherkind);
            other.setProperty("check", "other");
            other.setProperty("stamp", new Date());
            es.add(other);
            service.put(tx, es);
            tx.commit();

            sync(sleepTime);
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        sync(sleepTime);
    }

    private List<Entity> readMultipleGroup() {
        List<Entity> es = new ArrayList<Entity>();
        es.clear();
        Query q = new Query(kindName);
        es.add(service.prepare(q).asSingleEntity());
        q = new Query(otherkind);
        es.add(service.prepare(q).asSingleEntity());
        return es;
    }

    @Override
    public void clearData(String kind) {
        List<Key> elist = new ArrayList<Key>();
        Query query = new Query(kind);
        for (Entity readRec : service.prepare(query).asIterable()) {
            elist.add(readRec.getKey());
        }
        service.delete(elist);

        sync(sleepTime);
    }
}

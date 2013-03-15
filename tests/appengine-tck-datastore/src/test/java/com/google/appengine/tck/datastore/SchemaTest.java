package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * datastore Metadata Queries test.
 * http://code.google.com/appengine/docs/java/datastore/metadataqueries.html
 *
 * @author hchen@google.com (Hannah Chen)
 * @author ales.justin@jboss.org (Ales Justin)
 */
@RunWith(Arquillian.class)
public class SchemaTest extends DatastoreTestBase {
    private String[] namespaceDat = {"", "developerSchemaNS", "testingSchemaNS"};
    private String[] kindDat = {"google", "developer", "testing"};
    private int count = 3;
    private FetchOptions fo = FetchOptions.Builder.withDefaults();

    @Before
    public void createData() throws InterruptedException {
        List<Entity> eList = new ArrayList<Entity>();
        for (int i = 0; i < namespaceDat.length; i++) {
            NamespaceManager.set(namespaceDat[i]);
            for (int k = 0; k < kindDat.length; k++) {
                Query q = new Query(kindDat[k]);
                if (datastoreService.prepare(q).countEntities(fo) == 0) {
                    for (int c = 0; c < count; c++) {
                        Entity newRec = new Entity(kindDat[k]);
                        newRec.setProperty("name", kindDat[k] + c);
                        newRec.setProperty("timestamp", new Date());
                        newRec.setProperty("shortBlobData", new ShortBlob("shortBlobData".getBytes()));
                        newRec.setProperty("intData", 12345);
                        newRec.setProperty("textData", new Text("textData"));
                        newRec.setProperty("floatData", new Double(12345.12345));
                        newRec.setProperty("booleanData", true);
                        newRec.setProperty("urlData", new Link("http://www.google.com"));
                        newRec.setProperty("emailData", new Email("somebody123@google.com"));
                        newRec.setProperty("phoneData", new PhoneNumber("408-123-4567"));
                        newRec.setProperty("adressData", new PostalAddress("123 st. CA 12345"));
                        newRec.setProperty("ratingData", new Rating(55));
                        newRec.setProperty("geoptData", new GeoPt((float) 12.12, (float) 98.98));
                        newRec.setProperty("categoryData", new Category("abc"));
                        eList.add(newRec);
                    }
                }
            }
        }
        if (eList.size() > 0) {
            datastoreService.put(eList);
            Thread.sleep(waitTime);
        }
    }

    @Test
    public void testNamespaceMetadata() {
        NamespaceManager.set(""); // TODO -- shouldn't Entities.createNamespaceKey already do this by default?
        Query q = new Query("__namespace__").addSort(Entity.KEY_RESERVED_PROPERTY);
        Key key1 = Entities.createNamespaceKey(namespaceDat[1]);
        Key key2 = Entities.createNamespaceKey(namespaceDat[2]);
        q.setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.IN, Arrays.asList(key1, key2)));
        List<Entity> ns = datastoreService.prepare(q).asList(fo);
        assertEquals(2, ns.size());

        // Sort actual keys
        String sk1 = KeyFactory.keyToString(key1);
        String sk2 = KeyFactory.keyToString(key2);
        Map<String, String> map = new TreeMap<String, String>();
        map.put(sk1, namespaceDat[1]);
        map.put(sk2, namespaceDat[2]);
        List<String> keys = new ArrayList<String>(map.keySet());

        for (int i = 0; i < ns.size(); i++) {
            assertEquals(ns.get(i).getKey().getName(), map.get(keys.get(i)));
        }
    }

    @Test
    public void testKindMetadata() {
        // check non empty namespace only
        for (int i = 1; i < namespaceDat.length; i++) {
            NamespaceManager.set(namespaceDat[i]);
            Query q = new Query("__kind__").addSort(Entity.KEY_RESERVED_PROPERTY);
            int count = 0;
            for (Entity e : datastoreService.prepare(q).asIterable()) {
                // do not count those stats entities for namespace.
                if (!e.getKey().getName().startsWith("__Stat_Ns_")) {
                    count++;
                }
            }
            // For each namespace, only 3 user defined kinds.
            assertEquals(3, count);
            // check a specified namespace
            Key key1 = Entities.createKindKey("testing");
            q.setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key1));
            assertEquals(1, datastoreService.prepare(q).countEntities(fo));
            Entity ke = datastoreService.prepare(q).asSingleEntity();
            assertEquals("testing", ke.getKey().getName());
            assertEquals(namespaceDat[i], ke.getKey().getNamespace());
            assertEquals(namespaceDat[i], ke.getNamespace());
        }
    }

    @Test
    public void testPropertyMetadata() {
        NamespaceManager.set(namespaceDat[2]);
        // sort by kind/property, kindDat[1] < kindDat[0] < kindDat[2]
        Query q = new Query("__property__").addSort(Entity.KEY_RESERVED_PROPERTY).setKeysOnly();
        // filter out properties for kind "testing"
        Key key1 = Entities.createPropertyKey(kindDat[0], "urlData");
        Key key2 = Entities.createPropertyKey(kindDat[2], "urlData");
        q.setFilter(CompositeFilterOperator.and(
                new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.GREATER_THAN, key1),
                new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.LESS_THAN_OR_EQUAL, key2)));
        List<Entity> el = datastoreService.prepare(q).asList(fo);
        // un-indexed property, textData, will not be returned in __property__ queries.
        assertEquals(13, el.size());
        for (int i = 0; i < el.size(); i++) {
            assertEquals(namespaceDat[2], el.get(i).getKey().getNamespace());
            assertEquals(kindDat[2], el.get(i).getKey().getParent().getName());
            if (i == 0) {
                assertEquals("adressData", el.get(0).getKey().getName());
            } else if (i == el.size() - 1) {
                assertEquals("urlData", el.get(el.size() - 1).getKey().getName());
            }
        }
    }

    // work for HRD datastore only
    @Test
    public void testEntityGroupMetadata() throws EntityNotFoundException {
        if (datastoreService.getDatastoreAttributes().getDatastoreType() == DatastoreAttributes.DatastoreType.HIGH_REPLICATION) {
            NamespaceManager.set(namespaceDat[2]);
            Entity entity1 = new Entity(kindDat[2]);
            entity1.setProperty("name", "entity1");
            entity1.setProperty("timestamp", new Date());
            Key k1 = datastoreService.put(entity1);
            Key entityGroupKey = Entities.createEntityGroupKey(k1);
            long version1 = Entities.getVersionProperty(datastoreService.get(entityGroupKey));

            Entity entity2 = new Entity(kindDat[2]);
            entity2.setProperty("name", "entity2");
            entity2.setProperty("timestamp", new Date());
            datastoreService.put(entity2);
            // Get entity1's version again.  There should be no change.
            long version2 = Entities.getVersionProperty(datastoreService.get(entityGroupKey));
            assertEquals(version1, version2);

            Entity entity3 = new Entity(kindDat[2], k1);
            entity3.setProperty("name", "entity3");
            entity3.setProperty("timestamp", new Date());
            datastoreService.put(entity3);
            // Get entity1's version again.  There should be change since it is used as parent.
            long version3 = Entities.getVersionProperty(datastoreService.get(entityGroupKey));
            assertTrue(version3 > version1);
            // clean test data
            datastoreService.delete(entity3.getKey(), entity2.getKey(), k1);
        }
    }
}

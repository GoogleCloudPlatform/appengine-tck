package com.google.appengine.tck.datastore;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.MemcacheSerialization;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * datastore key data type test.
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class KeyTest extends DatastoreTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String kindName = "keyData";

  @Before
  public void createData() throws InterruptedException {
    Query q = new Query(kindName, rootKey);
    if (datastoreService.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
      Entity newRec;
      String[] locDat = {"ac", "ab", "ae", "aa", "ac"};
      List<Entity> elist = new ArrayList<Entity>();
      int[] popDat = {8008278, 279557, 1222, 0, 12345};
      for (int i = 0; i < locDat.length; i++) {
        newRec = new Entity(kindName, rootKey);
        newRec.setProperty("loc", locDat[i]);
        newRec.setProperty("pop", popDat[i]);
        elist.add(newRec);
      }
      datastoreService.put(elist);
      Thread.sleep(waitTime);
    }
  }

  @Test 
  public void testKeyOrder() {
    Query query = new Query(kindName, rootKey);
    query.addSort("__key__");
    List<Entity> ascRecs = datastoreService.prepare(query).asList(withLimit(5));

    query = new Query(kindName, rootKey);
    query.addSort("__key__", Query.SortDirection.DESCENDING);
    List<Entity> descRecs = datastoreService.prepare(query).asList(withLimit(5));

    int size = ascRecs.size();
    assertEquals(5, size);
    for (int i = 0; i < size; i++) {
      assertEquals(ascRecs.get(i).getProperty("pop").toString(), 
                   descRecs.get(size - i - 1).getProperty("pop").toString());
    }
  }

  @Test 
  public void testWithIneqi() {
    Query query = new Query(kindName, rootKey);
    query.setFilter(new FilterPredicate("loc", Query.FilterOperator.EQUAL, "ae"));
    Key key = datastoreService.prepare(query).asSingleEntity().getKey();

    query = new Query(kindName, rootKey);
    query.setFilter(new FilterPredicate("__key__", Query.FilterOperator.GREATER_THAN, key));
    query.addSort("__key__");
    List<Entity> ascRecs = datastoreService.prepare(query).asList(withLimit(5));

    query = new Query(kindName, rootKey);
    query.setFilter(new FilterPredicate("__key__", Query.FilterOperator.GREATER_THAN, key));
    query.addSort("__key__", Query.SortDirection.DESCENDING);
    List<Entity> descRecs = datastoreService.prepare(query).asList(withLimit(5));
    
    int size = ascRecs.size();
    for (int i = 0; i < size; i++) {
      assertEquals(ascRecs.get(i).getProperty("pop").toString(), 
                   descRecs.get(size - i - 1).getProperty("pop").toString());
    }
  }

  @Test 
  public void testWithIneqiAndFilter() {
    Query query = new Query(kindName, rootKey);
    query.setFilter(new FilterPredicate("loc", Query.FilterOperator.EQUAL, "ae"));
    Key key = datastoreService.prepare(query).asSingleEntity().getKey();
  
    query = new Query(kindName, rootKey);
    query.setFilter(new FilterPredicate("__key__", Query.FilterOperator.LESS_THAN, key));
    query.setFilter(new FilterPredicate("loc", Query.FilterOperator.EQUAL, "ac"));
    query.addSort("__key__");
    List<Entity> ascRecs = datastoreService.prepare(query).asList(withLimit(5));
  
    query = new Query(kindName, rootKey);
    query.setFilter(new FilterPredicate("__key__", Query.FilterOperator.LESS_THAN, key));
    query.setFilter(new FilterPredicate("loc", Query.FilterOperator.EQUAL, "ac"));
    query.addSort("__key__", Query.SortDirection.DESCENDING);
    List<Entity> descRecs = datastoreService.prepare(query).asList(withLimit(5));

    int size = ascRecs.size();
    for (int i = 0; i < size; i++) {
      assertEquals(ascRecs.get(i).getProperty("pop").toString(), 
                   descRecs.get(size - i - 1).getProperty("pop").toString());
    }
  }

  @Test
  public void testWithNamespce() {
    String[] namespaceDat = {"", "developer", "testing"};
    Entity entity;
    String kindTest = kindName + "-NS";
    List<Key> kList = new ArrayList<Key>();
    // create data and get key
    for (int i = 0; i < namespaceDat.length; i++) {
      NamespaceManager.set(namespaceDat[i]);
      Query q = new Query(kindTest);
      if (datastoreService.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
        entity = new Entity(kindTest);
        if (namespaceDat[i].equals("")) {
          entity.setProperty("jobType", "google");
        } else {
          entity.setProperty("jobType", namespaceDat[i]);
        }
        datastoreService.put(entity);
      } else {
        entity = datastoreService.prepare(q).asSingleEntity();
      }
      kList.add(entity.getKey());
    }
    // query in same namespace
    for (int i = 0; i < namespaceDat.length; i++) {
      NamespaceManager.set(namespaceDat[i]);
      Query q = new Query(kindTest);
      q.setFilter(new FilterPredicate("__key__", Query.FilterOperator.EQUAL, kList.get(i)));
      if (namespaceDat[i].equals("")) {
        assertEquals(datastoreService.prepare(q).asSingleEntity().getProperty("jobType"), 
                     "google");
      } else {
        assertEquals(datastoreService.prepare(q).asSingleEntity().getProperty("jobType"), 
                     namespaceDat[i]);
      }
    }
    // query in different namespace
    NamespaceManager.set(namespaceDat[1]);
    Query q = new Query(kindTest);
    q.setFilter(new FilterPredicate("__key__", Query.FilterOperator.EQUAL, kList.get(2)));

    thrown.expect(IllegalArgumentException.class);
    datastoreService.prepare(q).asSingleEntity();
  }
  
  // http://b/issue?id=2106725
  @Test
  public void testKeySerialization() throws EntityNotFoundException, IOException {
    Key parentKeyB = KeyFactory.createKey("family", "same");
    Key childKeyB = KeyFactory.createKey(parentKeyB, "children", "same");
    Entity entB1 = new Entity(childKeyB);
    datastoreService.put(entB1);

    Entity entB2 = datastoreService.get(childKeyB);
    assertEquals(new String(MemcacheSerialization.makePbKey(entB1.getKey())), 
                 new String(MemcacheSerialization.makePbKey(childKeyB)));
    assertEquals(new String(MemcacheSerialization.makePbKey(entB2.getKey())), 
                 new String(MemcacheSerialization.makePbKey(childKeyB)));
    datastoreService.delete(childKeyB);
    datastoreService.delete(parentKeyB);
  }
}

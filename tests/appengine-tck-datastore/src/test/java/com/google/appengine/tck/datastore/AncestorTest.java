package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * datastore ancestor test.
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class AncestorTest extends AbstractDatastoreTest {
  private static final String PARENTKIND = "school";
  private static final String CHILDKIND = "room";

  @Before
  public void createData() throws InterruptedException {
    Query q = new Query(PARENTKIND);
    if (datastoreService.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
      List<Entity> elist = new ArrayList<Entity>();
      Entity pRec, cRec;
      // add parents
      pRec = new Entity(PARENTKIND);
      pRec.setProperty("name", "redwood");
      Key key1 = datastoreService.put(pRec);
      pRec = new Entity(PARENTKIND);
      pRec.setProperty("name", "argonaut");
      Key key2 = datastoreService.put(pRec);
      // add children
      cRec = new Entity(CHILDKIND, key1);
      cRec.setProperty("teacher", "Mrs. Key1-redwood");
      elist.add(cRec);
      cRec = new Entity(CHILDKIND, key1);
      cRec.setProperty("teacher", "Mrs. Key2-redwood");
      elist.add(cRec);
      cRec = new Entity(CHILDKIND, key2);
      cRec.setProperty("teacher", "Mrs. Key1-argonaut");
      elist.add(cRec);
      cRec = new Entity(CHILDKIND, key2);
      cRec.setProperty("teacher", "Mrs. Key2-argonaut");
      elist.add(cRec);
      datastoreService.put(elist);
      Thread.sleep(waitTime);
    }
  }

  @Test
  public void testAncestor() {
    Key pKey = getParent().getKey();
    Query query = new Query(CHILDKIND, pKey);
    assertEquals(2, datastoreService.prepare(query)
                                    .countEntities(FetchOptions.Builder.withDefaults()));
    for (Entity cRec : datastoreService.prepare(query).asIterable()) {
      assertEquals(pKey, cRec.getParent());
    }
  }

  @Test
  public void testAncestorKey() {
    Key pKey = getParent().getKey();
    Query query = new Query(CHILDKIND, pKey);
    query.addSort("__key__");
    assertEquals(2, datastoreService.prepare(query)
                                    .countEntities(FetchOptions.Builder.withDefaults()));
    for (Entity cRec : datastoreService.prepare(query).asIterable()) {
      assertEquals(pKey, cRec.getParent());
    }
  }

  @Test
  public void testKindless() {
    Query query = new Query(PARENTKIND);
    query.setFilter(new FilterPredicate("name", Query.FilterOperator.EQUAL, "argonaut"));
    Entity parent = datastoreService.prepare(query).asSingleEntity();
    query = new Query(parent.getKey());
    assertEquals(3, datastoreService.prepare(query)
                                    .countEntities(FetchOptions.Builder.withDefaults()));
    query = new Query().setAncestor(parent.getKey());
    assertEquals(3, datastoreService.prepare(query)
                                    .countEntities(FetchOptions.Builder.withDefaults()));
  }

  @Test 
  public void testKeyName() {
    Entity pRec = new Entity(PARENTKIND, "测试文档keyname");
    pRec.setProperty("name", "regression");
    datastoreService.put(pRec);
    assertEquals(pRec.getKey().getName(), "测试文档keyname");

    Entity cRec = new Entity(CHILDKIND, "测试文档keyname", pRec.getKey());
    cRec.setProperty("teacher", "regression");
    datastoreService.put(cRec);
    assertEquals(cRec.getKey().getName(), "测试文档keyname");
    datastoreService.delete(cRec.getKey(), pRec.getKey());
  }

  private Entity getParent() {
    Query query = new Query(PARENTKIND);
    query.setFilter(new FilterPredicate("name", Query.FilterOperator.EQUAL, "argonaut"));
    Entity parent = datastoreService.prepare(query).asSingleEntity();
    return parent;
  }
}
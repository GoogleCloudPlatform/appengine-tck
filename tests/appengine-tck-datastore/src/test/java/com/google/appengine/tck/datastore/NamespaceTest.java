package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.utils.SystemProperty;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * datastore namespace test.
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class NamespaceTest extends DatastoreTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private String kindName = "namespaceType";
  private String[] namespaceDat = {"", "developerNS", "testingNS"};
  private String[] stringDat = {"google", "developer", "testing"};
  private int count = 3;

  @Before
  public void createData() throws InterruptedException {
    List<Entity> eList = new ArrayList<Entity>();
    for (int i = 0; i < namespaceDat.length; i++) {
      NamespaceManager.set(namespaceDat[i]);
      Query q = new Query(kindName);
      if (datastoreService.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
        for (int c = 0; c < count; c++) {
          Entity newRec = new Entity(kindName);
          newRec.setProperty("jobType", stringDat[i] + c);
          eList.add(newRec);
        }
      }
    }
    if (eList.size() > 0) {
      datastoreService.put(eList);
      Thread.sleep(waitTime);
    }
  }

  @Test
  public void testFilter() {
    for (int i = 0; i < namespaceDat.length; i++) {
      NamespaceManager.set(namespaceDat[i]);
      doAllFilters(kindName, "jobType", stringDat[i] + 1);
    }
  }

  @Test
  public void testSort() {
    for (String ns : namespaceDat) {
      NamespaceManager.set(ns);
      doSort(kindName, "jobType", 3, Query.SortDirection.ASCENDING);
      doSort(kindName, "jobType", 3, Query.SortDirection.DESCENDING);
    }
  }

  @Test
  public void testEntity() {
    for (String ns : namespaceDat) {
      NamespaceManager.set(ns);
      Query query = new Query(kindName);
      Entity readRec =  datastoreService.prepare(query).asIterator().next();
      assertEquals(ns, readRec.getNamespace());
      String appId = readRec.getAppId();
      appId = appId.substring(appId.indexOf("~") + 1);
      assertEquals(SystemProperty.applicationId.get(), appId);
      assertTrue(readRec.hasProperty("jobType"));
    }
  }

  @Test
  public void testQuery() {
    NamespaceManager.set("");
    Query query = new Query("__namespace__");
    int nsCount = datastoreService.prepare(query)
                  .countEntities(FetchOptions.Builder.withDefaults());
    assertTrue(nsCount > 0);
    String ns = "";
    for (Entity readRec : datastoreService.prepare(query).asIterable()) {
      ns = readRec.getKey().getName() + "," + ns; 
    }
    for (int i = 0; i < namespaceDat.length; i++) {
      if (!namespaceDat[i].equals("")) {
        assertTrue(ns.indexOf(namespaceDat[i]) >= 0);
      } else {
        assertTrue(ns.indexOf("null") >= 0);
      }
    }
  }

  @Test
  public void testValidation() {
    thrown.expect(IllegalArgumentException.class);
    NamespaceManager.set("abc#$%123");
  }

  @Test
  public void testDiffNamespace() {
    NamespaceManager.set(namespaceDat[1]);
    Query q = new Query(kindName);
    q.setFilter(new FilterPredicate("jobType", Query.FilterOperator.EQUAL, stringDat[2] + 1));
    int ttl = datastoreService.prepare(q).countEntities(FetchOptions.Builder.withDefaults());
    assertEquals(0, ttl);
  }

  @Override
  public void verifyFilter(String kind, String pName, Object fDat, 
      Query.FilterOperator operator, int rCont, boolean inChk) {
    Query query = new Query(kind);
    query.setFilter(new FilterPredicate(pName, operator, fDat));
    Object[] result = getResult(query, pName);
    assertEquals(rCont, result.length);
    if (inChk) {
      boolean find = false;
      for (Object data : result) {
        if (data.toString().equals(fDat.toString())) {
          find = true;
        }
      }
      assertEquals(true, find);
    }
  }

  @Override
  public void doSort(String kind, String pName, int expDat, Query.SortDirection direction) {
    Query query = new Query(kind);
    query.addSort(pName, direction);
    Object[] result = getResult(query, pName);
    assertEquals(expDat, result.length);
  }
}

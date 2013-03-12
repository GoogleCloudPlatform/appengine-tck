package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * datastore List data type test.
 * http://b/issue?id=1458158
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class ListTest extends AbstractDatastoreTest {
  private String kindName = "listData";
  private FetchOptions fo = FetchOptions.Builder.withDefaults();

  @Before
  public void createData() throws InterruptedException {
    Entity newRec;
    clearData(kindName);
    List<Entity> elist = new ArrayList<Entity>();
    newRec = new Entity(kindName, rootKey);
    newRec.setProperty("stringData", Arrays.asList("abc", "xyz", "mno"));
    newRec.setProperty("intData1", Arrays.asList(0, 55, 99));
    elist.add(newRec);
    
    newRec = new Entity(kindName, rootKey);
    newRec.setProperty("stringData", Arrays.asList("ppp", "kkk", "ddd"));
    newRec.setProperty("intData1", Arrays.asList(1, 10, null));
    elist.add(newRec);
  
    newRec = new Entity(kindName, rootKey);
    newRec.setProperty("stringData", Arrays.asList("hannah", "luoluo", "jia"));
    newRec.setProperty("intData1", Arrays.asList(28, 15, 23));
    elist.add(newRec);
    datastoreService.put(elist);
    Thread.sleep(waitTime);
  }

  @Test 
  public void testStrFilter() {
    Query q = new Query(kindName, rootKey);
    q.setFilter(new FilterPredicate("stringData", Query.FilterOperator.LESS_THAN, "qqq"));
    q.setFilter(new FilterPredicate("stringData", Query.FilterOperator.GREATER_THAN, "mmm"));
    q.addSort("stringData", Query.SortDirection.ASCENDING);
    assertEquals(2,  datastoreService.prepare(q).countEntities(fo));
    List<Entity> elist = datastoreService.prepare(q).asList(fo);
    assertEquals(Arrays.asList("abc", "xyz", "mno"), elist.get(0).getProperty("stringData"));
    assertEquals(Arrays.asList("ppp", "kkk", "ddd"), elist.get(1).getProperty("stringData"));
  }

  @Test 
  public void testIntFilter() {
    Query q = new Query(kindName, rootKey);
    q.setFilter(new FilterPredicate("intData1", Query.FilterOperator.LESS_THAN, 20));
    q.setFilter(new FilterPredicate("intData1", Query.FilterOperator.GREATER_THAN, 1));
    q.setFilter(new FilterPredicate("intData1", Query.FilterOperator.EQUAL, null));
    q.addSort("intData1", Query.SortDirection.ASCENDING);
    assertEquals(1,  datastoreService.prepare(q).countEntities(fo));
    List<Entity> elist = datastoreService.prepare(q).asList(fo);
    assertEquals(Arrays.asList(1L, 10L, null), elist.get(0).getProperty("intData1"));
  }
}

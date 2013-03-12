package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * datastore batch process test.
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class BatchTest extends DatastoreTestBase {
  private FetchOptions fo = FetchOptions.Builder.withDefaults();
  private String kindName = "batchType";
  private String bigStr = getBigString(250000);
  private int bigCount = 1005;
  private int bigNum = 4;
  private int limit = 1003;
  private int offset = 1002;

  private static String getBigString(int len) {
    char[] chars = new char[len];
    for (int i = 0; i < len; i++) chars[i]='x';
    return new String(chars);
  }

  /*
   * Prepare large testing data.
   * - 1005 entities and 4 big entities (250k) included.
   */
  @Before
  public void createData() throws InterruptedException {
    Query query = new Query(kindName, rootKey);
    if (datastoreService.prepare(query).countEntities(fo) == 0) {
      List<Entity> elist = new ArrayList<Entity>();
      for (int i = 0; i < (bigCount - bigNum); i++) {
        Entity newRec = new Entity(kindName, rootKey);
        newRec.setProperty("count", i);
        newRec.setProperty("desc", new Text("small"));
        elist.add(newRec);
      }
      if (bigNum > 0) {
        Text text = new Text(bigStr);
        for (int i = 0; i < bigNum; i++) {
          Entity newRec = new Entity(kindName, rootKey);
          newRec.setProperty("count", bigCount - bigNum + i);
          newRec.setProperty("desc", text);
          elist.add(newRec);
        }
      }
      datastoreService.put(elist);
      Thread.sleep(waitTime);
    }
  }
  
  @Test
  public void testStep1GetCount() {
    Query q = new Query(kindName, rootKey);
    assertEquals(bigCount, datastoreService.prepare(q).countEntities(fo));
  }

  @Test
  public void testStep2BigAsList() {
    Query q = new Query(kindName, rootKey);
    q.addSort("count", Query.SortDirection.DESCENDING);
    List<Entity> eData = datastoreService.prepare(q).asList(fo);
    assertEquals(bigCount, eData.size());
    assertEquals(new Integer(bigCount - 1).longValue(), eData.get(0).getProperty("count"));
  }

  @Test
  public void testStep3ListWithOption() {
    FetchOptions foList = FetchOptions.Builder.withLimit(limit);
    Query q = new Query(kindName, rootKey);
    List<Entity> eData = datastoreService.prepare(q).asList(foList);
    assertEquals(limit, eData.size());
    foList = FetchOptions.Builder.withOffset(offset);
    eData = datastoreService.prepare(q).asList(foList);
    assertEquals(bigCount - offset, eData.size());
  }

  @Test
  public void testStep4BigAsIterator() {
    Query q = new Query(kindName, rootKey);
    q.setFilter(new FilterPredicate("count", FilterOperator.LESS_THAN, bigCount));
    Iterator<Entity> eData = datastoreService.prepare(q).asIterator(fo);
    assertEquals(bigCount, getSize(eData));
  }

  @Test
  public void testStep5IteratorWithOption() {
    FetchOptions foIterator = FetchOptions.Builder.withLimit(limit);
    Query q = new Query(kindName, rootKey);
    Iterator<Entity> eData = datastoreService.prepare(q).asIterator(foIterator);
    assertEquals(limit, getSize(eData));
    foIterator = FetchOptions.Builder.withOffset(offset);
    eData = datastoreService.prepare(q).asIterator(foIterator);
    assertEquals(bigCount - offset, getSize(eData));
  }

  @Test
  public void testStep6BigAsIterable() {
    Query q = new Query(kindName, rootKey).addSort("count", Query.SortDirection.ASCENDING);
    Iterator<Entity> eData = datastoreService.prepare(q).asIterable(fo).iterator();
    assertEquals(bigCount, getSize(eData));
  }

  @Test
  public void testStep7IterableWithOption() {
    FetchOptions foIterable = FetchOptions.Builder.withLimit(limit);
    Query q = new Query(kindName, rootKey);
    Iterator<Entity> eData = datastoreService.prepare(q).asIterator(fo.limit(limit));
    assertEquals(limit, getSize(eData));
    foIterable = FetchOptions.Builder.withOffset(offset);
    eData = datastoreService.prepare(q).asIterator(foIterable);
    assertEquals(bigCount - offset, getSize(eData));
  }

  private int getSize(Iterator<Entity> eData) {
    int i = 0;
    while (eData.hasNext()) {
      eData.next();
      i++;
    }
    return i;
  }

  @Test
  public void testStep8FetchOption() {
    Query q = new Query(kindName, rootKey).addSort("count", Query.SortDirection.DESCENDING);
    Entity e = datastoreService.prepare(q).asIterator().next();
    assertEquals(new Integer(bigCount - 1).longValue(), e.getProperty("count"));

    FetchOptions foTest = FetchOptions.Builder.withDefaults();
    int ttl = datastoreService.prepare(q).countEntities(foTest.limit(500));
    assertEquals(500, ttl);

    foTest = FetchOptions.Builder.withDefaults();
    ttl = datastoreService.prepare(q).countEntities(foTest.offset(150));
    assertEquals((bigCount - 150), ttl);

    fo = FetchOptions.Builder.withDefaults();
    ttl = datastoreService.prepare(q).countEntities(foTest.offset(50).limit(150));
    assertEquals(150, ttl);

    fo = FetchOptions.Builder.withDefaults();
    ttl = datastoreService.prepare(q).countEntities(foTest.limit(150).offset(offset));
    int expect = (150 < (bigCount - offset)) ? 150 : (bigCount - offset);
    assertEquals(expect , ttl);
  }

  @Test
  public void testStep9BigFilterIn() {
    int filterNum = 500;
    Query q = new Query(kindName, rootKey);
    q.setFilter(new FilterPredicate("count", FilterOperator.IN, getFilterIn(filterNum)));
    FetchOptions fo = FetchOptions.Builder.withDefaults();
    int ttl = datastoreService.prepare(q).countEntities(fo);
    assertEquals(filterNum, ttl);
  }

  @Test
  public void testStep10FilterInWithOption() {
    int filterNum = 100;
    Query q = new Query(kindName, rootKey);
    q.setFilter(new FilterPredicate("count", FilterOperator.IN, getFilterIn(filterNum)));
    int ttl = datastoreService.prepare(q).countEntities(fo.offset(filterNum / 2));
    assertEquals((filterNum / 2), ttl);
  }

  private List<Integer> getFilterIn(int num) {
    List<Integer> inFilter = new ArrayList<Integer>();
    for (int i = 0; i < num; i++) inFilter.add(i);
    return inFilter;
  }

  @Test
  public void testStep11Limit() {
    String str1mb = getBigString(1000000);
    Text text = new Text(str1mb);
    List<Entity> elist = new ArrayList<Entity>();
    for (int i = 0; i < 32; i++) {
      Entity newRec = new Entity(kindName, rootKey);
      newRec.setProperty("count", 2000 + i);
      newRec.setProperty("desc", text);
      elist.add(newRec);
    }
    List<Key> eKeys = datastoreService.put(elist);
    assertEquals(32, eKeys.size());
  }

  @Test
  public void testStep12BigDelete() throws InterruptedException {
    clearData(kindName);
    Query q = new Query(kindName, rootKey);
    int ttl = datastoreService.prepare(q).countEntities(fo);
    assertEquals(0, ttl);
  }
}

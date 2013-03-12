package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Projection;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * datastore Index only query test. http://go/indexonlyqueries
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class IndexQueryTest extends DatastoreTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private String kindName = "indexquery";
  private FetchOptions fetchOption = FetchOptions.Builder.withDefaults();
  private int count = 10;

  @Before
  public void addData() throws InterruptedException {
    Query query = new Query(kindName, rootKey);
    if (datastoreService.prepare(query).countEntities(fetchOption) == 0) {
      List<Entity> elist = new ArrayList<Entity>();
      for (int i = 0; i < count; i++) {
        Entity newRec = new Entity(kindName, rootKey);
        newRec.setProperty("stringData", "string test data " + i);
        newRec.setProperty("intData", 10 * i);
        newRec.setProperty("stringList", Arrays.asList("abc" + i, "xyz" + i, "abc" + i));
        newRec.setProperty("intList", Arrays.asList(i, 50 + i, 90 + i));
        newRec.setProperty("timestamp", new Date());
        newRec.setProperty("floatData", new Float(i + 0.1));
        newRec.setProperty("ratingData", new Rating(i + 20));
        newRec.setProperty("booleanData", new Boolean("True"));
        newRec.setProperty("geoptData",
                           new GeoPt(new Float(i * 20 - 90), new Float(i * 30 - 179.1)));
        newRec.setProperty("byteStrProp", new ShortBlob(("shortblob" + (i * 30)).getBytes()));
        elist.add(newRec);
      }
      datastoreService.put(elist);
      Thread.sleep(waitTime);
    }
  }

  @Test
  public void testProjection() {
    List<Projection> ppList = new ArrayList<Projection>();
    Query query = new Query(kindName, rootKey);
    Projection pp = new PropertyProjection("stringData", String.class);
    assertEquals("stringData", pp.getName());
    query.addProjection(pp);
    assertEquals(1, query.getProjections().size());
    ppList.add(pp);
    pp = new PropertyProjection("intData", Integer.class);
    query.addProjection(pp);
    ppList.add(pp);
    for (Projection p : query.getProjections()) {
      String pName = p.getName();
      assertTrue(pName.endsWith("stringData") || pName.endsWith("intData"));
    }
  }

  @Test
  public void testBasicQuery() {
    Query query = new Query(kindName, rootKey);
    query.addProjection(new PropertyProjection("stringData", String.class));
    query.addProjection(new PropertyProjection("intData", Integer.class));
    String sql = "SELECT intData, stringData FROM " + kindName
        + " WHERE __ancestor__ is " + rootKey;
    assertEquals(sql.toLowerCase(), query.toString().toLowerCase());
    List<Entity> results = datastoreService.prepare(query).asList(fetchOption);
    assertEquals(count, results.size());
    for (Entity e : results) {
      assertEquals(2, e.getProperties().size());
      assertTrue(e.getProperties().containsKey("stringData"));
      assertTrue(e.getProperties().containsKey("intData"));
    }
  }

  @Test
  public void testListQuery() {
    Query query = new Query(kindName, rootKey);
    query.addProjection(new PropertyProjection("stringList", String.class));
    query.addProjection(new PropertyProjection("intList", Integer.class));
    List<Entity> results = datastoreService.prepare(query).asList(fetchOption);
    // Distinct stringList data 2 * Distinct intList data 3 * entity's count 10
    assertEquals(60, results.size());
    Entity e = results.get(0);
    assertEquals(2, e.getProperties().size());
    assertTrue(e.getProperties().containsKey("stringList"));
    assertTrue(e.getProperties().containsKey("intList"));
  }

  @Test
  public void testQueryOrder() {
    Query query = new Query(kindName, rootKey);
    query.addProjection(new PropertyProjection("intData", Integer.class));
    query.addSort("stringData", Query.SortDirection.DESCENDING);
    List<Entity> results = datastoreService.prepare(query).asList(fetchOption);
    assertEquals(count, results.size());
    int first = new Integer(results.get(0).getProperty("intData").toString());
    int last = new Integer(results.get(count - 1).getProperty("intData").toString());
    assertTrue(first > last);
  }

  @Test
  public void testQueryFilter() {
    Query query = new Query(kindName, rootKey);
    query.addProjection(new PropertyProjection("stringData", String.class));
    query.setFilter(new FilterPredicate("intData", FilterOperator.NOT_EQUAL, 50));
    query.addSort("intData");
    List<Entity> results = datastoreService.prepare(query).asList(fetchOption);
    assertEquals(count - 1, results.size());
    for (Entity e : results) {
      assertTrue(e.getProperty("stringData").toString().indexOf("5") == -1);
    }
  }

  @Test
  public void testQueryFloadType() {
    checkQueryType("floatData", Float.class);
  }

  @Test
  public void testQueryRatingType() {
    checkQueryType("ratingData", Rating.class);
  }

  @Test
  public void testQueryBooleanType() {
    checkQueryType("booleanData", Boolean.class);
  }

  @Test
  public void testQueryGeoPtType() {
    checkQueryType("geoptData", GeoPt.class);
  }

  @Test
  public void testWrongType() {
    Query query = new Query(kindName, rootKey);
    query.addProjection(new PropertyProjection("stringData", Integer.class));
    List<Entity> results = datastoreService.prepare(query).asList(fetchOption);
    thrown.expect(IllegalArgumentException.class);
    for (Entity e : results) {
      // fetch data
    }
  }
  
  @Test
  public void testLimit() {
    checkQueryWithLimit(5);
  }

  @Test
  public void testLimitZero() {
    checkQueryWithLimit(0);
  }

  @Test
  public void testCount() {
    Query query = new Query(kindName, rootKey);
    query.addProjection(new PropertyProjection("stringData", String.class));
    assertEquals(count, datastoreService.prepare(query).countEntities(fetchOption));
  }

  private void checkQueryType(String property, Class<?> type) {
    Query query = new Query(kindName, rootKey);
    query.addProjection(new PropertyProjection(property, type));
    String sql = "SELECT " + property + " FROM " + kindName + " WHERE __ancestor__ is " + rootKey;
    assertEquals(sql.toLowerCase(), query.toString().toLowerCase());
    List<Entity> results = datastoreService.prepare(query).asList(fetchOption);
    for (Entity e : results) {
      assertEquals(1, e.getProperties().size());
      assertTrue(e.getProperties().containsKey(property));
    }
  }

  private void checkQueryWithLimit(int limit) {
    FetchOptions fo = FetchOptions.Builder.withLimit(limit);
    Query query = new Query(kindName, rootKey);
    query.addProjection(new PropertyProjection("stringData", String.class));
    List<Entity> results = datastoreService.prepare(query).asList(fo);
    assertEquals(limit, results.size());
  }
}

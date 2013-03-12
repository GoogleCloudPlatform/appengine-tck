package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * cursor test.
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class CursorTest extends DatastoreTestBase {
  private String kindName = "cursorType";
  private String[] testDat = {"aa", "bb", "cc", "dd", "ee", "ff", "gg", "hh", "ii", "jj"};
  private int total = 100;

  @Before
  public void createData() throws InterruptedException {
    Query query = new Query(kindName, rootKey);
    if (datastoreService.prepare(query).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
      List<Entity> eList = new ArrayList<Entity>();
      Entity newRec;
      for (int i = 0; i < total; i++) {
        newRec = new Entity(kindName, rootKey);
        newRec.setProperty("count", i);
        newRec.setProperty("name", testDat[(i) % 10]);
        newRec.setProperty("created", new Date());
        eList.add(newRec);
      }
      datastoreService.put(eList);
      Thread.sleep(waitTime);
    }
  }

  @Test
  public void testFilter() {
    int onePage = 5;
    String filterData = "ff";
    Query query = new Query(kindName, rootKey);
    query.setFilter(new FilterPredicate("name", Query.FilterOperator.EQUAL, filterData));
    // fetch first page
    Cursor cursor = checkPage(query, null, null, onePage, onePage, filterData, filterData);
    Cursor decodedCursor = Cursor.fromWebSafeString(cursor.toWebSafeString());
    // fetch next page
    checkPage(query, decodedCursor, null, onePage, onePage, filterData, filterData);
  }

  @Test
  public void testSort() {
    int onePage = 6;
    Query query = new Query(kindName, rootKey);
    query.addSort("name", Query.SortDirection.ASCENDING);
    // fetch first page   aa,aa,aa,aa,aa,aa
    Cursor cursor = checkPage(query, null, null, onePage, onePage, testDat[0], testDat[0]);
    Cursor decodedCursor = Cursor.fromWebSafeString(cursor.toWebSafeString());
    // fetch next page    aa,aa,aa,aa,bb,bb
    checkPage(query, decodedCursor, null, onePage, onePage, testDat[0], testDat[1]);

    // desc
    onePage = total / testDat.length;
    query = new Query(kindName, rootKey);
    query.addSort("name", Query.SortDirection.DESCENDING);
    // fetch first page   jj,jj,........,jj,jj
    String chkChar = testDat[testDat.length - 1];
    cursor = checkPage(query, null, null, onePage, onePage, chkChar, chkChar);
    decodedCursor = Cursor.fromWebSafeString(cursor.toWebSafeString());
    // fetch next page   ii,ii,........,ii,ii
    chkChar = testDat[testDat.length - 2];
    checkPage(query, decodedCursor, null, onePage, onePage, chkChar, chkChar);
  }

  @Test
  public void testEndFetch() {
    int onePage = total - 30;
    Query query = new Query(kindName, rootKey);
    // fetch first page
    Cursor cursor = checkPage(query, null, null, onePage, onePage, null, null);
    Cursor decodedCursor = Cursor.fromWebSafeString(cursor.toWebSafeString());
    // fetch next page,   get remaining after 1st page.
    checkPage(query, decodedCursor, null, onePage, total - onePage, null, null);
  }

  @Test
  public void testEndCursor() {
    int limit = total / testDat.length;
    Query query = new Query(kindName, rootKey);
    query.addSort("name", Query.SortDirection.ASCENDING);
    // fetch 1st page
    Cursor cursor = checkPage(query, null, null, limit, limit, testDat[0], testDat[0]);
    Cursor decodedCursor = Cursor.fromWebSafeString(cursor.toWebSafeString());
    // fetch 1st page again since using decodedCursor as end cursor     
    checkPage(query, null, decodedCursor, limit, limit, testDat[0], testDat[0]);
  }

  @Test
  public void testStartEndCursor() {
    int limit = total / testDat.length;
    Query query = new Query(kindName, rootKey);
    query.addSort("name", Query.SortDirection.ASCENDING);
    FetchOptions fetchOption = FetchOptions.Builder.withLimit(limit);
    // fetch 1st page and get cursor1
    QueryResultList<Entity> nextBatch = datastoreService.prepare(query)
                                                       .asQueryResultList(fetchOption);
    Cursor cursor1 = Cursor.fromWebSafeString(nextBatch.getCursor().toWebSafeString());
    // fetch 2nd page and get cursor2
    nextBatch = datastoreService.prepare(query).asQueryResultList(fetchOption.startCursor(cursor1));
    Cursor cursor2 = Cursor.fromWebSafeString(nextBatch.getCursor().toWebSafeString()); 
    // cursor1 as start and cursor2 as end and 15 in limit -- -- should return 2nd page.
    checkPage(query, cursor1, cursor2, limit, limit, testDat[1], testDat[1]);
    // cursor1 as start and cursor2 as end and 30 in limit -- should return 2nd page.
    checkPage(query, cursor1, cursor2, 2 * limit, limit, testDat[1], testDat[1]);
    // cursor2 as start and cursor1 as end and 15 in limit -- should not return any.
    checkPage(query, cursor2, cursor1, limit, 0, null, null);
  }

  private Cursor checkPage(Query query, Cursor stCursor, Cursor endCursor, int limit, int exptRet, 
      String chkSt, String chkEnd) {
    FetchOptions fetchOption = FetchOptions.Builder.withLimit(limit);
    if (stCursor != null) {
      fetchOption = fetchOption.startCursor(stCursor);
    }
    if (endCursor != null) {
      fetchOption = fetchOption.endCursor(endCursor);
    }
    QueryResultList<Entity> nextBatch = datastoreService.prepare(query)
                                                        .asQueryResultList(fetchOption);
    assertEquals(exptRet, nextBatch.size());
    if (chkSt != null) {
      assertEquals(chkSt, nextBatch.get(0).getProperty("name"));
    }
    if (chkEnd != null) {
      assertEquals(chkEnd, nextBatch.get(nextBatch.size() - 1).getProperty("name"));
    }
    return nextBatch.getCursor();
  }
}
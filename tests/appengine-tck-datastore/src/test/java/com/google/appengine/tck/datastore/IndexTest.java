package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Index.IndexState;
import com.google.appengine.api.datastore.Index.Property;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Map;

/**
 * datastore Index test.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class IndexTest extends AbstractDatastoreTest {

  // the index exist already.  This is just for showing detail.
  @Before
  public void addIndex() {
    Query query = new Query("indextest2");
    for (Entity readRec : datastoreService.prepare(query).asIterable()) {
      datastoreService.delete(readRec.getKey());
    }
    Entity newRec = new Entity("indextest2");
    newRec.setProperty("stringData", "测试文档资料data");
    newRec.setProperty("age", 9);
    newRec.setProperty("timestamp", new Date());
    datastoreService.put(newRec);

    query = new Query("indextest2");
    query.setFilter(new FilterPredicate("stringData", Query.FilterOperator.LESS_THAN, "bdefgh"));
    query.addSort("stringData", Query.SortDirection.DESCENDING);
    query.addSort("age");
    for (Entity readRec : datastoreService.prepare(query).asIterable()) {
      // fetch entities for index creation.
    }
    datastoreService.prepare(query).asIterable();
  }

  @Test
  public void testGetIndex() {
    boolean exist = false;

    Map<Index, IndexState> indexes = datastoreService.getIndexes();
    assertTrue(indexes.size() > 0);
    for (Map.Entry<Index, IndexState> entry: indexes.entrySet()) {
      Index index = entry.getKey();
      if (index.getKind().equals("indextest2")) {
        exist = true;
        assertEquals(Index.IndexState.SERVING, entry.getValue());
        for (Property property : index.getProperties()) {
          if (property.getName().equals("stringData")) {
            assertEquals(Query.SortDirection.DESCENDING, property.getDirection());
          } else if (property.getName().equals("age")) {
            assertEquals(Query.SortDirection.ASCENDING, property.getDirection());
          }
        }
      }
    }
    assertTrue(exist);
  }
}

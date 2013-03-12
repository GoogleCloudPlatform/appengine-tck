package com.google.appengine.tck.datastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * datastore datetime data type test.
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class DatetimeDataTest extends AbstractDatastoreTest {
  private String kindName = "datetimeType";
  private DateFormat dfDateTime = new SimpleDateFormat("yyyy,M,d,k,m,s");

  @Before
  public void createData() throws InterruptedException, ParseException {
    Date[] testDatas = {dfDateTime.parse("2001,1,1,23,59,59"), 
        dfDateTime.parse("2005,5,5,13,19,19"), dfDateTime.parse("2008,8,8,3,9,9")};

    Query q = new Query(kindName, rootKey);
    if (datastoreService.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
      List<Entity> elist = new ArrayList<Entity>();
      for (Date data : testDatas) {
        Entity newRec = new Entity(kindName, rootKey);
        newRec.setProperty(propertyName, data);
        elist.add(newRec);
      }
      datastoreService.put(elist);
      Thread.sleep(waitTime);
    }
  }

  @Test
  public void testFilter() throws Exception {
    doAllFilters(kindName, propertyName, dfDateTime.parse("2005,5,5,13,19,19"));
  }

  @Test
  public void testSort() throws Exception {
    doSort(kindName, propertyName, dfDateTime.parse("2001,1,1,23,59,59"), 
        Query.SortDirection.ASCENDING);
    doSort(kindName, propertyName, dfDateTime.parse("2008,8,8,3,9,9"), 
        Query.SortDirection.DESCENDING);
  }
}

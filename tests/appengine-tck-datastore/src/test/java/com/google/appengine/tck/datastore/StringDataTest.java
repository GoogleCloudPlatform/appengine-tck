package com.google.appengine.tck.datastore;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * datastore string data type test.
 *  
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class StringDataTest extends DatastoreTestBase {
  private static final String kindName = "stringType";
  
  @Before
  public void createData() throws InterruptedException {
    Entity newRec;
    String[] stringDat = {"abc", "xyz", "mno"};
    PhoneNumber[] phoneDat = {new PhoneNumber("408-123-4567"), new PhoneNumber("650-321-7654"),
        new PhoneNumber("408-987-6543")};
    PostalAddress[] addressDat = {new PostalAddress("123 Google Rd. CA 12345"),
        new PostalAddress("19451 Via Monte Rd. CA95070"), new PostalAddress("9 1st St. CA 95000")};
    Email[] emailDat = {new Email("somebody@google.com"), new Email("somebody2@gmail.com"),
        new Email("somebody3@hotmail.com")};
    Link[] linkDat = {new Link("http://www.hotmail.com"), new Link("http://www.google.com.com"), 
        new Link("http://www.gmail.com")};
    Category[] categoryDat = {new Category("developer"), new Category("test"), 
        new Category("manager")};
    Text[] textDat = {new Text("english"), new Text("chinese"), new Text("japanese")};
    ShortBlob[] byteString = {new ShortBlob("shortblob".getBytes()), 
        new ShortBlob("shortText".getBytes()), new ShortBlob("shortImage".getBytes())};

    Query q = new Query(kindName, rootKey);
    if (datastoreService.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
      List<Entity> elist = new ArrayList<Entity>();
      for (int i = 0; i < 3; i++) {
        newRec = new Entity(kindName, rootKey);
        newRec.setProperty("stringProp", stringDat[i]);
        newRec.setProperty("phoneProp", phoneDat[i]);
        newRec.setProperty("addressProp", addressDat[i]);
        newRec.setProperty("emailProp", emailDat[i]);
        newRec.setProperty("linkProp", linkDat[i]);
        newRec.setProperty("categoryProp", categoryDat[i]);
        newRec.setProperty("textProp", textDat[i]);
        newRec.setProperty("byteStrProp", byteString[i]);
        elist.add(newRec);
      }
      datastoreService.put(elist);
      Thread.sleep(waitTime);
    } 
  }
  
  @Test
  public void testFilter() {
    doAllFilters(kindName, "stringProp", "mno");
    doEqOnlyFilter(kindName, "phoneProp", new PhoneNumber("650-253-5017"));
    doEqOnlyFilter(kindName, "addressProp", new PostalAddress("19451 Via Monte Rr. CA95070"));
    doEqOnlyFilter(kindName, "emailProp", new Email("hchen@google.com"));
    doEqOnlyFilter(kindName, "linkProp", new Link("http://www.google.com.com"));
    doEqOnlyFilter(kindName, "categoryProp", new Category("test"));
    doEqOnlyFilter(kindName, "byteStrProp", new ShortBlob("shortText".getBytes()));
    String[] inDat = {"abc", "xyz"};
    doInFilter(kindName, "stringProp", inDat);
  }

  @Test
  public void testSort() {
    String[] expData = {"abc"};
    doSort(kindName, "stringProp", "abc", Query.SortDirection.ASCENDING);
    doSort(kindName, "stringProp", "xyz", Query.SortDirection.DESCENDING);
  }
  
  protected void doInFilter(String kind, String pName, String[] inDat) {
    Query query = new Query(kind, rootKey);
    query.setFilter(new FilterPredicate(pName, Query.FilterOperator.IN, Arrays.asList(inDat)));
    Object[] result = getResult(query, pName);
    assertEquals(inDat.length, result.length);
    assertArrayEquals(inDat, result);
  }
}

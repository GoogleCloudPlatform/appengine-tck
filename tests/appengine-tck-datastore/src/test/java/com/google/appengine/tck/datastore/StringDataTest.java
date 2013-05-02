package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * datastore string data type test.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class StringDataTest extends DatastoreTestBase {
    private static final String kindName = "stringType";
    private FetchOptions fo = FetchOptions.Builder.withDefaults();

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
        if (service.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
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
            service.put(elist);
            sync(waitTime);
        }
    }

    @Test
    public void testFilter() {
        doAllFilters(kindName, "stringProp", "mno");
        doEqOnlyFilter(kindName, "phoneProp", new PhoneNumber("650-321-7654"));
        doEqOnlyFilter(kindName, "addressProp", new PostalAddress("19451 Via Monte Rd. CA95070"));
        doEqOnlyFilter(kindName, "emailProp", new Email("somebody2@gmail.com"));
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
  
    @Test
    public void testPhoneNumType() {
        String propertyName = "phoneProp";
        List<Entity> elist = doQuery(kindName, propertyName, PhoneNumber.class, true);
        PhoneNumber phonenum = (PhoneNumber) elist.get(0).getProperty(propertyName);
        PhoneNumber sameDat = (PhoneNumber) elist.get(0).getProperty(propertyName);
        PhoneNumber diffDat = (PhoneNumber) elist.get(1).getProperty(propertyName);
        assertTrue(phonenum.equals(sameDat));
        assertFalse(phonenum.equals(diffDat));
        assertEquals("408-123-4567", phonenum.getNumber());
        assertEquals(0, phonenum.compareTo(sameDat));
        assertTrue(phonenum.compareTo(diffDat) != 0);
        assertEquals(phonenum.hashCode(), phonenum.hashCode());
    }
  
    @Test
    public void testPostalAddrType() {
        String propertyName = "addressProp";
        List<Entity> elist = doQuery(kindName, propertyName, PostalAddress.class, true);
        PostalAddress postaladdr = (PostalAddress) elist.get(0).getProperty(propertyName);
        PostalAddress sameDat = (PostalAddress) elist.get(0).getProperty(propertyName);
        PostalAddress diffDat = (PostalAddress) elist.get(1).getProperty(propertyName);
        assertTrue(postaladdr.equals(sameDat));
        assertFalse(postaladdr.equals(diffDat));
        assertEquals("123 Google Rd. CA 12345", postaladdr.getAddress());
        assertEquals(0, postaladdr.compareTo(sameDat));
        assertTrue(postaladdr.compareTo(diffDat) != 0);
        assertEquals(postaladdr.hashCode(), postaladdr.hashCode());
    }
    
    @Test
    public void testEmailType() {
        String propertyName = "emailProp";
        List<Entity> elist = doQuery(kindName, propertyName, Email.class, true);
        Email email = (Email) elist.get(0).getProperty(propertyName);
        Email sameDat = (Email) elist.get(0).getProperty(propertyName);
        Email diffDat = (Email) elist.get(1).getProperty(propertyName);
        assertTrue(email.equals(sameDat));
        assertFalse(email.equals(diffDat));
        assertEquals("somebody2@gmail.com", email.getEmail());
        assertEquals(0, email.compareTo(sameDat));
        assertTrue(email.compareTo(diffDat) != 0);
        assertEquals(email.hashCode(), email.hashCode());
    }
  
    @Test
    public void testLinkType() {
        String propertyName = "linkProp";
        List<Entity> elist = doQuery(kindName, propertyName, Link.class, true);
        Link link = (Link) elist.get(0).getProperty(propertyName);
        Link sameDat = (Link) elist.get(0).getProperty(propertyName);
        Link diffDat = (Link) elist.get(1).getProperty(propertyName);
        assertTrue(link.equals(sameDat));
        assertFalse(link.equals(diffDat));
        assertEquals("http://www.gmail.com", link.getValue());
        assertEquals(0, link.compareTo(sameDat));
        assertTrue(link.compareTo(diffDat) != 0);
        assertEquals(link.hashCode(), link.hashCode());
    }
  
    @Test
    public void testCategoryType() {
        String propertyName = "categoryProp";
        List<Entity> elist = doQuery(kindName, propertyName, Category.class, true);
        Category Category = (Category) elist.get(0).getProperty(propertyName);
        Category sameDat = (Category) elist.get(0).getProperty(propertyName);
        Category diffDat = (Category) elist.get(1).getProperty(propertyName);
        assertTrue(Category.equals(sameDat));
        assertFalse(Category.equals(diffDat));
        assertEquals("developer", Category.getCategory());
        assertEquals(0, Category.compareTo(sameDat));
        assertTrue(Category.compareTo(diffDat) != 0);
        assertEquals(Category.hashCode(), Category.hashCode());
    }
  
    @Test
    public void testShortBlobType() {
        String propertyName = "byteStrProp";
        List<Entity> elist = doQuery(kindName, propertyName, ShortBlob.class, true);
        ShortBlob shortblob = (ShortBlob) elist.get(0).getProperty(propertyName);
        ShortBlob sameDat = (ShortBlob) elist.get(0).getProperty(propertyName);
        ShortBlob diffDat = (ShortBlob) elist.get(1).getProperty(propertyName);
        assertTrue(shortblob.equals(sameDat));
        assertFalse(shortblob.equals(diffDat));
        Arrays.equals("shortblob".getBytes(), shortblob.getBytes());
        assertEquals(0, shortblob.compareTo(sameDat));
        assertTrue(shortblob.compareTo(diffDat) != 0);
        assertEquals(shortblob.hashCode(), shortblob.hashCode());
    }
  
    @Test
    public void testTextType() {
        String propertyName = "textProp";
        List<Entity> elist = doQuery(kindName, propertyName, null, false);
        Text text = (Text) elist.get(0).getProperty(propertyName);
        Text sameDat = (Text) elist.get(0).getProperty(propertyName);
        Text diffDat = (Text) elist.get(1).getProperty(propertyName);
        assertTrue(text.equals(sameDat));
        assertFalse(text.equals(diffDat));
        String getText = text.getValue();
        assertTrue(getText.equals("english") || getText.equals("chinese")
            || getText.equals("japanese"));
        assertEquals(text.hashCode(), text.hashCode());
    }

    protected void doInFilter(String kind, String pName, String[] inDat) {
        Query query = new Query(kind, rootKey);
        query.setFilter(new FilterPredicate(pName, Query.FilterOperator.IN, Arrays.asList(inDat)));
        Object[] result = getResult(query, pName);
        assertEquals(inDat.length, result.length);
        assertArrayEquals(inDat, result);
    }
}

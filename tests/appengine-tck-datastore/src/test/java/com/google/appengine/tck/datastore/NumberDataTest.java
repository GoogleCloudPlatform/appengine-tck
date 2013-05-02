package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Rating;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * datastore number data type test.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class NumberDataTest extends DatastoreTestBase {
    private String kindName = "numberType";

    @Before
    public void createData() throws InterruptedException {
        Entity newRec;
        Integer[] intDat = {456, 987, 123};
        Long[] longDat = {Long.MAX_VALUE, new Long(456), Long.MIN_VALUE};
        Double[] doubleDat = {456.456, 123.123, 987.987};
        Short[] shortDat = {Short.MIN_VALUE, Short.MAX_VALUE, 456};
        Float[] floatDat = {Float.MAX_VALUE, Float.MIN_VALUE, new Float(456.456)};
        Rating[] ratingDat = {new Rating(11), new Rating(55), new Rating(99)};

        Query q = new Query(kindName, rootKey);
        if (service.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
            List<Entity> eList = new ArrayList<Entity>();
            for (int i = 0; i < 3; i++) {
                newRec = new Entity(kindName, rootKey);
                newRec.setProperty("intProp", intDat[i]);
                newRec.setProperty("longProp", longDat[i]);
                newRec.setProperty("doubleProp", doubleDat[i]);
                newRec.setProperty("shortProp", shortDat[i]);
                newRec.setProperty("floatProp", floatDat[i]);
                newRec.setProperty("ratingProp", ratingDat[i]);
                eList.add(newRec);
            }
            service.put(eList);
            sync(waitTime);
        }
    }

    @Test
    public void testFilter() throws Exception {
        doAllFilters(kindName, "intProp", 456);
        doAllFilters(kindName, "longProp", 456);
        doNonEqFilters(kindName, "doubleProp", 456.456);
        doAllFilters(kindName, "shortProp", 456);
        doNonEqFilters(kindName, "floatProp", new Float(456.456));
        doAllFilters(kindName, "ratingProp", new Rating(55));
    }

    @Test
    public void testSort() {
        doSort(kindName, "intProp", new Integer("123"), Query.SortDirection.ASCENDING);
        doSort(kindName, "intProp", new Integer("987"), Query.SortDirection.DESCENDING);
        doSort(kindName, "longProp", Long.MIN_VALUE, Query.SortDirection.ASCENDING);
        doSort(kindName, "longProp", Long.MAX_VALUE, Query.SortDirection.DESCENDING);
        doSort(kindName, "doubleProp", new Double("123.123"), Query.SortDirection.ASCENDING);
        doSort(kindName, "doubleProp", new Double("987.987"), Query.SortDirection.DESCENDING);
        doSort(kindName, "shortProp", new Integer(Short.MIN_VALUE), Query.SortDirection.ASCENDING);
        doSort(kindName, "shortProp", new Integer(Short.MAX_VALUE), Query.SortDirection.DESCENDING);
        doSort(kindName, "ratingProp", new Rating(11), Query.SortDirection.ASCENDING);
        doSort(kindName, "ratingProp", new Rating(99), Query.SortDirection.DESCENDING);
    }
  
    @Test
    public void testRatingType() {
        List<Entity> elist = doQuery(kindName, "ratingProp", Rating.class, true);
        Rating rate = (Rating) elist.get(0).getProperty("ratingProp");
        Rating sameDat = (Rating) elist.get(0).getProperty("ratingProp");
        Rating diffDat = (Rating) elist.get(1).getProperty("ratingProp");
        assertTrue(rate.equals(sameDat));
        assertFalse(rate.equals(diffDat));
        assertEquals(11, rate.getRating());
        assertEquals(0, rate.compareTo(sameDat));
        assertTrue(rate.compareTo(diffDat) != 0);
        assertEquals(rate.hashCode(), rate.hashCode());
    }
}

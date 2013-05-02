package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * datastore geopt data type test.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class GeoPtDataTest extends DatastoreTestBase {
    private String kindName = "geoPtType";
    private GeoPt[] testDatas = {
        new GeoPt(Float.valueOf(-12).floatValue(), Float.valueOf(120).floatValue()),
        new GeoPt(Float.valueOf(24).floatValue(), Float.valueOf(-90).floatValue()),
        new GeoPt(Float.valueOf(60).floatValue(), Float.valueOf(145).floatValue())
    };
    private FetchOptions fo = FetchOptions.Builder.withDefaults();

    @Before
    public void createData() throws InterruptedException {
        Query q = new Query(kindName, rootKey);
        if (service.prepare(q).countEntities(fo) == 0) {
            Entity newRec;
            List<Entity> elist = new ArrayList<Entity>();
            for (Object data : testDatas) {
                newRec = new Entity(kindName, rootKey);
                newRec.setProperty(propertyName, data);
                elist.add(newRec);
            }
            service.put(elist);
            sync(waitTime);
        }
    }

    @Test
    public void testFilter() throws Exception {
        doAllFilters(kindName, propertyName, new GeoPt(Float.valueOf(24).floatValue(), Float.valueOf(-90).floatValue()));
    }

    @Test
    public void testGets() throws Exception {
        Query query = new Query(kindName, rootKey);
        GeoPt filter = new GeoPt(Float.valueOf(60).floatValue(), Float.valueOf(145).floatValue());
        query.setFilter(new FilterPredicate(propertyName, Query.FilterOperator.EQUAL, filter));
        Entity entity = service.prepare(query).asSingleEntity();
        GeoPt geopt = (GeoPt) entity.getProperty(propertyName);
        assertTrue(geopt.equals(filter));
        assertEquals(Float.valueOf(geopt.getLatitude()).toString(), Float.valueOf(60).toString());
        assertEquals(Float.valueOf(geopt.getLongitude()).toString(), Float.valueOf(145).toString());
    }
  
    @Test
    public void testGeoptType() {
        List<Entity> elist = doQuery(kindName, propertyName, GeoPt.class, true);
        GeoPt rate = (GeoPt) elist.get(0).getProperty(propertyName);
        GeoPt sameDat = (GeoPt) elist.get(0).getProperty(propertyName);
        GeoPt diffDat = (GeoPt) elist.get(1).getProperty(propertyName);
        assertTrue(rate.equals(sameDat));
        assertFalse(rate.equals(diffDat));
        assertEquals(-12, rate.getLatitude(), 0);
        assertEquals(120, rate.getLongitude(), 0);
        assertEquals(0, rate.compareTo(sameDat));
        assertTrue(rate.compareTo(diffDat) != 0);
        assertEquals(rate.hashCode(), rate.hashCode());
  }
}

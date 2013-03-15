package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author hchen@google.com (Hannah Chen)
 */
public abstract class DatastoreTestBase extends TestBase {
    protected String rootKind = "root";
    protected Key rootKey = null;
    protected String propertyName = "propName";
    protected DatastoreService datastoreService;
    protected int waitTime = 2000;

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getTckDeployment();
        war.addClasses(DatastoreTestBase.class);
        war.addAsWebInfResource("datastore-indexes.xml");
        return war;
    }

    @Before
    public void setUp() throws InterruptedException {
        datastoreService = DatastoreServiceFactory.getDatastoreService();
        ensureRootEntityExists();
    }

    protected void clearData(String kind) throws InterruptedException {
        List<Key> eList = new ArrayList<Key>();
        Query query = new Query(kind, rootKey);
        for (Entity readRec : datastoreService.prepare(query).asIterable()) {
            eList.add(readRec.getKey());
        }
        if (eList.size() > 0) {
            datastoreService.delete(eList);
            sync(waitTime);
        }
    }

    protected Object[] getResult(Query query, String pName) {
        int count = datastoreService.prepare(query).countEntities(FetchOptions.Builder.withDefaults());
        Object result[] = new Object[count];
        int pt = 0;
        for (Entity readRec : datastoreService.prepare(query).asIterable()) {
            result[pt++] = readRec.getProperty(pName);
        }
        return result;
    }

    protected void doAllFilters(String kind, String pName, Object expected) {
        doNonEqFilters(kind, pName, expected);
        doEqFilters(kind, pName, expected);
    }

    protected void doNonEqFilters(String kind, String pName, Object expected) {
        verifyFilter(kind, pName, expected, Query.FilterOperator.LESS_THAN, 1, false);
        verifyFilter(kind, pName, expected, Query.FilterOperator.GREATER_THAN, 1, false);
    }

    protected void doEqFilters(String kind, String pName, Object expected) {
        verifyFilter(kind, pName, expected, Query.FilterOperator.LESS_THAN_OR_EQUAL, 2, true);
        verifyFilter(kind, pName, expected, Query.FilterOperator.EQUAL, 1, true);
        verifyFilter(kind, pName, expected, Query.FilterOperator.GREATER_THAN_OR_EQUAL, 2, true);
    }

    protected void doEqOnlyFilter(String kind, String pName, Object expected) {
        verifyFilter(kind, pName, expected, Query.FilterOperator.EQUAL, 1, true);
    }

    /**
     * inChk
     * - true: check if fDat are in result and if result count is correct;
     * - false: only check if result count is correct
     */
    protected void verifyFilter(String kind, String pName, Object fDat,
                                Query.FilterOperator operator, int rCont, boolean inChk) {
        Query query = new Query(kind, rootKey);
        query.setFilter(new FilterPredicate(pName, operator, fDat));
        Object[] result = getResult(query, pName);
        assertEquals(rCont, result.length);
        if (inChk) {
            boolean find = false;
            for (Object data : result) {
                if (data.toString().equals(fDat.toString())) {
                    find = true;
                }
            }
            assertEquals(true, find);
        }
    }

    protected void doSort(String kind, String pName, Object fDat, Query.SortDirection direction) {
        Query query = new Query(kind, rootKey);
        query.addSort(pName, direction);
        Object[] result = getResult(query, pName);
        assertEquals(fDat.toString(), result[0].toString());
    }

    protected void doSort(String kind, String pName, Object[] expDat, Query.SortDirection direction) {
        Query query = new Query(kind, rootKey);
        query.addSort(pName, direction);
        Object[] result = getResult(query, pName);
        assertEquals(expDat.length, result.length);
        assertTrue(Arrays.equals(result, expDat));
    }

    protected void doSort(String kind, String pName, int expDat, Query.SortDirection direction) {
        Query query = new Query(kind, rootKey);
        query.addSort(pName, direction);
        Object[] result = getResult(query, pName);
        assertEquals(expDat, result.length);
    }

    protected void ensureRootEntityExists() throws InterruptedException {
        Query query = new Query(rootKind);
        Entity rootEntity = datastoreService.prepare(query).asSingleEntity();
        if (rootEntity == null) {
            rootEntity = new Entity(rootKind);
            rootEntity.setProperty("name", "junittests group");
            rootKey = datastoreService.put(rootEntity);
            sync(waitTime);
        } else {
            rootKey = rootEntity.getKey();
        }
    }
}

package com.google.appengine.tck.datastore;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * datastore ancestor test.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class AncestorTest extends DatastoreTestBase {
    private static final String PARENTKIND = "school";
    private static final String CHILDKIND = "room";

    @Before
    public void createData() throws InterruptedException {
        Query q = new Query(PARENTKIND);
        if (service.prepare(q).countEntities(FetchOptions.Builder.withDefaults()) == 0) {
            List<Entity> elist = new ArrayList<Entity>();
            Entity pRec, cRec;
            // add parents
            pRec = new Entity(PARENTKIND);
            pRec.setProperty("name", "redwood");
            Key key1 = service.put(pRec);
            pRec = new Entity(PARENTKIND);
            pRec.setProperty("name", "argonaut");
            Key key2 = service.put(pRec);
            // add children
            cRec = new Entity(CHILDKIND, key1);
            cRec.setProperty("teacher", "Mrs. Key1-redwood");
            elist.add(cRec);
            cRec = new Entity(CHILDKIND, key1);
            cRec.setProperty("teacher", "Mrs. Key2-redwood");
            elist.add(cRec);
            cRec = new Entity(CHILDKIND, key2);
            cRec.setProperty("teacher", "Mrs. Key1-argonaut");
            elist.add(cRec);
            cRec = new Entity(CHILDKIND, key2);
            cRec.setProperty("teacher", "Mrs. Key2-argonaut");
            elist.add(cRec);
            service.put(elist);
            sync(waitTime);
        }
    }

    @Test
    public void testAncestor() {
        Key pKey = getParent().getKey();
        Query query = new Query(CHILDKIND, pKey);
        assertEquals(2, service.prepare(query)
                .countEntities(FetchOptions.Builder.withDefaults()));
        for (Entity cRec : service.prepare(query).asIterable()) {
            assertEquals(pKey, cRec.getParent());
        }
    }

    @Test
    public void testAncestorKey() {
        Key pKey = getParent().getKey();
        Query query = new Query(CHILDKIND, pKey);
        query.addSort("__key__");
        assertEquals(2, service.prepare(query)
                .countEntities(FetchOptions.Builder.withDefaults()));
        for (Entity cRec : service.prepare(query).asIterable()) {
            assertEquals(pKey, cRec.getParent());
        }
    }

    @Test
    public void testKindless() {
        Query query = new Query(PARENTKIND);
        query.setFilter(new FilterPredicate("name", Query.FilterOperator.EQUAL, "argonaut"));
        Entity parent = service.prepare(query).asSingleEntity();
        query = new Query(parent.getKey());
        assertEquals(3, service.prepare(query)
                .countEntities(FetchOptions.Builder.withDefaults()));
        query = new Query().setAncestor(parent.getKey());
        assertEquals(3, service.prepare(query)
                .countEntities(FetchOptions.Builder.withDefaults()));
    }

    @Test
    public void testKeyName() {
        Entity pRec = new Entity(PARENTKIND, "测试文档keyname");
        pRec.setProperty("name", "regression");
        service.put(pRec);
        assertEquals(pRec.getKey().getName(), "测试文档keyname");

        Entity cRec = new Entity(CHILDKIND, "测试文档keyname", pRec.getKey());
        cRec.setProperty("teacher", "regression");
        service.put(cRec);
        assertEquals(cRec.getKey().getName(), "测试文档keyname");
        service.delete(cRec.getKey(), pRec.getKey());
    }

    private Entity getParent() {
        Query query = new Query(PARENTKIND);
        query.setFilter(new FilterPredicate("name", Query.FilterOperator.EQUAL, "argonaut"));
        Entity parent = service.prepare(query).asSingleEntity();
        return parent;
    }
}
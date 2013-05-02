/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.tck.datastore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterPredicate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class UnindexedPropertiesTest extends SimpleTestBase {

    private static final Field PROPERTY_MAP_FIELD;
    private static final String UNINDEXED_ENTITY = "unindexedTest";

    static {
        try {
            PROPERTY_MAP_FIELD = Entity.class.getDeclaredField("propertyMap");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        PROPERTY_MAP_FIELD.setAccessible(true);
    }

    @Test
    public void testUnindexedProperties() throws Exception {
        Entity entity = new Entity(UNINDEXED_ENTITY);
        entity.setUnindexedProperty("unindexedString", "unindexedString");
        entity.setUnindexedProperty("unindexedList", new ArrayList<String>(Arrays.asList("listElement1", "listElement2", "listElement3")));
        entity.setUnindexedProperty("unindexedText", new Text("unindexedText"));
        entity.setUnindexedProperty("unindexedBlob", new Blob("unindexedBlob".getBytes()));
        entity.setProperty("text", new Text("text"));
        entity.setProperty("blob", new Blob("blob".getBytes()));

        Key key = service.put(entity);
        sync(3000);  // Not using ancestor queries, so pause before doing queries below.
        Entity entity2 = service.get(key);

        assertTrue(isUnindexed(getRawProperty(entity2, "unindexedString")));
        assertTrue(isUnindexed(getRawProperty(entity2, "unindexedList")));
        assertTrue(isUnindexed(getRawProperty(entity2, "unindexedText")));
        assertTrue(isUnindexed(getRawProperty(entity2, "unindexedBlob")));
        assertTrue(isUnindexed(getRawProperty(entity2, "text")));
        assertTrue(isUnindexed(getRawProperty(entity2, "blob")));

        assertNull(getResult(new Query(UNINDEXED_ENTITY).setFilter(new FilterPredicate("unindexedString", EQUAL, "unindexedString"))));
        assertNull(getResult(new Query(UNINDEXED_ENTITY).setFilter(new FilterPredicate("unindexedList", EQUAL, "listElement1"))));
        assertNull(getResult(new Query(UNINDEXED_ENTITY).setFilter(new FilterPredicate("unindexedText", EQUAL, "unindexedText"))));
        assertNull(getResult(new Query(UNINDEXED_ENTITY).setFilter(new FilterPredicate("text", EQUAL, "text"))));

        service.delete(key);
    }

    @Ignore("CAPEDWARF-66")
    @Test
    public void testFilterWithUnindexedPropertyType() throws Exception {
        Entity entity = new Entity(UNINDEXED_ENTITY);
        entity.setProperty("prop", "bbb");
        service.put(entity);
        sync(3000);

        assertNull(getResult(new Query(UNINDEXED_ENTITY).setFilter(new FilterPredicate("prop", EQUAL, new Text("bbb")))));
        assertNull(getResult(new Query(UNINDEXED_ENTITY).setFilter(new FilterPredicate("prop", LESS_THAN, new Text("ccc")))));
        assertNull(getResult(new Query(UNINDEXED_ENTITY).setFilter(new FilterPredicate("prop", LESS_THAN_OR_EQUAL, new Text("ccc")))));

        // it's strange that GREATER_THAN actually DOES return a result, whereas LESS_THAN doesn't
        assertEquals(entity, getResult(new Query(UNINDEXED_ENTITY).setFilter(new FilterPredicate("prop", GREATER_THAN, new Text("aaa")))));
        assertEquals(entity, getResult(new Query(UNINDEXED_ENTITY).setFilter(new FilterPredicate("prop", GREATER_THAN_OR_EQUAL, new Text("aaa")))));

        service.delete(entity.getKey());
    }

    private Entity getResult(Query query) {
        return service.prepare(query).asSingleEntity();
    }

    private boolean isUnindexed(Object rawProperty) {
        return rawProperty.getClass().getName().endsWith("$UnindexedValue");
    }

    private Object getRawProperty(Entity entity, String propertyName) throws IllegalAccessException {
        return getRawPropertyMap(entity).get(propertyName);
    }

    private Map getRawPropertyMap(Entity entity) throws IllegalAccessException {
        return ((Map) PROPERTY_MAP_FIELD.get(entity));
    }
}

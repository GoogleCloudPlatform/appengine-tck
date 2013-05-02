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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)

//public class SmokeTest extends SimpleTestBase {
public class SmokeTest extends DatastoreTestBase {

    private static final String SMOKE_TEST_ENTITY = "SmokeTestEntity";

    @Test
    public void putStoresEntity() throws Exception {
        Entity entity = createTestEntity();
        service.put(entity);
        assertStoreContains(entity);
    }

    @Test
    public void putStoresAllGivenEntities() throws Exception {
        Collection<Entity> entities = createTestEntities();
        for (Entity e : entities) {
            service.put(e);
        }
        assertStoreContainsAll(entities);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getThrowsNotFoundExceptionWhenKeyIsNotFound() throws Exception {
        Key nonExistingKey = KeyFactory.createKey("NonExistingKey", 1);
        service.get(nonExistingKey);
    }

    @Test
    public void batchGetReturnsOnlyExistingKeysInMap() throws Exception {
        Key existingKey = KeyFactory.createKey("batch", "existing");
        Key nonExistingKey = KeyFactory.createKey("batch", "nonExisting");
        service.put(new Entity(existingKey));

        Map<Key, Entity> map = service.get(Arrays.asList(existingKey, nonExistingKey));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(existingKey));
    }

    @Test
    public void deleteRemovesEntityFromStore() throws Exception {
        Entity entity = createTestEntity();
        Key key = entity.getKey();
        service.put(entity);

        service.delete(key);
        assertStoreDoesNotContain(key);
    }

//    @Test
//    public void deleteRemovesAllGivenEntities() throws Exception {
//        Collection<Entity> entities = createTestEntities();
//        Collection<Key> keys = extractKeys(entities);
//        for (Entity e : entities) {
//            service.put(e);
//        }
//
//        for (Key k : keys) {
//            service.delete(k);
//        }
//        assertStoreDoesNotContain(keys);
//    }

    @Test
    public void queriesDontReturnDeletedEntities() throws Exception {
        String methodName = "queriesDontReturnDeletedEntities";
        Entity entity = createTestEntityWithUniqueMethodNameKey(SMOKE_TEST_ENTITY, methodName);
        Key key = entity.getKey();
        service.put(entity);

        service.delete(key);

        List<Entity> entities = service.prepare(new Query(SMOKE_TEST_ENTITY).setAncestor(key))
            .asList(FetchOptions.Builder.withDefaults());
        assertEquals(0, entities.size());
    }


}

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * embedded entity property test
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class EncodedEntityTest extends DatastoreTestBase {
    private String kindName = "entityproperty";

    @Test
    public void testEmbeddedEntity() throws InterruptedException {
        clearData(kindName);

        List<Entity> elist = new ArrayList<Entity>();
        Entity newRec = new Entity(kindName, rootKey);
        newRec.setProperty("entityDat", null);
        newRec.setProperty("stringData", "no entity property");
        newRec.setProperty("timestamp", new Date());
        elist.add(newRec);

        // Notice, no kind, or key is required
        EmbeddedEntity newRec0 = new EmbeddedEntity();
        newRec0.setProperty("stringData", "check6009464");
        newRec0.setProperty("timestamp", new Date());

        newRec = new Entity(kindName, rootKey);
        newRec.setProperty("entityDat", newRec0);
        newRec.setProperty("stringData", "have entity property");
        newRec.setProperty("timestamp", new Date());
        elist.add(newRec);
        service.put(elist);
        sync(waitTime);

        Query q = new Query(kindName, rootKey);
        int count = service.prepare(q).countEntities(FetchOptions.Builder.withDefaults());
        assertEquals(2, count);
        for (Entity readRec : service.prepare(q).asIterable()) {
            EmbeddedEntity ee2 = (EmbeddedEntity) readRec.getProperty("entityDat");
            if (ee2 != null) {
                assertEquals("have entity property", readRec.getProperty("stringData"));
                assertEquals(readRec.getProperty("entityDat").getClass(), EmbeddedEntity.class);
                Entity e = new Entity(ee2.getKey());
                e.setPropertiesFrom(ee2);
                assertEquals("check6009464", e.getProperty("stringData"));
            } else {
                assertEquals("no entity property", readRec.getProperty("stringData"));
            }
        }
    }
}

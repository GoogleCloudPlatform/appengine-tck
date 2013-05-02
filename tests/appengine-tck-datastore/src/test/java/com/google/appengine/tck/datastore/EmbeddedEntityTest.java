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

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class EmbeddedEntityTest extends SimpleTestBase {

    @Test
    public void test() throws Exception {
        EmbeddedEntity embedded = new EmbeddedEntity();
        embedded.setProperty("string", "foo");

        Entity entity = createTestEntity();
        entity.setProperty("embedded", embedded);
        Key key = service.put(entity);

        Entity storedEntity = service.get(key);
        EmbeddedEntity storedEmbedded = (EmbeddedEntity) storedEntity.getProperty("embedded");

        assertEquals(embedded, storedEmbedded);
    }
}

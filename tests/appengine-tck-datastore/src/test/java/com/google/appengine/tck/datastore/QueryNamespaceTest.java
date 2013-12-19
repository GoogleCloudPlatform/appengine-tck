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

import java.util.List;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Datastore querying namespace tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class QueryNamespaceTest extends QueryTestBase {
    private static final String QUERY_NAMESPACE_ENTITY = "QueryNamespaceTestEntity";

    private Key createQueryNamespaceTestParent(String methodName) {
        Entity parent = createTestEntityWithUniqueMethodNameKey(QUERY_NAMESPACE_ENTITY, methodName);
        return parent.getKey();
    }

    @Test
    public void testQueryListWithNamespaceChange() throws Exception {
        Key parentKey = createQueryNamespaceTestParent("testQueryListWithNamespaceChange");
        Entity bob = createEntity("QLWNC", parentKey)
            .withProperty("name", "Bob")
            .withProperty("lastName", "Smith")
            .store();

        try {
            Query query = new Query("QLWNC");
            List<Entity> list = service.prepare(query).asList(withDefaults());

            final String previousNS = NamespaceManager.get();
            NamespaceManager.set("QwertyNS");
            try {
                assertEquals(1, list.size());
            } finally {
                NamespaceManager.set(previousNS);
            }
        } finally {
            service.delete(bob.getKey(), parentKey);
        }
    }
}

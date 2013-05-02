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

package com.google.appengine.tck.memcache;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class MemcacheMultitenancyTest extends TestBase {

    @Deployment
    public static Archive getDeployment() {
        return getTckDeployment();
    }

    @Test
    public void testMemcacheServiceNotBoundToSpecificNamespaceReturnsNullNamespace() {
        MemcacheService service = MemcacheServiceFactory.getMemcacheService();
        assertNull(service.getNamespace());
    }

    @Test
    public void testMemcacheServiceNotBoundToSpecificNamespaceAlwaysUsesCurrentNamespace() {
        MemcacheService service = MemcacheServiceFactory.getMemcacheService();

        NamespaceManager.set("one");
        service.put("key", "value in namespace one");
        NamespaceManager.set("two");
        service.put("key", "value in namespace two");

        NamespaceManager.set("one");
        assertEquals("value in namespace one", service.get("key"));

        NamespaceManager.set("two");
        assertEquals("value in namespace two", service.get("key"));
    }

    @Test
    public void testMemcacheServiceBoundToSpecificNamespaceIgnoresNamespaceManager() {
        NamespaceManager.set("one");

        MemcacheService service = MemcacheServiceFactory.getMemcacheService("two");
        service.put("key", "value");

        NamespaceManager.set("three");

        assertEquals("two", service.getNamespace());

        assertTrue(service.contains("key"));
        assertEquals("value", service.get("key"));
    }
}

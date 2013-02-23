/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package com.google.appengine.tck.memcache;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.capedwarf.common.test.TestBase;
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
        return getCapedwarfDeployment();
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

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

import java.util.Arrays;
import java.util.logging.Level;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.BaseMemcacheService;
import com.google.appengine.api.memcache.ConsistentLogAndContinueErrorHandler;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests methods that are common, between MemcacheService and AsyncMemcacheService,
 * which implement BaseMemcacheService.
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class MemcacheCommonServiceTest extends CacheTestBase {

    private MemcacheService memcache;
    private AsyncMemcacheService asyncMemcache;

    @Before
    public void setUp() {
        memcache = MemcacheServiceFactory.getMemcacheService();
        memcache.clearAll();

        asyncMemcache = MemcacheServiceFactory.getAsyncMemcacheService();
        asyncMemcache.clearAll();
    }

    @Test
    public void testErrorHandler() {
        ConsistentLogAndContinueErrorHandler handler = new ConsistentLogAndContinueErrorHandler(Level.ALL);
        ConsistentLogAndContinueErrorHandler returned = initConsistentErrorHandler(memcache, handler);

        ConsistentLogAndContinueErrorHandler asyncHandler = new ConsistentLogAndContinueErrorHandler(Level.ALL);
        ConsistentLogAndContinueErrorHandler asyncReturned = initConsistentErrorHandler(asyncMemcache, asyncHandler);

        assertTrue(handler.equals(returned));
        assertTrue(asyncHandler.equals(asyncReturned));

        int recordSize = (1024 * 1024) * 2;  // 2MB which should fail.
        byte[] filledRec = new byte[recordSize];
        Arrays.fill(filledRec, (byte) 0x41);

        // MemcacheServiceException should not surface to the test.
        memcache.put("BiggerThanOneMBValue", filledRec);
        asyncMemcache.put("BiggerThanOneMBValue", filledRec);
    }

    @Test
    /**
     * Call getNamespace() via BaseMemcacheService interface for method coverage reporting.
     */
    public void testGetNamespace() {
        assertNull("Default namespace should be null.", ((BaseMemcacheService) memcache).getNamespace());
        assertNull("Default namespace should be null.", ((BaseMemcacheService) asyncMemcache).getNamespace());

        String cacheName = "My-CacheNamespaceForSpecialCase.09";
        MemcacheService namedMemcache = MemcacheServiceFactory.getMemcacheService(cacheName);
        assertEquals(cacheName, ((BaseMemcacheService) namedMemcache).getNamespace());
    }

    /**
     * ErrorHandler is deprecated, return ConsistentErrorHandler instead.  Method coverage will be
     * detected on BaseMemcacheService, as opposed to just calling getErrorHandler() on the class
     * directly.
     */
    protected ConsistentLogAndContinueErrorHandler initConsistentErrorHandler(BaseMemcacheService service,
                                                                              ConsistentLogAndContinueErrorHandler handler) {

        service.setErrorHandler(handler);
        return (ConsistentLogAndContinueErrorHandler) service.getErrorHandler();
    }
}

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

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class MemcacheStatisticsTest extends TestBase {

    protected MemcacheService service;

    @Deployment
    public static Archive getDeployment() {
        return getTckDeployment();
    }

    @Before
    public void setUp() {
        service = MemcacheServiceFactory.getMemcacheService();
    }

    @After
    public void tearDown() {
        service.clearAll();
        service = null;
    }

    @Test
    @Ignore("enable JMX stats for cache")
    public void testItemCount() {
        assertEquals(0, service.getStatistics().getItemCount());
        service.put("key1", "value1");
        assertEquals(1, service.getStatistics().getItemCount());
    }

    @Test
    @Ignore("enable JMX stats for cache")
    public void testHitAndMissCount() {
        service.put("key1", "value1");

        assertEquals(0, service.getStatistics().getHitCount());
        assertEquals(0, service.getStatistics().getMissCount());

        service.get("key1");
        assertEquals(1, service.getStatistics().getHitCount());
        assertEquals(0, service.getStatistics().getMissCount());

        service.get("key2");
        assertEquals(1, service.getStatistics().getHitCount());
        assertEquals(1, service.getStatistics().getMissCount());
    }

}

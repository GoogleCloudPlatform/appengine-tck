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

package com.google.appengine.tck.modules;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class MemcacheTest extends CrudTestBase {
    private static final Object KEY_1 = "123";
    private static final Object VALUE_1 = "querty";
    private static final Object VALUE_2 = "xyz";

    private MemcacheService ms;

    @Deployment
    public static EnterpriseArchive getDeployment() {
        return getCrudDeployment(MemcacheTest.class);
    }

    @Before
    public void setUp() throws Exception {
        ms = MemcacheServiceFactory.getMemcacheService();
    }

    @Override
    protected void doTestCreate() {
        ms.put(KEY_1, VALUE_1);
    }

    @Override
    protected void doTestRead() throws Exception {
        Assert.assertEquals(VALUE_1, ms.get(KEY_1));
    }

    @Override
    protected void doTestUpdate() throws Exception {
        ms.put(KEY_1, VALUE_2);
    }

    @Override
    protected void doTestReRead() throws Exception {
        Assert.assertEquals(VALUE_2, ms.get(KEY_1));
    }

    @Override
    protected void doTestDelete() {
        ms.delete(KEY_1);
    }

    @Override
    protected void doTestCheck() {
        Assert.assertFalse(ms.contains(KEY_1));
    }
}

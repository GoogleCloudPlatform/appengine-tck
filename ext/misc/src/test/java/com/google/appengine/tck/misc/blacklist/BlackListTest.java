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

package com.google.appengine.tck.misc.blacklist;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test BlackList.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class BlackListTest extends TestBase {
    protected Context context;

    @Deployment
    public static WebArchive getDeployment() {
        return getTckDeployment();
    }

    @Test(expected = NoClassDefFoundError.class)
    public void testDirectInitialization() throws Exception {
        context = new InitialContext();
    }

    @Test(expected = IllegalAccessException.class)
    public void testReflectionInitialization() throws Exception {
        Class<?> clazz = Class.forName(InitialContext.class.getName());
        clazz.newInstance();
    }
}

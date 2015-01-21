/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.appengine.tck.misc.servlet;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.misc.servlet.support.WarmupData;
import com.google.appengine.tck.misc.servlet.support.WarmupServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public abstract class AbstractWarmupTestBase extends TestBase {

    protected static WebArchive getBaseDeployment(boolean enabled) {
        System.setProperty("appengine.warmup.enabled", Boolean.toString(enabled));
        try {
            TestContext context = new TestContext();
            context.setWebXmlFile("web-warmup.xml");
            context.setAppEngineWebXmlFile("appengine-web-warmup.xml");
            WebArchive war = getTckDeployment(context);
            war.addClass(AbstractWarmupTestBase.class);
            war.addClass(WarmupServlet.class);
            war.addClass(WarmupData.class);
            return war;
        } finally {
            System.clearProperty("appengine.warmup.enabled");
        }
    }

    @AfterClass
    public static void cleanup() {
        deleteTempData(WarmupData.class);
    }
}

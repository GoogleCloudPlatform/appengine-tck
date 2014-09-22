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

package com.google.appengine.tck.byteman;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.byteman.support.ConcurrentTxServlet;
import com.google.appengine.tck.byteman.support.Poke;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BytemanTestBase extends TestBase {
    protected static WebArchive getBytemanDeployment() {
        TestContext context = new TestContext();
        context.setWebXmlFile("bm-web.xml");
        context.setAppEngineWebXmlFile("bm-appengine-web.xml");

        WebArchive war = getTckDeployment(context);
        war.addClass(BytemanTestBase.class);
        war.addClasses(ConcurrentTxServlet.class, Poke.class);
        return war;
    }
}

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

package com.google.appengine.tck.gcsclient;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.lib.LibUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;


/**
 * @author <a href="mailto:hchen@google.com">Hannah Chen</a>
 */
public abstract class GCSClientTestBase extends TestBase {

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext();
        WebArchive war = getTckDeployment(context);

        war.addClass(TestBase.class);
        war.addClass(GCSClientTestBase.class);
        war.addClasses(GCSClientTest.class);

        LibUtils libUtils = new LibUtils();
        libUtils.addGaeAsLibrary(war);
        libUtils.addLibrary(war, "com.google.guava", "guava");
        libUtils.addLibrary(war, "com.google.appengine.tools", "appengine-gcs-client");

        return war;
    }
}

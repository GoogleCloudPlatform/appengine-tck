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

package com.google.appengine.tck.appidentity;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import org.apache.commons.validator.routines.EmailValidator;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

import java.io.IOException;


/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public abstract class AppIdentityTestBase extends TestBase {

    protected String appIdproperty;
    protected String appEngineServer;

    protected static WebArchive getDefaultDeployment() {
        TestContext context = new TestContext().setUseSystemProperties(true).setCompatibilityProperties(TCK_PROPERTIES);

        // Add a custom web.xml.  These resource files are located:
        // in tests/appengine-tck-the-test/src/test/resource
        // context.setWebXmlFile("web-taskqueue.xml");

        WebArchive war = getTckDeployment(context);
        war.addClass(AppIdentityTestBase.class);
        war.addPackage(EmailValidator.class.getPackage());

        // Add a taskqueue configuration.
        // war.addAsWebInfResource("queue.xml");

        return war;
    }

    @Before
    public void initSystemProperties() {
        try {
            appIdproperty = readProperties(TCK_PROPERTIES).getProperty("appengine.appId");

            appEngineServer = readProperties(TCK_PROPERTIES).getProperty("appengine.server");
            if (appEngineServer == null) {
                appEngineServer = "appspot.com";
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}

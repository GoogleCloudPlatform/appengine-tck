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

import java.io.IOException;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.event.Property;
import org.apache.commons.validator.routines.EmailValidator;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;


/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public abstract class AppIdentityTestBase extends TestBase {

    protected String appIdproperty;
    protected String appEngineServer;

    protected static WebArchive getDefaultDeployment() {
        TestContext context = new TestContext().setUseSystemProperties(true).setCompatibilityProperties(TCK_PROPERTIES);

        WebArchive war = getTckDeployment(context);
        war.addClass(AppIdentityTestBase.class);
        war.addPackage(EmailValidator.class.getPackage());

        return war;
    }

    @Before
    public void initSystemProperties() {
        try {
            appIdproperty = readProperties(TCK_PROPERTIES).getProperty("appengine.appId");
            appEngineServer = readProperties(TCK_PROPERTIES).getProperty("appengine.server");
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    protected String getExpectedAppId(String context) {
        String expectedAppId = appIdproperty;
        if (expectedAppId == null) {
            Property property = property(context);
            if (property.exists()) {
                expectedAppId = property.getPropertyValue();
            } else {
                String nullIdMsg = "Either -Dappengine.appId= or test-contexts.properties must set the app id.";
                throw new IllegalStateException(nullIdMsg);
            }
        }
        return expectedAppId;
    }

    protected String getExpectedAppHostname(String context) {
        String expectedHostname = appEngineServer;
        if (expectedHostname == null) {
            Property property = property(context);
            if (property.exists()) {
                expectedHostname = property.getPropertyValue();
            } else {
                String nullHostnameMsg = "Either -Dappengine.server= or test-contexts.properties must set the server name.";
                throw new IllegalStateException(nullHostnameMsg);
            }
        }
        return expectedHostname;
    }
}

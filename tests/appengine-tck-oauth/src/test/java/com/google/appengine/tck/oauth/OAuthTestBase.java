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

package com.google.appengine.tck.oauth;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.event.Property;
import com.google.appengine.tck.oauth.support.OAuthServiceServlet;
import com.google.appengine.tck.oauth.support.OAuthServletAnswer;
import org.jboss.shrinkwrap.api.spec.WebArchive;


/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public abstract class OAuthTestBase extends TestBase {

    protected String nonAdminTestingAccountEmail;
    protected String nonAdminTestingAccountPw;
    protected String oauthClientId;
    protected String oauthRedirectUri;

    protected static WebArchive getBaseDeployment() {

        TestContext context = new TestContext();

        context.setWebXmlFile("oauth-web.xml");

        WebArchive war = getTckDeployment(context);
        war.addClasses(OAuthTestBase.class, OAuthServiceServlet.class, OAuthServletAnswer.class);

        return war;
    }


    /**
     * Only accessible with tests annotated with @RunAsClient
     */
    public void initProperties() {
        nonAdminTestingAccountEmail = System.getProperty("appengine.nonAdminTestingAccount.email");
        nonAdminTestingAccountPw = System.getProperty("appengine.nonAdminTestingAccount.pw");

        oauthClientId = System.getProperty("appengine.oauth.clientId");
        oauthRedirectUri = System.getProperty("appengine.oauth.redirectUri");

        Property nonAdmin = property("nonAdminTestingAccount");
        if (nonAdmin.exists()) {
            nonAdminTestingAccountEmail = nonAdmin.getPropertyValue();
            nonAdminTestingAccountPw = "boguspw";
        }

        boolean testingAccountRequired;
        Property accountRequired = property("testingAccountRequired");
        if (!accountRequired.exists()) {
            testingAccountRequired = true;
        } else {
            testingAccountRequired = accountRequired.getPropertyValue().equalsIgnoreCase("true");
        }

        if (nonAdminTestingAccountEmail == null && testingAccountRequired) {
            throw new IllegalStateException("-Dappengine.nonAdminTestingAccount.email is not defined.");
        }
        if (nonAdminTestingAccountPw == null && testingAccountRequired) {
            throw new IllegalStateException("-Dappengine.nonAdminTestingAccount.pw is not defined.");
        }
        if (oauthClientId == null && testingAccountRequired) {
            throw new IllegalStateException("-Dappengine.oauth.clientId is not defined.");
        }
        if (oauthRedirectUri == null && testingAccountRequired) {
            throw new IllegalStateException("-Dappengine.oauth.redirectUri is not defined.");
        }
    }
}

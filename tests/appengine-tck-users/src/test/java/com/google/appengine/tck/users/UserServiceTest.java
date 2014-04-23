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
package com.google.appengine.tck.users;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.UserServiceFailureException;
import com.google.appengine.tck.env.Environment;
import com.google.appengine.tck.users.support.ServletAnswer;
import com.google.appengine.tck.util.AuthClientException;
import com.google.appengine.tck.util.GaeAuthClient;
import com.google.appengine.tck.util.Utils;
import com.google.apphosting.api.ApiProxy;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;


/**
 * There are 3 authentication settings.
 * 1) Google Accounts
 * 2) Google Apps Accounts
 * 3) Federated Login/OpenId Provider
 * <p/>
 * This test class covers #1.  See https://developers.google.com/appengine/articles/auth
 * <p/>
 * Dev_appserver and appspot differences, dev_appserver does not actually
 * authenticate a user, while appspot does.
 *
 * @author ales.justin@jboss.org
 * @author terryok@google.com
 */
@RunWith(Arquillian.class)
public class UserServiceTest extends UserTestBase {

    private UserService userService;
    private GaeAuthClient authClient;

    private static final String DEST_URL = "http://gaetcktest.org?abc=123&xyz=456";

    // dev_appserver also encodes : and /
    private static final String DEST_URL_ENCODED_SINGLE = "http%3A%2F%2Fgaetcktest.org%3Fabc%3D123%26xyz%3D456";

    private static final String DEST_URL_ENCODED_DOUBLE = "http://gaetcktest.org/%253Fabc%253D123%2526xyz%253D456";
    private static final String DEST_URL_ENCODED_TRIPLE = "http://gaetcktest.org/%25253Fabc%25253D123%252526xyz%25253D456";
    private String userId;
    private String pw;
    private Boolean prodEnv;
    private Boolean devEnv;

    @Before
    public void setUp() {
        if (isInContainer()) {
            // should not create one for client side
            userService = UserServiceFactory.getUserService();
        }

        userId = System.getProperty("appengine.userId");
        pw = System.getProperty("appengine.password");
    }

    private void initServletEnvironment(URL url) throws IOException {
        ServletAnswer answer = getUnAuthServletAnswer(url, "env");
        prodEnv = answer.isEnvironmentProd();
        devEnv = !prodEnv;
    }

    private boolean isServletProd(URL url) throws IOException {
        if (prodEnv == null) {
            initServletEnvironment(url);
        }
        return prodEnv;
    }

    private boolean isServletDev(URL url) throws IOException {
        if (devEnv == null) {
            initServletEnvironment(url);
        }
        return devEnv;
    }

    @After
    public void tearDown() {
        shutdownAuthClient();
    }

    private ServletAnswer getUnAuthServletAnswer(URL url, String method) throws IOException {
        URL pingUrl;
        String servletMethod = "user-service-helper?method=" + method;
        if (url.getPath().endsWith("/")) {
            pingUrl = new URL(url.toString() + servletMethod);
        } else {
            pingUrl = new URL(url.toString() + "/" + servletMethod);
        }
        URLConnection conn = pingUrl.openConnection();
        String answer = Utils.readFullyAndClose(conn.getInputStream());
        return new ServletAnswer(answer);
    }

    private ServletAnswer getAuthServletAnswer(GaeAuthClient client, URL url, String method) throws IOException {

        HttpResponse response = client.getUrl(url + "/user-service-helper?method=" + method);
        String resp = EntityUtils.toString(response.getEntity());
        return new ServletAnswer(resp);
    }

    private void initAuthClient(URL url, String userId, String pw) {
        if (authClient == null) {
            try {
                authClient = new GaeAuthClient(url.toString(), userId, pw);
            } catch (AuthClientException ae) {
                throw new IllegalStateException(ae.toString());
            }
        }
    }

    private void shutdownAuthClient() {
        if (authClient != null) {
            GaeAuthClient tmp = authClient;
            authClient = null;

            tmp.shutdown();
        }
    }

    @Test
    @RunAsClient
    public void testGetEmailProd(@ArquillianResource URL url) throws Exception {
        if (!isServletProd(url)) {
            return;
        }

        initAuthClient(url, userId, pw);
        ServletAnswer answer = getAuthServletAnswer(authClient, url, "getEmail");
        Assert.assertEquals("UserId should be same as authenticated user:" + answer, userId, answer.getReturnVal());
    }

    @Test
    @RunAsClient
    public void testGetEmailDev(@ArquillianResource URL url) throws Exception {
        if (!isServletDev(url)) {
            return;
        }

        ServletAnswer answer = getUnAuthServletAnswer(url, "getEmail");
        Assert.assertEquals("user is null", answer.getReturnVal());
    }

    /**
     * Verify isUserLoggedIn on production instance.
     */
    @Test
    @RunAsClient
    public void testIsUserLoggedInProd(@ArquillianResource URL url) throws Exception {
        if (!isServletProd(url)) {
            return;
        }

        initAuthClient(url, userId, pw);
        ServletAnswer answer = getAuthServletAnswer(authClient, url, "isUserLoggedIn");
        Assert.assertEquals("User should be Logged in since the GaeAuthClient is initialized.", "true", answer.getReturnVal());
    }

    /**
     * Verify isUserLoggedIn on dev_appserver.
     */
    @Test
    @RunAsClient
    public void testIsUserLoggedInDev(@ArquillianResource URL url) throws Exception {
        if (!isServletDev(url)) {
            return;
        }

        ServletAnswer answer = getUnAuthServletAnswer(url, "isUserLoggedIn");
        Assert.assertEquals("false", answer.getReturnVal());
    }

    @Test
    @RunAsClient
    public void testIsUserAdminInProd(@ArquillianResource URL url) throws Exception {
        if (!isServletProd(url)) {
            return;
        }

        initAuthClient(url, userId, pw);
        ServletAnswer answer = getAuthServletAnswer(authClient, url, "isUserAdmin");
        Assert.assertEquals("User should be logged in and admin.",
            "true", answer.getReturnVal());
    }

    @Test
    @RunAsClient
    public void testIsUserAdminInDev(@ArquillianResource URL url) throws Exception {
        if (!isServletDev(url)) {
            return;
        }

        ServletAnswer answer = getUnAuthServletAnswer(url, "isUserLoggedIn");
        Assert.assertEquals("false", answer.getReturnVal());
    }

    @Test
    public void testCreateLoginUrlProd() throws Exception {
        assumeEnvironment(Environment.APPSPOT);

        String start = "https://www.google.com/accounts/ServiceLogin?service=ah";
        String createdURL = UserServiceFactory.getUserService().createLoginURL(DEST_URL);

        String failMsgStartsWith = "Prod url should start with: " + start + " but was: " + createdURL;
        Assert.assertTrue(failMsgStartsWith, createdURL.startsWith(start));

        String failMsgContains = "Prod url should contain: " + DEST_URL_ENCODED_DOUBLE + " but was: " + createdURL;
        Assert.assertTrue(failMsgContains, createdURL.contains(DEST_URL_ENCODED_DOUBLE));
    }

    @Test
    public void testCreateLoginUrlDev() throws Exception {
        assumeEnvironment(Environment.CAPEDWARF, Environment.SDK);

        String destURLenc = "/_ah/email?continue=" + DEST_URL_ENCODED_SINGLE;
        String createdURL = UserServiceFactory.getUserService().createLoginURL(DEST_URL);
        Assert.assertEquals(destURLenc, createdURL);
    }

    @Test
    public void testCreateLoginUrlDomainProd() throws Exception {
        assumeEnvironment(Environment.APPSPOT);

        String start = "https://www.google.com/accounts/ServiceLogin?service=ah";
        String authDomain = "othergaetcktest.org";
        String createdURL = UserServiceFactory.getUserService().createLoginURL(DEST_URL, authDomain);

        // TODO: verify how to check for the authDomain.

        String failMsgStartsWith = "Prod url should start with: " + start + " but was: " + createdURL;
        Assert.assertTrue(failMsgStartsWith, createdURL.startsWith(start));

        String failMsgContains = "Prod url should contain: " + DEST_URL_ENCODED_DOUBLE + " but was: " + createdURL;
        Assert.assertTrue(failMsgContains, createdURL.contains(DEST_URL_ENCODED_DOUBLE));
    }

    @Test
    public void testCreateLoginUrlDomainDev() throws Exception {
        assumeEnvironment(Environment.CAPEDWARF, Environment.SDK);

        String authDomain = "othergaetcktest.org";
        String destURLenc = "/_ah/email?continue=" + DEST_URL_ENCODED_SINGLE;
        String createdURL = UserServiceFactory.getUserService().createLoginURL(DEST_URL, authDomain);
        // TODO: verify how to check for the authDomain.
        Assert.assertTrue(createdURL.startsWith(destURLenc));
    }

    @Test
    public void testCreateLoginUrlFederatedNotSetProd() throws Exception {
        assumeEnvironment(Environment.APPSPOT);

        // Assuming Authentication Type set to Google Accounts, so org should be blank.
        String userOrg = ApiProxy.getCurrentEnvironment()
            .getAttributes()
            .get("com.google.appengine.api.users.UserService.user_organization").toString();
        Assert.assertEquals("", userOrg);

        String authDomain = "othergaetcktest.org";
        String federatedIdentity = "FedIdentTest";
        Set<String> attrRequest = new HashSet<>();

        // throws IllegalArgumentException since not set to Federated Identity.
        Exception thrownException = null;
        try {
            UserServiceFactory.getUserService().createLoginURL(DEST_URL, authDomain, federatedIdentity, attrRequest);
        } catch (Exception e) {
            thrownException = e;
        }
        // Testing exception like this since we cannot use the junit annotation in this case.
        Assert.assertEquals(IllegalArgumentException.class, thrownException.getClass());

    }

    @Test
    public void testCreateLoginUrlFederatedNotSetDev() throws Exception {
        assumeEnvironment(Environment.CAPEDWARF, Environment.SDK);

        String authDomain = "othergaetcktest.org";
        String destURLenc = "/_ah/email?continue=" + DEST_URL_ENCODED_SINGLE;
        String createdURL = UserServiceFactory.getUserService().createLoginURL(DEST_URL, authDomain);
        // TODO: verify how to check for the authDomain.
        Assert.assertTrue(createdURL.startsWith(destURLenc));
    }

    @Test
    public void testCreateLogoutUrlProd() throws Exception {
        assumeEnvironment(Environment.APPSPOT);

        String contains1 = "https://www.google.com/accounts/Logout";
        String createdURL = UserServiceFactory.getUserService().createLogoutURL(DEST_URL);

        String failMsgContains1 = "Prod url should contain: " + contains1 + " but was: " + createdURL;
        Assert.assertTrue(failMsgContains1, createdURL.contains(contains1));

        String failMsgContains2 = "Prod url should contain: " + DEST_URL_ENCODED_TRIPLE + " but was: " + createdURL;
        Assert.assertTrue(failMsgContains2, createdURL.contains(DEST_URL_ENCODED_TRIPLE));
    }

    @Test
    public void testCreateLogoutUrlDev() throws Exception {
        assumeEnvironment(Environment.CAPEDWARF, Environment.SDK);

        String destURLenc = "/_ah/logout?continue=" + DEST_URL_ENCODED_SINGLE;
        String createdURL = UserServiceFactory.getUserService().createLogoutURL(DEST_URL);
        Assert.assertTrue(createdURL.startsWith(destURLenc));
    }

    @Test
    public void testCreateLogoutUrlDevWithAuthDomain() throws Exception {
        assumeEnvironment(Environment.CAPEDWARF, Environment.SDK);

        String authDomain = "othergaetcktest.org";
        String destURLenc = "/_ah/logout?continue=" + DEST_URL_ENCODED_SINGLE;
        String createdURL = UserServiceFactory.getUserService().createLogoutURL(DEST_URL, authDomain);
        Assert.assertTrue(createdURL.startsWith(destURLenc));
    }

    /**
     * On both dev_appserver and appspot, no user should be logged in.  The Arquillian container
     * does not execute the test under an authenticated user.
     */
    @Test
    public void testUserShouldNotBeLoggedIn() {
        boolean loggedIn = userService.isUserLoggedIn();
        assertFalse("User should not be logged in for both dev_appserver and appspot.", loggedIn);
    }

    /**
     * User is not logged in so calling isUserAdmin() should throw an exception.
     */
    @Test(expected = IllegalStateException.class)
    public void testUserShouldNotBeAdmin() {
        userService.isUserAdmin();
    }

    @Test(expected = UserServiceFailureException.class)
    public void testException() {
        throw new UserServiceFailureException("Test!");
    }
}

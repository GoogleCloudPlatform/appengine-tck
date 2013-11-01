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

import com.google.appengine.api.oauth.InvalidOAuthParametersException;
import com.google.appengine.api.oauth.InvalidOAuthTokenException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.tck.event.Property;
import com.google.appengine.tck.oauth.support.OAuthServletAnswer;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This uses the Client-Side Web Applications Flow.
 *
 * Set up:
 *
 * Create a client Id, and redirect URL for type Web Application. As of today you need to use the
 * soon to be retired Cloud Console as creating the client Id is not supported in the new one yet.
 *   https://code.google.com/apis/console/b/0/?noredirect
 *
 * Create a non-admin account that will be used to get the authentication token.
 *   -Dappengine.nonAdminTestingAccount.email=
 *   -Dappengine.nonAdminTestingAccount.pw=
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */

@SuppressWarnings("UnusedDeclaration")
@RunWith(Arquillian.class)
public class ClientSideWebAppFlowTest extends OAuthTestBase {

    private OAuthService oAuthService;
    private HttpClient client;
    @Drone
    private WebDriver driver;

    private static final String GOOGLE_OAUTH2_REQUEST_TOKEN_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String SCOPE_USER = "https://www.googleapis.com/auth/userinfo.email";
    private static final String SCOPE_PROFILE = "https://www.googleapis.com/auth/userinfo.profile";

    private static Map<String, String> tokenCache;

    @Deployment
    public static WebArchive getDeployment() {
        return getBaseDeployment();
    }

    @BeforeClass
    public static void initTokenTable() {
        tokenCache = new HashMap<>();
    }

    @Before
    public void setUp() {
        initProperties();
        oAuthService = OAuthServiceFactory.getOAuthService();
        client = new DefaultHttpClient();
    }

    @Test
    @RunAsClient
    public void testUnauthenticatedRequest(@ArquillianResource URL url) throws Exception {
        String response = invokeMethodOnServerUnauthenticated(url, "getEmail");
        OAuthServletAnswer answer = new OAuthServletAnswer(response);

        assertEquals("Should NOT be authenticated: " + answer.getReturnVal(), "user is null", answer.getReturnVal());
    }

    @Test
    @RunAsClient
    public void testGetEmailValidScope(@ArquillianResource URL url) throws Exception {
        String accessToken = getGoogleAccessToken(nonAdminTestingAccountEmail, nonAdminTestingAccountPw,
            oauthClientId, oauthRedirectUri, SCOPE_USER);

        String response = invokeMethodOnServer(url, "getEmail", accessToken, SCOPE_USER);
        OAuthServletAnswer answer = new OAuthServletAnswer(response);

        assertEquals("Should have been authenticated.", nonAdminTestingAccountEmail, answer.getReturnVal());
    }

    @Test
    @RunAsClient
    public void testNoScopeThrowsInvalidOAuthTokenException(@ArquillianResource URL url) throws Exception {
        if (doIgnore("testNoScopeThrowsInvalidOAuthTokenException")) {
            return;
        }

        String accessToken = getGoogleAccessToken(nonAdminTestingAccountEmail, nonAdminTestingAccountPw,
            oauthClientId, oauthRedirectUri, SCOPE_USER);

        String emptyScope = "";
        String response = invokeMethodOnServer(url, "getEmail", accessToken, emptyScope);
        OAuthServletAnswer answer = new OAuthServletAnswer(response);

        String expectedException = InvalidOAuthTokenException.class.getName();
        assertTrue("Expected: " + expectedException, answer.getReturnVal().startsWith(expectedException));
    }

    @Test
    @RunAsClient
    public void testInvalidScopeThrowsInvalidOAuthTokenException(@ArquillianResource URL url) throws Exception {
        if (doIgnore("testInvalidScopeThrowsInvalidOAuthTokenException")) {
            return;
        }
        String accessToken = getGoogleAccessToken(nonAdminTestingAccountEmail, nonAdminTestingAccountPw,
            oauthClientId, oauthRedirectUri, SCOPE_USER);

        String invalidScope = SCOPE_PROFILE;
        String response = invokeMethodOnServer(url, "getEmail", accessToken, invalidScope);
        OAuthServletAnswer answer = new OAuthServletAnswer(response);

        String expectedException = InvalidOAuthParametersException.class.getName();
        assertTrue("Expected: " + expectedException + ", but was " + answer.getReturnVal(),
            answer.getReturnVal().startsWith(expectedException));
    }

    @Test
    @RunAsClient
    public void testUnauthorizedMultipleScopeDoesNotAuthenticate(@ArquillianResource URL url) throws Exception {
        if (doIgnore("testUnauthorizedMultipleScopeDoesNotAuthenticate")) {
            return;
        }
        String accessToken = getGoogleAccessToken(nonAdminTestingAccountEmail, nonAdminTestingAccountPw,
            oauthClientId, oauthRedirectUri, SCOPE_USER);

        String multiScope = SCOPE_USER + " " + SCOPE_PROFILE;
        String response = invokeMethodOnServer(url, "getEmail", accessToken, multiScope);
        OAuthServletAnswer answer = new OAuthServletAnswer(response);


        String expectedException = InvalidOAuthParametersException.class.getName();
        assertTrue("Multiple scopes should not match, expected " + expectedException + ", but was " + answer.getReturnVal(),
            answer.getReturnVal().contains(expectedException));
    }

    @RunAsClient
    public void testIsUserAdminFalse(@ArquillianResource URL url) throws Exception {
        String accessToken = getGoogleAccessToken(nonAdminTestingAccountEmail, nonAdminTestingAccountPw,
            oauthClientId, oauthRedirectUri, SCOPE_USER);

        String response = invokeMethodOnServer(url, "isUserAdmin", accessToken, SCOPE_USER);
        OAuthServletAnswer answer = new OAuthServletAnswer(response);

        assertTrue(nonAdminTestingAccountEmail + " should NOT be a valid admin.",
            answer.getReturnVal().equals("false"));
    }

    @Test
    @RunAsClient
    public void testGetClientId(@ArquillianResource URL url) {
        if (doIgnore("testGetClientId")) {
            return;
        }
        String accessToken = getGoogleAccessToken(nonAdminTestingAccountEmail, nonAdminTestingAccountPw,
            oauthClientId, oauthRedirectUri, SCOPE_USER);

        String response = invokeMethodOnServer(url, "getClientId", accessToken, SCOPE_USER);
        OAuthServletAnswer answer = new OAuthServletAnswer(response);

        assertEquals(oauthClientId, answer.getReturnVal());
    }

    /**
     * This can get a single scope only.  Unable to automate requesting token for multiple scopes.
     */
    private String getGoogleAccessToken(String email, String pw, String client_id, String redirect_uri, String scope) {
        Property staticToken = property("authToken");
        if (staticToken.exists()) {
            return staticToken.getPropertyValue();
        }

        // https://accounts.google.com/o/oauth2/auth?response_type=token&client_id=37298738223-o26xasxd7a217srs4t7ue1fudmt1ao1ge5.apps.googleusercontent.com&redirect_uri=https://[YOUR_TEST_APP].appspot.com/your_redirect_path&scope=https://www.googleapis.com/auth/userinfo.email

        String requestTokenUrl = String.format("%s?response_type=token&client_id=%s&redirect_uri=%s&scope=%s",
            GOOGLE_OAUTH2_REQUEST_TOKEN_URL, client_id, redirect_uri, scope);

        String cacheKey = email + ":" + requestTokenUrl;
        if (tokenCache.containsKey(cacheKey)) {
            return tokenCache.get(cacheKey);
        }
        driver.manage().deleteAllCookies();
        driver.get(requestTokenUrl);

        try {
            driver.findElement(By.id("Email")).sendKeys(email);
            driver.findElement(By.id("Passwd")).sendKeys(pw);
            driver.findElement(By.id("signIn")).submit();
        } catch (NoSuchElementException nsee) {
            String errMsg = driver.getCurrentUrl() + " ----- " + driver.getPageSource();
            throw new NoSuchElementException(nsee.toString() + " ----- " + errMsg);
        }

        // The redirect looks like:
        // https://[YOUR_TEST_APP.appspot.com/your_redirect_path#access_token=ee29.AHES6ZQPPOue4vgQhacbi__AN8Y0wLLt60sEchFaw&token_type=Bearer&expires_in=3600

        // URLEncodedUtils unable to parse parameters after #
        String redirectUrlWithToken = driver.getCurrentUrl().replace("#access_token=", "?access_token=");

        List<NameValuePair> params = null;
        try {
            params = URLEncodedUtils.parse(new URI(redirectUrlWithToken), "UTF-8");
        } catch (URISyntaxException urise) {
            throw new IllegalStateException(urise);
        }

        String accessToken = null;
        for (NameValuePair param : params) {
            if (param.getName().equals("access_token")) {
                accessToken = param.getValue();
            }
        }
        assertNotNull("Token is null:" + driver.getCurrentUrl() + " ----- " + driver.getPageSource(), accessToken);

        tokenCache.put(cacheKey, accessToken);
        return accessToken;
    }

    private String invokeMethodOnServer(URL baseUrl, String methodName, String accessToken, String scope) {

        HttpGet g = new HttpGet(String.format("%s/oauth-service-helper?method=%s", baseUrl, methodName));
        g.addHeader("Authorization", "Bearer " + accessToken);
        g.addHeader("oauth-test-scope", scope);
        HttpResponse response;
        String responseContent;
        try {
            responseContent = EntityUtils.toString(client.execute(g).getEntity());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return responseContent;
    }

    private String invokeMethodOnServerUnauthenticated(URL baseUrl, String methodName) {

        HttpGet g = new HttpGet(String.format("%s/oauth-service-helper?method=%s", baseUrl, methodName));
        HttpResponse response;
        String responseContent;
        try {
            responseContent = EntityUtils.toString(client.execute(g).getEntity());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return responseContent;
    }
}


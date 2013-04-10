package com.google.appengine.tck.users;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.UserServiceFailureException;
import com.google.appengine.tck.users.support.ServletAnswer;
import com.google.appengine.tck.util.AuthClientException;
import com.google.appengine.tck.util.GaeAuthClient;
import com.google.appengine.tck.util.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;


/**
 * Users Service tests.
 *
 * dev_appserver does not actually authenticate a user, while appserver does.
 * <p/>
 * @author smithd@google.com
 * @author ales.justin@jboss.org
 */
@RunWith(Arquillian.class)
public class UserServiceTest extends UserTestBase {

    private UserService userService;
    private GaeAuthClient authClient;

    @Before
    public void setUp() {
        userService = UserServiceFactory.getUserService();
    }

    @After
    public void tearDown() {
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

    private ServletAnswer getAuthServletAnswer(GaeAuthClient client, URL url,
                                               String method) throws IOException {
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

    @Test
    @RunAsClient
    public void pingService(@ArquillianResource URL url) throws Exception {
        ServletAnswer unAuthAnswer = getUnAuthServletAnswer(url, "env");

        String userId = System.getProperty("appengine.userId");
        String pw = System.getProperty("appengine.password");

        if (unAuthAnswer.env.equals("Production")) {
            initAuthClient(url, userId, pw);
            ServletAnswer answer = getAuthServletAnswer(authClient, url, "getEmail");
            Assert.assertEquals("UserId should be same as authenticated user:" + answer,
                userId, answer.returnVal);
        }
    }


    @Test
    public void userServiceIsAvailable() {
        UserService userService = UserServiceFactory.getUserService();
        assertNotNull("Expecting user service to be available.", userService);
    }

    @Test
    public void testSimple() throws Exception {
        String destinationURL = UserServiceFactory.getUserService().createLoginURL("destinationURL");
        assertNotNull(destinationURL);
    }

    @Test
    // TODO: Re-enable check of authenticated user.
    public void testGetCurrentUser() {
        User user = userService.getCurrentUser();
        if (isRuntimeProduction()) {
            //assertNotNull("User should be valid under prod.", user);
            assertNull("No User should be valid under prod.", user);
        } else {
            assertNull("User should be null under dev_appserver", user);
        }
    }

    @Test
    // TODO: Re-enable check of authenticated user.
    public void testIsUserLoggedIn() {
        boolean loggedIn = userService.isUserLoggedIn();
        if (isRuntimeProduction()) {
            //assertTrue("User should be logged in under prod.", loggedIn);
            assertFalse("No User should be logged in under prod.", loggedIn);
        } else {
            assertFalse("User should not be logged in under dev_appserver", loggedIn);
        }
    }

//  @Test
//  // TODO: Re-enable check of admin authenticated user.
//  public void testIsUserAdmin() {
//    if (!isRuntimeProduction()) {
//      thrown.expect(IllegalStateException.class);
//    }
//    boolean userAdmin = userService.isUserAdmin();
//    if (isRuntimeProduction()) {
//      assertTrue("Whoever is running this test in prod is an admin.", userAdmin);
//    }
//  }

    @Test(expected = IllegalStateException.class) // per javadoc
    // TODO: Remove after test runs with authenticated admin user.
    public void testIsUserAdmin() {
        boolean userAdmin = userService.isUserAdmin();
    }

    @Test(expected = UserServiceFailureException.class)
    public void testException() {
        throw new UserServiceFailureException("Test!");
    }
}

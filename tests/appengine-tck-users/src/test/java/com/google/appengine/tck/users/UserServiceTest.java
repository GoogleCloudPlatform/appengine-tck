package com.google.appengine.tck.users;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.UserServiceFailureException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


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

    @Before
    public void setUp() {
        userService = UserServiceFactory.getUserService();
    }

    @After
    public void tearDown() {
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

    @Test
    // TODO: Remove after test runs with authenticated admin user.
    public void testIsUserAdmin() {
        boolean userAdmin = userService.isUserAdmin();
    }

    @Test(expected = UserServiceFailureException.class)
    public void testException() {
        throw new UserServiceFailureException("Test!");
    }
}

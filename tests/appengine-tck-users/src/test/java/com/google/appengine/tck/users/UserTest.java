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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tck.login.UserIsLoggedIn;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test various user configurations.
 */
@RunWith(Arquillian.class)
public class UserTest extends UserTestBase {

    @Test
    @UserIsLoggedIn(email = "tck@appengine-tck.org")
    public void testAnnotation() {
        User user = UserServiceFactory.getUserService().getCurrentUser();
        assertNotNull(user);
    }

    /**
     * Tests constructor.
     * <p>Both email and authDomain are valid string, no exception.
     */
    @Test
    public void testUser() {
        User user = new User("someone@example.com", "gmail.com");
        assertNotNull(user);
    }

    /**
     * Tests constructor.
     * <p>email is null, expect NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testUser_emailNull() {
        new User(null, "gmail.com");
    }

    /**
     * Tests constructor.
     * <p>authDomain is null, expect NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testUser_authNull() {
        new User("someone@example.com", null);
    }

    /**
     * Tests compareTo null.
     */
    @Test(expected = NullPointerException.class)
    public void testCompareTo_null() {
        User user = new User("a", "b");
        user.compareTo(null);
    }

    /**
     * Tests compareTo email1 less than email2.
     */
    @Test
    public void testCompareTo_less() {
        User user1 = new User("aa", "dd");
        User user2 = new User("bb", "cc");
        assertTrue(user1.compareTo(user2) < 0);
    }

    /**
     * Tests compareTo email1 equal to email2.
     * <p>auth1 not equal to auth2, but irrelevant to return value of compareTo.
     */
    @Test
    public void testCompareTo_equal() {
        User user1 = new User("aa", "dd");
        User user2 = new User("aa", "cc");
        assertEquals(user1.compareTo(user2), 0);
    }

    /**
     * Tests compareTo email1 greater than email2.
     */
    @Test
    public void testCompareTo_greater() {
        User user1 = new User("xx", "dd");
        User user2 = new User("aa", "cc");
        assertTrue(user1.compareTo(user2) > 0);
    }

    /**
     * Tests equals email eq and auth eq.
     */
    @Test
    public void testEquals_eq() {
        User user1 = new User("abc", "xyz");
        User user2 = new User("abc", "xyz");
        assertTrue(user1.equals(user2));
    }

    /**
     * Tests equals email eq but auth not eq.
     */
    @Test
    public void testEquals_neqAuthDiff() {
        User user1 = new User("abc", "xyz");
        User user2 = new User("abc", "uvw");
        assertFalse(user1.equals(user2));
    }

    /**
     * Tests equals email not eq but auth eq.
     */
    @Test
    public void testEquals_neqEmailDiff() {
        User user1 = new User("abc", "xyz");
        User user2 = new User("def", "xyz");
        assertFalse(user1.equals(user2));
    }

    /**
     * Tests equals compare to null.
     */
    @Test
    public void testEquals_null() {
        User user = new User("abc", "xyz");
        assertFalse(user.equals(null));
    }

    /**
     * Tests equals compare to a non-User object.
     */
    @Test
    public void testEquals_generalObject() {
        User user = new User("abc", "xyz");
        assertFalse(user.equals(new Object()));
    }

    /**
     * Tests hashCode.
     * <p>Given user1.equals(user2) returns true,
     * expect hashCode of user1 == hashCode of user2.
     */
    @Test
    public void testHashCode() {
        User user1 = new User("aaa@example.com", "gmail.com");
        User user2 = new User("aaa@example.com", "gmail.com");
        assertTrue(user1.equals(user2));
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    /**
     * Tests getNickname, email matches authDomain.
     */
    @Test
    public void testGetNickname_matchDomain() {
        String username = "01someone23456789";
        User user = new User(username + "@example.com", "example.com");
        assertEquals(user.getNickname(), username);
    }

    /**
     * Tests getNickname, email does not match authDomain.
     */
    @Test
    public void testGetNickname_diffDomain() {
        String email = "someone@example.com";
        User user = new User(email, "gmail.com");
        assertEquals(user.getNickname(), email);
    }

    /**
     * Tests getNickname, username contain non-alnum char.
     */
    @Test
    public void testGetNickname_nonAlnum() {
        // this one passed google account sign-in email validity check
        String username = "a~b`c#d$e%f^g&h*i'j_k=l+m{n}o|p/q";
        String email = username + "@gmail.com";
        User user = new User(email, "gmail.com");
        assertEquals(user.getNickname(), username);
    }

    /**
     * Tests getNickname. Illegal email.
     * <p/>
     * google account authentication ensures email must be of the form:
     * <code>myname@example.com</code>
     */
    @Test
    public void testGetNickname_illegalEmail() {
        // no @ in email
        {
            String email = "someone.example.com";
            User user = new User(email, "example.com");
            assertTrue(user.getNickname().equals(email));
        }

        // email only @
        {
            String email = "@";
            User user = new User(email, "example.com");
            assertTrue(user.getNickname().equals(email));
        }

        // two different @authDomain in email, match second
        {
            String prefix = "someone@example.com";
            String email = prefix + "@gmail.com";
            User user = new User(email, "gmail.com");
            assertTrue(user.getNickname().equals(prefix));
        }

        // two different @autDomain in email, match first
        {
            String username = "someone";
            String email = username + "@example.com@gmail.com";
            User user = new User(email, "example.com");
            assertTrue(user.getNickname().equals(username));
        }

        // two identical @authDomain in email, match first
        {
            String username = "someone";
            String suffix = "example.com";
            String email = username + "@" + suffix + "@" + suffix;
            User user = new User(email, suffix);
            assertTrue(user.getNickname().equals(username));
        }

        // email is empty string
        {
            String email = "";
            User user = new User(email, "gmail.com");
            assertTrue(user.getNickname().equals(""));
        }

        // authDomain is empty string
        {
            String username = "someone";
            String email = username + "@google.com" + "@example.com" + "@gmail.com";
            User user = new User(email, "");
            assertTrue(user.getNickname().equals(username));
        }

        // email begin with @authDomain
        {
            String email = "@gmail.com";
            User user = new User(email, "gmail.com");
            assertTrue(user.getNickname().equals(""));
        }
    }
}

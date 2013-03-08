package com.google.appengine.tck.users;

import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Users base class.
 */
public abstract class UserTestBase extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getTckDeployment();
        war.addClass(UserTestBase.class);
        return war;
    }
}
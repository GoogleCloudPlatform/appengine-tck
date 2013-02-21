package com.google.appengine.tck.users;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.appengine.tck.base.TestBase;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 */
public abstract class UserTestBase extends TestBase {

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getTckDeployment();
        war.addClass(UserTestBase.class);
        return war;
    }
}
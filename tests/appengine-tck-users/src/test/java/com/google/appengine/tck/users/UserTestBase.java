package com.google.appengine.tck.users;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.users.support.ServletAnswer;
import com.google.appengine.tck.users.support.UserServiceServlet;
import com.google.appengine.tck.util.AuthClientException;
import com.google.appengine.tck.util.GaeAuthClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Users base class.
 */
public abstract class UserTestBase extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext();
        context.setWebXmlFile("web-userservice.xml");

        WebArchive war = getTckDeployment(context);

        war.addClasses(UserTestBase.class, GaeAuthClient.class, AuthClientException.class)
            .addClasses(UserServiceServlet.class, ServletAnswer.class);
        return war;
    }

}
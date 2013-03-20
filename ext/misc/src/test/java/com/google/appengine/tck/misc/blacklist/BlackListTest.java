package com.google.appengine.tck.misc.blacklist;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test BlackList.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class BlackListTest extends TestBase {
    protected Context context;

    @Deployment
    public static WebArchive getDeployment() {
        return getTckDeployment();
    }

    @Test(expected = NoClassDefFoundError.class)
    public void testDirectInitialization() throws Exception {
        context = new InitialContext();
    }

    @Test(expected = IllegalAccessException.class)
    public void testReflectionInitialization() throws Exception {
        Class<?> clazz = Class.forName(InitialContext.class.getName());
        clazz.newInstance();
    }
}

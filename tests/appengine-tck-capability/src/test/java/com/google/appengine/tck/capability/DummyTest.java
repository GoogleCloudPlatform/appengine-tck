package com.google.appengine.tck.capability;

import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Sandbox test.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class DummyTest extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getTckDeployment();
        war.addAsWebInfResource(new StringAsset("qwert"), "dummy.txt");
        return war;
    }

    @Test
    public void testDummy() {
    }
}

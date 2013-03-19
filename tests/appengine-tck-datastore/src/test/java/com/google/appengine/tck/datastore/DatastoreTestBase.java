package com.google.appengine.tck.datastore;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Split due to dup @Deployment issue.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class DatastoreTestBase extends DatastoreHelperTestBase {
    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getHelperDeployment();
        war.addClass(DatastoreTestBase.class);
        war.addAsWebInfResource("datastore-indexes.xml");
        return war;
    }
}

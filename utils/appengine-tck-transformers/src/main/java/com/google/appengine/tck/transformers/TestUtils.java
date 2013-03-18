package com.google.appengine.tck.transformers;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tck.event.TestLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycles;

/**
 * This should only depend on GAE API.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestUtils {
    // a hack to clean the DS after test
    public static void clean() {
        try {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            TestLifecycleEvent event = TestLifecycles.createServiceLifecycleEvent(null, ds);
            TestLifecycles.after(event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

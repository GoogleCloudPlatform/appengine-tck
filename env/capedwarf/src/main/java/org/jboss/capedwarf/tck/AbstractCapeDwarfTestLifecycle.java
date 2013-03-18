package org.jboss.capedwarf.tck;

import com.google.appengine.tck.event.TestLifecycle;
import com.google.appengine.tck.event.TestLifecycleEvent;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractCapeDwarfTestLifecycle implements TestLifecycle {
    public void before(TestLifecycleEvent event) {
    }

    public void after(TestLifecycleEvent event) {
    }

    protected static Asset getCompatibility(String key, String value) {
        return null;
    }
}

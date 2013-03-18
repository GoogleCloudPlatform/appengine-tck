package org.jboss.capedwarf.tck;

import java.lang.reflect.Method;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.log.LogService;
import com.google.appengine.tck.event.ServiceLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycle;
import com.google.appengine.tck.event.TestLifecycleEvent;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class CapeDwarfServicesLifecycle implements TestLifecycle {
    public void before(TestLifecycleEvent event) {
    }

    public void after(TestLifecycleEvent event) {
        if (event instanceof ServiceLifecycleEvent) {
            Object service = ServiceLifecycleEvent.class.cast(event).getService();
            if (service instanceof DatastoreService) {
                invoke(service, "clearCache");
            } else if (service instanceof LogService) {
                invoke(service, "clearLog");
            }
        }
    }

    protected void invoke(Object service, String method) {
        final Class<?> clazz = service.getClass();
        try {
            Method m = clazz.getMethod(method);
            m.setAccessible(true);
            m.invoke(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

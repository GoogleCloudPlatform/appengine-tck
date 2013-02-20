package org.jboss.capedwarf.tck;

import java.lang.reflect.Method;

import com.google.appengine.api.log.LogService;
import com.google.appengine.tck.base.ServicesLifecycle;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class CapeDwarfServicesLifecycle implements ServicesLifecycle {
    public <T> void before(T service) {
    }

    public <T> void after(T service) {
        if (service instanceof LogService) {
            final Class<?> clazz = service.getClass();
            try {
                Method clearLog = clazz.getMethod("clearLog");
                clearLog.invoke(service);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

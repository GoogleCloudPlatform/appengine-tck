package com.google.appengine.tck.base;

import java.util.ServiceLoader;

/**
 * Service lifecycle hooks.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ServicesLifecycles {
    protected static Iterable<ServicesLifecycle> getServicesLifecycles() {
        return ServiceLoader.load(ServicesLifecycle.class, TestBase.class.getClassLoader());
    }

    public static void before(Object service) {
        for (ServicesLifecycle sl : getServicesLifecycles()) {
            sl.before(service);
        }
    }

    public static void after(Object service) {
        for (ServicesLifecycle sl : getServicesLifecycles()) {
            sl.after(service);
        }
    }
}
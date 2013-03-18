package com.google.appengine.tck.event;

import java.util.ServiceLoader;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Test lifecycle hooks.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestLifecycles {
    protected static Iterable<TestLifecycle> getServicesLifecycles() {
        return getServicesLifecycles(TestBase.class.getClassLoader());
    }

    public static Iterable<TestLifecycle> getServicesLifecycles(ClassLoader cl) {
        return ServiceLoader.load(TestLifecycle.class, cl);
    }

    public static void before(TestLifecycleEvent event) {
        for (TestLifecycle sl : getServicesLifecycles()) {
            sl.before(event);
        }
    }

    public static void after(TestLifecycleEvent event) {
        for (TestLifecycle sl : getServicesLifecycles()) {
            sl.after(event);
        }
    }

    public static TestLifecycleEvent createTestContextLifecycleEvent(Class<?> caller, TestContext context) {
        return new TestContextLifecycleEventImpl(caller, context);
    }

    public static TestLifecycleEvent createServiceLifecycleEvent(Class<?> caller, Object service) {
        return new ServiceLifecycleEventImpl(caller, service);
    }
}
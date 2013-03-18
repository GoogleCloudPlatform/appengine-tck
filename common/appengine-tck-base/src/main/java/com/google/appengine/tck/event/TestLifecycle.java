package com.google.appengine.tck.event;

/**
 * Test lifecycle hook.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface TestLifecycle {
    void before(TestLifecycleEvent event);
    void after(TestLifecycleEvent event);
}
package com.google.appengine.tck.event;

import com.google.appengine.tck.base.TestContext;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface TestContextLifecycleEvent extends TestLifecycleEvent {
    TestContext getTestContext();
}

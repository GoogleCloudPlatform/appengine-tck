package com.google.appengine.tck.event;

import com.google.appengine.tck.base.TestContext;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class TestContextLifecycleEventImpl extends AbstractTestLifecycleEventImpl<TestContext> implements TestContextLifecycleEvent {
    public TestContextLifecycleEventImpl(Class<?> caller, TestContext context) {
        super(caller, context);
    }

    public TestContext getTestContext() {
        return getContext();
    }
}

package com.google.appengine.tck.event;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class TestLifecycleEventImpl implements TestLifecycleEvent {
    private Class<?> caller;

    protected TestLifecycleEventImpl(Class<?> caller) {
        this.caller = caller;
    }

    public Class<?> getCallerClass() {
        return caller;
    }
}

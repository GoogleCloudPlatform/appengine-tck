package com.google.appengine.tck.event;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractTestLifecycleEventImpl<T> extends TestLifecycleEventImpl {
    private T context;

    public AbstractTestLifecycleEventImpl(Class<?> caller, T context) {
        super(caller);
        this.context = context;
    }

    protected T getContext() {
        return context;
    }
}

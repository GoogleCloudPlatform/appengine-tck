package com.google.appengine.tck.event;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class ServiceLifecycleEventImpl extends AbstractTestLifecycleEventImpl<Object> implements ServiceLifecycleEvent {
    public ServiceLifecycleEventImpl(Class<?> caller, Object service) {
        super(caller, service);
    }

    public Object getService() {
        return getContext();
    }
}

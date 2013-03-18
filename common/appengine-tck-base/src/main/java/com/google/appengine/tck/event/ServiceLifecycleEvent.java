package com.google.appengine.tck.event;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface ServiceLifecycleEvent extends TestLifecycleEvent {
    Object getService();
}

package com.google.appengine.tck.event;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface TestLifecycleEvent {
    /**
     * Can be null if cannot be determined.
     *
     * @return class or null if unknown
     */
    Class<?> getCallerClass();
}

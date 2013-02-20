package com.google.appengine.tck.base;

/**
 * Service lifecycle hook.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface ServicesLifecycle {
    <T> void before(T service);
    <T> void after(T service);
}
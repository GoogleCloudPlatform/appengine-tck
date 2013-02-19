package com.google.appengine.testing.e2e.multisuite;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface MultiProvider {
    void provide(MultiContext context) throws Exception;
}

package com.google.appengine.tck.base;

/**
 * Enhance TestContext just before we create web archive.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface TestContextEnhancer {
    void enhance(TestContext context);
}
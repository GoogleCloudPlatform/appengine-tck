package com.google.appengine.testing.e2e.multisuite.scan;

import com.google.appengine.testing.e2e.multisuite.MultiContext;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface ScanStrategy {
    boolean doMerge(MultiContext context, Class<?> clazz);
}

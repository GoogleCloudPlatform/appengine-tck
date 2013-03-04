package com.google.appengine.tck.transformers;

import java.lang.instrument.ClassFileTransformer;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface MatchingClassFileTransformer extends ClassFileTransformer {
    boolean match(String className);
}

package com.google.appengine.tck.coverage;

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface MethodExclusion {
    boolean exclude(ClassFile clazz, MethodInfo methodInfo);
}

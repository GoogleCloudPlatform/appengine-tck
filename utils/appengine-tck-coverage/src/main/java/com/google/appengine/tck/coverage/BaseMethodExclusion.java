package com.google.appengine.tck.coverage;

import java.lang.reflect.Modifier;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BaseMethodExclusion implements MethodExclusion {
    public boolean exclude(ClassFile clazz, MethodInfo mi) {
        if (Modifier.isPublic(mi.getAccessFlags()) == false) {
            return true;
        }

        AnnotationsAttribute aa = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
        if (aa != null) {
            Annotation annotation = aa.getAnnotation(Deprecated.class.getName());
            if (annotation != null) {
                return true;
            }
        }

        return false;
    }
}

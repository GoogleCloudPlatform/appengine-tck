/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.tck.coverage;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BaseMethodExclusion implements MethodExclusion {
    private static final Set<String> EXCLUDES;

    static {
        EXCLUDES = new HashSet<>();
        EXCLUDES.add("equals@(Ljava/lang/Object;)Z");
        EXCLUDES.add("hashCode@()I");
        EXCLUDES.add("toString@()Ljava/lang/String;");
    }

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

        String methodName = mi.getName();
        String descriptor = mi.getDescriptor();
        return EXCLUDES.contains(methodName + "@" + descriptor);
    }
}

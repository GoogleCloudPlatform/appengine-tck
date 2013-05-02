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

package com.google.appengine.tck.transformers.dn;

import com.google.appengine.tck.transformers.JavassistTransformer;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DatastoreTestCaseTransformer extends JavassistTransformer {
    protected boolean isAlreadyTransformed(CtClass clazz) throws Exception {
        return false;
    }

    /**
     * Change method.
     */
    protected void transform(CtClass clazz) throws Exception {
        CtMethod isUseHelper = clazz.getDeclaredMethod("isUseHelper");
        isUseHelper.setBody("{return false;}");
    }

    // Old code
    protected void transformOld(CtClass clazz) throws Exception {
        CtMethod setUp = clazz.getDeclaredMethod("setUp");
        String newName = setUp.getName() + System.currentTimeMillis();
        CtMethod copy = CtNewMethod.copy(setUp, newName, clazz, null);
        clazz.addMethod(copy);
        setUp.setBody(
            "{" +
                "   com.google.apphosting.api.ApiProxy.Environment previous = com.google.apphosting.api.ApiProxy.getCurrentEnvironment();" +
                "   try {" +
                "       " + newName + "();" +
                "   } finally {" +
                "       com.google.apphosting.api.ApiProxy.setEnvironmentForCurrentThread(previous);" +
                "   }" +
                "}");

        CtMethod tearDown = clazz.getDeclaredMethod("tearDown");
        newName = tearDown.getName() + System.currentTimeMillis();
        copy = CtNewMethod.copy(tearDown, newName, clazz, null);
        clazz.addMethod(copy);
        tearDown.setBody(
            "{" +
                "   com.google.apphosting.api.ApiProxy.Environment previous = com.google.apphosting.api.ApiProxy.getCurrentEnvironment();" +
                "   try {" +
                "       " + newName + "();" +
                "    } finally {" +
                "       com.google.apphosting.api.ApiProxy.setEnvironmentForCurrentThread(previous);" +
                "   }" +
                "}");
    }
}

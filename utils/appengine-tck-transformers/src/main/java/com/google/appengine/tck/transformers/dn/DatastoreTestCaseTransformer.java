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

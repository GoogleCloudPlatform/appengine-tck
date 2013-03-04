package com.google.appengine.tck.transformers;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class JavassistTransformer implements ClassFileTransformer {
    protected Logger log = Logger.getLogger(getClass().getName());

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassPool pool = new ClassPool();
            pool.appendClassPath(new LoaderClassPath(loader));
            CtClass clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
            if (isAlreadyTransformed(clazz) == false) {
                log.info("Transforming " + className + " with " + getClass().getSimpleName());
                transform(clazz);
            } else {
                log.fine(className + " is already transformed with " + getClass().getSimpleName());
            }
            return clazz.toBytecode();
        } catch (Exception e) {
            throw new IllegalClassFormatException(e.getMessage());
        }
    }

    protected abstract boolean isAlreadyTransformed(CtClass clazz) throws Exception;

    protected abstract void transform(CtClass clazz) throws Exception;
}

package com.google.appengine.tck.transformers;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class JavassistTransformer implements ClassFileTransformer {
    protected Logger log = Logger.getLogger(getClass().getName());

    public byte[] transform(final ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        final ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
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
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    protected abstract boolean isAlreadyTransformed(CtClass clazz) throws Exception;

    protected abstract void transform(CtClass clazz) throws Exception;

    protected static Class<?> loadClas(CtClass clazz, String annotationClassname) throws ClassNotFoundException {
        return clazz.getClassPool().getClassLoader().loadClass(annotationClassname);
    }

    protected static void addAnnotation(CtClass clazz, CtMethod method, String annotationClassName) throws Exception {
        ClassFile ccFile = clazz.getClassFile();
        ConstPool constPool = ccFile.getConstPool();

        AttributeInfo info = method.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        AnnotationsAttribute attr;
        if (info == null) {
            attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        } else {
            attr = AnnotationsAttribute.class.cast(info);
        }

        constPool.addUtf8Info(annotationClassName);

        Annotation annotation = new Annotation(annotationClassName, constPool);
        attr.addAnnotation(annotation);

        method.getMethodInfo().addAttribute(attr);
    }
}

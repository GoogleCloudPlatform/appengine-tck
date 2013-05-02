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

package com.google.appengine.tck.transformers;

import java.lang.reflect.Modifier;
import java.util.Random;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ArquillianJUnitTransformer extends ShrinkWrapTransformer {

    protected boolean isAlreadyTransformed(CtClass clazz) throws Exception {
        return clazz.hasAnnotation(RunWith.class);
    }

    protected void transform(CtClass clazz) throws Exception {
        addRunWithArquillian(clazz);
        addDeploymentMethod(clazz);
        addTestAnnotations(clazz);
        addLifecycleMethods(clazz);
    }

    protected void addRunWithArquillian(CtClass clazz) throws Exception {
        ClassFile ccFile = clazz.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        AttributeInfo info = ccFile.getAttribute(AnnotationsAttribute.visibleTag);
        AnnotationsAttribute attr;
        if (info == null) {
            attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        } else {
            attr = AnnotationsAttribute.class.cast(info);
        }

        String runWithClassName = RunWith.class.getName();
        String arquillianClassName = Arquillian.class.getName();

        constPool.addUtf8Info(runWithClassName);
        constPool.addUtf8Info(arquillianClassName);

        Annotation annotation = new Annotation(runWithClassName, constPool);
        annotation.addMemberValue("value", new ClassMemberValue(arquillianClassName, constPool));
        attr.addAnnotation(annotation);

        ccFile.addAttribute(attr);
    }

    protected void addDeploymentMethod(CtClass clazz) throws Exception {
        ClassPool pool = clazz.getClassPool();
        CtClass archiveClass = pool.get(WebArchive.class.getName());

        CtMethod m = new CtMethod(archiveClass, "getDeployment", new CtClass[]{}, clazz);
        m.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
        addDeploymentAnnotation(clazz, m);
        m.setBody(getDeploymentMethodBody(clazz));

        clazz.addMethod(m);
    }

    protected static WebArchive createWar() {
        final String archiveName = System.getProperty("archive.name");
        return (archiveName != null) ? ShrinkWrap.create(WebArchive.class, archiveName) : ShrinkWrap.create(WebArchive.class);
    }

    protected abstract String getDeploymentMethodBody(CtClass clazz) throws Exception;

    protected void addTestAnnotations(CtClass clazz) throws Exception {
        for (CtMethod m : clazz.getMethods()) {
            if (isTestMethod(m)) {
                addTestAnnotation(clazz, m);
            }
        }
    }

    protected boolean isTestMethod(CtMethod m) throws Exception {
        return m.getName().startsWith("test") && m.getParameterTypes().length == 0 && ((m.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC);
    }

    protected void addDeploymentAnnotation(CtClass clazz, CtMethod method) throws Exception {
        addAnnotation(clazz, method, Deployment.class.getName());
    }

    protected void addTestAnnotation(CtClass clazz, CtMethod method) throws Exception {
        addAnnotation(clazz, method, Test.class.getName());
    }

    protected void addLifecycleMethods(CtClass clazz) throws Exception {
        Random random = new Random();

        ClassPool pool = clazz.getClassPool();
        CtClass voidClass = pool.get(Void.TYPE.getName());
        CtClass exceptionClass = pool.get(Exception.class.getName());

        ClassFile ccFile = clazz.getClassFile();
        ConstPool constPool = ccFile.getConstPool();

        CtMethod before = new CtMethod(voidClass, "before" + random.nextInt(), new CtClass[]{}, clazz);
        before.setModifiers(Modifier.PUBLIC);
        before.setBody("{" + setUpSrc() + "}");
        before.setExceptionTypes(new CtClass[]{exceptionClass});
        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        String beforeClassName = Before.class.getName();
        constPool.addUtf8Info(beforeClassName);
        Annotation annotation = new Annotation(beforeClassName, constPool);
        attr.addAnnotation(annotation);
        before.getMethodInfo().addAttribute(attr);
        clazz.addMethod(before);

        CtMethod after = new CtMethod(voidClass, "after" + random.nextInt(), new CtClass[]{}, clazz);
        after.setModifiers(Modifier.PUBLIC);
        after.setBody("{" + tearDownSrc() + "}");
        after.setExceptionTypes(new CtClass[]{exceptionClass});
        attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        String afterClassName = After.class.getName();
        constPool.addUtf8Info(afterClassName);
        annotation = new Annotation(afterClassName, constPool);
        attr.addAnnotation(annotation);
        after.getMethodInfo().addAttribute(attr);
        clazz.addMethod(after);
    }

    protected String setUpSrc() {
        return "setUp();";
    }

    protected String tearDownSrc() {
        return "tearDown();";
    }
}

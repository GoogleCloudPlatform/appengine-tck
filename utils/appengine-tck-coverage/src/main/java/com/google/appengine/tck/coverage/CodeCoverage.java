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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author Stuart Douglas
 */
@SuppressWarnings("unchecked")
public class CodeCoverage {
    private static final Logger log = Logger.getLogger(CodeCoverage.class.getName());
    private static final Tuple DUMMY = new Tuple("<init>", "");

    private final Map<String, Boolean> annotations = new HashMap<>();
    private final Map<String, List<Tuple>> descriptors = new TreeMap<>();
    private final Map<String, List<String>> supers = new HashMap<>();
    private final Map<String, Map<Tuple, Set<CodeLine>>> report = new TreeMap<>();

    private static ClassLoader toClassLoader(File classesToScan) throws MalformedURLException {
        return new URLClassLoader(new URL[]{classesToScan.toURI().toURL()}, CodeCoverage.class.getClassLoader());
    }

    public static void report(String module, ClassLoader classLoader, File baseDir, File classesToScan, MethodExclusion exclusion, String... classes) throws Exception {
        if (classes == null || classes.length == 0) {
            log.warning("No classes to check!");
            return;
        }

        if (classLoader == null) {
            classLoader = toClassLoader(classesToScan);
        }

        CodeCoverage cc = new CodeCoverage(classLoader, exclusion, classes);
        cc.scan(classesToScan, "");
        cc.print(
            SoutPrinter.INSTANCE,
            new HtmlPrinter(baseDir, new File(classesToScan, "../../index.html"), module),
            new CsvPrinter(new File(classesToScan, "../../coverage-results.csv"))
        );
    }

    private CodeCoverage(ClassLoader classLoader, MethodExclusion exclusion, String... classes) throws Exception {
        for (String clazz : classes) {
            Map<Tuple, Set<CodeLine>> map = new TreeMap<>();
            report.put(clazz, map);

            List<Tuple> mds = new ArrayList<>();
            descriptors.put(clazz, mds);

            InputStream is = classLoader.getResourceAsStream(clazz.replace(".", "/") + ".class");
            ClassFile classFile = getClassFile(clazz, is);

            List<MethodInfo> methods = classFile.getMethods();
            for (MethodInfo m : methods) {
                if (exclusion.exclude(classFile, m) == false) {
                    String descriptor = m.getDescriptor();
                    Tuple tuple = new Tuple(m.getName(), descriptor);
                    map.put(tuple, new TreeSet<CodeLine>());
                    mds.add(tuple);
                }
            }

            if (isAccessFlagSet(classFile.getAccessFlags(), AccessFlag.ANNOTATION)) {
                boolean hasAttributes = methods.size() > 0;
                annotations.put(clazz, hasAttributes);
                if (hasAttributes == false) {
                    map.put(DUMMY, new TreeSet<CodeLine>());
                    mds.add(DUMMY);
                }
            } else {
                fillSupers(classFile);
            }
        }
    }

    protected static boolean isAccessFlagSet(int accessFlags, int constant) {
        return ((accessFlags & constant) == constant);
    }

    protected void fillSupers(ClassFile clazz) {
        String name = clazz.getName();
        List<String> list = supers.get(name);
        if (list == null) {
            list = new ArrayList<>();
            supers.put(name, list);
        }
        if (clazz.isInterface()) {
            Collections.addAll(list, clazz.getInterfaces());
        } else {
            String superclass = clazz.getSuperclass();
            if (superclass != null) {
                list.add(superclass);
            }
        }
    }

    protected void scan(File current, String fqn) throws Exception {
        if (current.isFile()) {
            if (fqn.endsWith(".class")) {
                FileInputStream fis = new FileInputStream(current);
                ClassFile cf = getClassFile(fqn, fis);
                checkClassFile(cf);
            }
        } else {
            File[] files = current.listFiles();
            for (File file : files) {
                scan(file, fqn.length() > 0 ? fqn + "." + file.getName() : file.getName());
            }
        }
    }

    private ClassFile getClassFile(Object info, InputStream is) throws IOException {
        if (is == null) {
            throw new IOException("Missing class: " + info);
        }

        ClassFile cf;
        try {
            cf = new ClassFile(new DataInputStream(is));
        } finally {
            is.close();
        }
        return cf;
    }

    protected void checkClassFile(ClassFile file) throws Exception {
        Map<Integer, Triple> calls = new HashMap<>();

        ConstPool pool = file.getConstPool();
        for (int i = 1; i < pool.getSize(); ++i) {
            // we have a method call
            BytecodeUtils.Ref ref = BytecodeUtils.getRef(pool, i);
            String className = ref.getClassName(pool, i);
            if (className != null) {
                String methodName = ref.getName(pool, i);
                String methodDesc = ref.getDesc(pool, i);
                fillCalls(i, className, methodName, methodDesc, calls);
            }
        }

        if (calls.isEmpty() && annotations.isEmpty()) {
            return;
        }

        String className = file.getName();

        AnnotationsAttribute faa = (AnnotationsAttribute) file.getAttribute(AnnotationsAttribute.visibleTag);
        checkAnnotations(className, DUMMY.getMethodName(), faa, -1);

        List<MethodInfo> methods = file.getMethods();
        for (MethodInfo m : methods) {
            try {
                // ignore abstract methods
                if (m.getCodeAttribute() == null) {
                    continue;
                }

                AnnotationsAttribute maa = (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag);
                boolean annotationsChecked = false;
                int firstLine = -1;

                CodeIterator it = m.getCodeAttribute().iterator();
                while (it.hasNext()) {
                    // loop through the bytecode
                    final int index = it.next();
                    final int line = m.getLineNumber(index);

                    if (annotationsChecked == false) {
                        annotationsChecked = true;
                        firstLine = line;
                        checkAnnotations(className, m.getName(), maa, line - 2); // -2 to get the line above the method
                    }

                    int op = it.byteAt(index);
                    // if the bytecode is a method invocation
                    if (op == CodeIterator.INVOKEVIRTUAL || op == CodeIterator.INVOKESTATIC || op == CodeIterator.INVOKEINTERFACE || op == CodeIterator.INVOKESPECIAL) {
                        int val = it.s16bitAt(index + 1);
                        Triple triple = calls.get(val);
                        if (triple != null) {
                            Map<Tuple, Set<CodeLine>> map = report.get(triple.className);
                            Set<CodeLine> set = map.get(triple.tuple);
                            CodeLine cl = new CodeLine(className, m.getName(), line);
                            set.add(cl.modify()); // check for .jsp, etc
                        }
                    }
                }

                ParameterAnnotationsAttribute paa = (ParameterAnnotationsAttribute) m.getAttribute(ParameterAnnotationsAttribute.visibleTag);
                if (paa != null) {
                    Annotation[][] paas = paa.getAnnotations();
                    if (paas != null) {
                        for (Annotation[] params : paas) {
                            for (Annotation a : params) {
                                for (Map.Entry<String, Boolean> entry : annotations.entrySet()) {
                                    if (entry.getKey().equals(a.getTypeName())) {
                                        checkAnnotation(className, m.getName(), firstLine - 1, entry.getValue(), entry.getKey(), a);
                                    }
                                }
                            }
                        }
                    }
                }

                m.getCodeAttribute().computeMaxStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void checkAnnotations(String clazz, String member, AnnotationsAttribute aa, int line) {
        if (aa != null) {
            for (Map.Entry<String, Boolean> entry : annotations.entrySet()) {
                String annotation = entry.getKey();
                Annotation ann = aa.getAnnotation(annotation);
                if (ann != null) {
                    checkAnnotation(clazz, member, line, entry.getValue(), annotation, ann);
                }
            }
        }
    }

    protected void checkAnnotation(String clazz, String member, int line, boolean hasMembers, String annotation, Annotation ann) {
        Map<Tuple, Set<CodeLine>> map = report.get(annotation);
        if (hasMembers) {
            List<Tuple> tuples = descriptors.get(annotation);
            for (Tuple tuple : tuples) {
                Set<CodeLine> set = map.get(tuple);
                String name = tuple.getMethodName();
                MemberValue mv = ann.getMemberValue(name);
                if (mv != null) {
                    set.add(new CodeLine(clazz, member, line));
                }
            }
        } else {
            Set<CodeLine> set = map.get(DUMMY);
            set.add(new CodeLine(clazz, member, line));
        }
    }

    protected boolean fillCalls(int i, String className, String methodName, String methodDesc, Map<Integer, Triple> calls) {
        List<Tuple> mds = descriptors.get(className);
        if (mds != null) {
            for (Tuple tuple : mds) {
                if (tuple.methodName.equals(methodName) && tuple.methodDesc.equals(methodDesc)) {
                    calls.put(i, new Triple(className, tuple));
                    return true;
                }
            }
        }
        List<String> classes = supers.get(className);
        if (classes != null) {
            for (String clazz : classes) {
                if (fillCalls(i, clazz, methodName, methodDesc, calls)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void print(Printer... printers) throws Exception {
        for (Printer printer : printers) {
            printer.print(report);
        }
    }

    static class Triple {
        private String className;
        private Tuple tuple;

        private Triple(String className, Tuple tuple) {
            this.className = className;
            this.tuple = tuple;
        }
    }
}

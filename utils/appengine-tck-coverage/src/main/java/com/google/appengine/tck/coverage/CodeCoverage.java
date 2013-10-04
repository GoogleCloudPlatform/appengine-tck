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

import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author Stuart Douglas
 */
@SuppressWarnings("unchecked")
public class CodeCoverage {
    private static final Logger log = Logger.getLogger(CodeCoverage.class.getName());

    private final Map<String, List<Tuple>> descriptors = new TreeMap<String, List<Tuple>>();
    private final Map<String, List<String>> supers = new HashMap<String, List<String>>();
    private final Map<String, Map<Tuple, Set<CodeLine>>> report = new TreeMap<String, Map<Tuple, Set<CodeLine>>>();

    private static ClassLoader toClassLoader(File classesToScan) throws MalformedURLException {
        return new URLClassLoader(new URL[]{classesToScan.toURI().toURL()}, CodeCoverage.class.getClassLoader());
    }

    public static void report(String module, ClassLoader classLoader, File baseDir, File classesToScan, MethodExclusion exclusion, String... interfaces) throws Exception {
        if (interfaces == null || interfaces.length == 0) {
            log.warning("No interfaces defined!");
            return;
        }

        if (classLoader == null) {
            classLoader = toClassLoader(classesToScan);
        }

        CodeCoverage cc = new CodeCoverage(classLoader, exclusion, interfaces);
        cc.scan(classesToScan, "");
        cc.print(
            SoutPrinter.INSTANCE,
            new HtmlPrinter(baseDir, new File(classesToScan, "../../index.html"), module),
            new CsvPrinter(new File(classesToScan, "../../coverage-results.csv"))
        );
    }

    private CodeCoverage(ClassLoader classLoader, MethodExclusion exclusion, String... interfaces) throws Exception {
        for (String iface : interfaces) {
            Map<Tuple, Set<CodeLine>> map = new TreeMap<Tuple, Set<CodeLine>>();
            report.put(iface, map);

            List<Tuple> mds = new ArrayList<Tuple>();
            descriptors.put(iface, mds);

            InputStream is = classLoader.getResourceAsStream(iface.replace(".", "/") + ".class");
            ClassFile clazz = getClassFile(iface, is);

            fillSupers(clazz);

            List<MethodInfo> methods = clazz.getMethods();
            for (MethodInfo m : methods) {
                if (exclusion.exclude(clazz, m) == false) {
                    String descriptor = m.getDescriptor();
                    Tuple tuple = new Tuple(m.getName(), descriptor);
                    map.put(tuple, new TreeSet<CodeLine>());
                    mds.add(tuple);
                }
            }
        }
    }

    protected void fillSupers(ClassFile clazz) {
        String name = clazz.getName();
        List<String> list = supers.get(name);
        if (list == null) {
            list = new ArrayList<String>();
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
                checkClass(cf);
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

    protected void checkClass(ClassFile file) throws Exception {
        Map<Integer, Triple> calls = new HashMap<Integer, Triple>();

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

        if (calls.isEmpty())
            return;

        List<MethodInfo> methods = file.getMethods();
        for (MethodInfo m : methods) {
            try {
                // ignore abstract methods
                if (m.getCodeAttribute() == null) {
                    continue;
                }
                CodeIterator it = m.getCodeAttribute().iterator();
                while (it.hasNext()) {
                    // loop through the bytecode
                    int index = it.next();
                    int op = it.byteAt(index);
                    // if the bytecode is a method invocation
                    if (op == CodeIterator.INVOKEVIRTUAL || op == CodeIterator.INVOKESTATIC || op == CodeIterator.INVOKEINTERFACE || op == CodeIterator.INVOKESPECIAL) {
                        int val = it.s16bitAt(index + 1);
                        Triple triple = calls.get(val);
                        if (triple != null) {
                            Map<Tuple, Set<CodeLine>> map = report.get(triple.className);
                            Set<CodeLine> set = map.get(triple.tuple);
                            CodeLine cl = new CodeLine(file.getName(), m.getName(), m.getLineNumber(index));
                            set.add(cl);
                        }
                    }
                }
                m.getCodeAttribute().computeMaxStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

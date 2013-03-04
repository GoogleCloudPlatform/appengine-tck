package com.google.appengine.tck.coverage;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import javassist.ClassPool;
import javassist.LoaderClassPath;
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

    private final ClassPool pool;
    private final Map<String, List<Tuple>> descriptors = new TreeMap<String, List<Tuple>>();
    private final Map<String, Map<Tuple, Set<String>>> report = new TreeMap<String, Map<Tuple, Set<String>>>();

    private static ClassLoader toClassLoader(File classesToScan) throws MalformedURLException {
        return new URLClassLoader(new URL[]{classesToScan.toURI().toURL()}, CodeCoverage.class.getClassLoader());
    }

    public static void report(ClassLoader classLoader, File classesToScan, String... interfaces) throws Exception {
        if (interfaces == null || interfaces.length == 0) {
            log.warning("No interfaces defined!");
            return;
        }

        if (classLoader == null) {
            classLoader = toClassLoader(classesToScan);
        }

        CodeCoverage cc = new CodeCoverage(classLoader, interfaces);
        cc.scan(classesToScan, "");
        cc.print();
    }

    private CodeCoverage(ClassLoader classLoader, String... interfaces) throws Exception {
        pool = new ClassPool();
        pool.appendClassPath(new LoaderClassPath(classLoader));

        for (String iface : interfaces) {
            Class<?> clazz = classLoader.loadClass(iface);
            Method[] methods = clazz.getMethods();

            Map<Tuple, Set<String>> map = new TreeMap<Tuple, Set<String>>();
            report.put(iface, map);

            List<Tuple> mds = new ArrayList<Tuple>();
            descriptors.put(iface, mds);

            for (Method m : methods) {
                String descriptor = DescriptorUtils.getDescriptor(m);
                Tuple tuple = new Tuple(m.getName(), descriptor);
                map.put(tuple, new TreeSet<String>());
                mds.add(tuple);
            }

        }
    }

    protected void scan(File current, String fqn) throws Exception {
        if (current.isFile()) {
            if (fqn.endsWith(".class")) {
                String classname = fqn.substring(0, fqn.length() - ".class".length());
                ClassFile cf = pool.get(classname).getClassFile();
                checkClass(cf);
            }
        } else {
            File[] files = current.listFiles();
            for (File file : files) {
                scan(file, fqn.length() > 0 ? fqn + "." + file.getName() : file.getName());
            }
        }
    }

    protected void checkClass(ClassFile file) throws Exception {
        Map<Integer, Triple> calls = new HashMap<Integer, Triple>();

        ConstPool pool = file.getConstPool();
        for (int i = 1; i < pool.getSize(); ++i) {
            // we have a method call
            if (pool.getTag(i) == ConstPool.CONST_InterfaceMethodref) {
                String className = pool.getInterfaceMethodrefClassName(i);
                List<Tuple> mds = descriptors.get(className);
                if (mds != null) {
                    String methodName = pool.getInterfaceMethodrefName(i);
                    String methodDesc = pool.getInterfaceMethodrefType(i);
                    for (Tuple tuple : mds) {
                        if (tuple.methodName.equals(methodName) && tuple.methodDesc.equals(methodDesc)) {
                            calls.put(i, new Triple(className, tuple));
                        }
                    }
                }
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
                            Map<Tuple, Set<String>> map = report.get(triple.className);
                            Set<String> set = map.get(triple.tuple);
                            set.add(file.getName() + " @ " + m + " # " + m.getLineNumber(index));
                        }
                    }
                }
                m.getCodeAttribute().computeMaxStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void print() {
        StringBuilder builder = new StringBuilder("\n");
        for (String iface : report.keySet()) {
            builder.append("Interface: ").append(iface).append("\n");
            Map<Tuple, Set<String>> map = report.get(iface);
            for (Map.Entry<Tuple, Set<String>> entry : map.entrySet()) {
                builder.append("\t").append(entry.getKey()).append("\n");
                Set<String> value = entry.getValue();
                if (value.isEmpty()) {
                    builder.append("\t\t").append("MISSING -- TODO?").append("\n");
                } else {
                    for (String info : value) {
                        builder.append("\t\t").append(info).append("\n");
                    }
                }
            }
        }
        System.out.println(builder);
    }

    private static class Tuple implements Comparable<Tuple> {
        String methodName;
        String methodDesc;

        private Tuple(String name, String methodDesc) {
            this.methodName = name;
            this.methodDesc = methodDesc;
        }

        public int compareTo(Tuple o) {
            return toString().compareTo(o.toString());
        }

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tuple tuple = (Tuple) o;

            if (methodDesc.equals(tuple.methodDesc) == false) return false;
            if (methodName.equals(tuple.methodName) == false) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = methodName.hashCode();
            result = 31 * result + methodDesc.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return methodName + "@" + methodDesc;
        }
    }

    private static class Triple {
        private String className;
        private Tuple tuple;

        private Triple(String className, Tuple tuple) {
            this.className = className;
            this.tuple = tuple;
        }
    }
}

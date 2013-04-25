package com.google.appengine.tck.coverage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
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

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author Stuart Douglas
 */
@SuppressWarnings("unchecked")
public class CodeCoverage {
    private static final Logger log = Logger.getLogger(CodeCoverage.class.getName());

    private final Map<String, List<Tuple>> descriptors = new TreeMap<String, List<Tuple>>();
    private final Map<String, Map<Tuple, Set<CodeLine>>> report = new TreeMap<String, Map<Tuple, Set<CodeLine>>>();

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
        cc.print(
                SoutPrinter.INSTANCE,
                new HtmlPrinter(new File(classesToScan, "../../index.html")),
                new CsvPrinter(new File(classesToScan, "../../coverage-results.csv"))
        );
    }

    private CodeCoverage(ClassLoader classLoader, String... interfaces) throws Exception {
        for (String iface : interfaces) {
            Map<Tuple, Set<CodeLine>> map = new TreeMap<Tuple, Set<CodeLine>>();
            report.put(iface, map);

            List<Tuple> mds = new ArrayList<Tuple>();
            descriptors.put(iface, mds);

            InputStream is = classLoader.getResourceAsStream(iface.replace(".", "/") + ".class");
            ClassFile clazz = getClassFile(iface, is);
            List<MethodInfo> methods = clazz.getMethods();

            for (MethodInfo m : methods) {
                if (includeMethod(m)) {
                    String descriptor = m.getDescriptor();
                    Tuple tuple = new Tuple(m.getName(), descriptor);
                    map.put(tuple, new TreeSet<CodeLine>());
                    mds.add(tuple);
                }
            }
        }
    }

    protected boolean includeMethod(MethodInfo mi) {
        if (Modifier.isPublic(mi.getAccessFlags()) == false) {
            return false;
        }

        AnnotationsAttribute aa = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
        if (aa != null) {
            Annotation annotation = aa.getAnnotation(Deprecated.class.getName());
            if (annotation != null) {
                return false;
            }
        }

        // TODO -- explicit excludes

        return true;
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
                List<Tuple> mds = descriptors.get(className);
                if (mds != null) {
                    String methodName = ref.getName(pool, i);
                    String methodDesc = ref.getDesc(pool, i);
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

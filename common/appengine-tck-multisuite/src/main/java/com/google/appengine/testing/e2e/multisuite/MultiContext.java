package com.google.appengine.testing.e2e.multisuite;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;

import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MultiContext {
    private final WebArchive war;
    private final File root;
    private final ClassLoader classLoader;

    public MultiContext(WebArchive war, File root, ClassLoader classLoader) {
        this.war = war;
        this.root = root;
        this.classLoader = classLoader;
    }

    public static Object newInstance(ClassLoader cl, String className) throws Exception {
        return loadClass(cl, className).newInstance();
    }

    public static Object newInstance(ClassLoader cl, String className, Object[] args, Class[] types) throws Exception {
        Class<?> clazz = loadClass(cl, className);
        Constructor<?> ctor = clazz.getConstructor(types);
        return ctor.newInstance(args);
    }

    public static Class<?> loadClass(ClassLoader cl, String className) throws Exception {
        return cl.loadClass(className);
    }

    public Class<?> loadClass(String className) throws Exception {
        return loadClass(classLoader, className);
    }

    public URL getResource(String resource) {
        return classLoader.getResource(resource);
    }

    public InputStream getResourceAsStream(String resource) {
        return classLoader.getResourceAsStream(resource);
    }

    public String toFQN(File classFile) {
        if (classFile.getName().endsWith(".class") == false) {
            throw new IllegalArgumentException("File is not Java class: " + classFile);
        }
        String relativePath = getRelativePath(classFile);
        relativePath = relativePath.replace("/", ".");
        return relativePath.substring(0, relativePath.length() - ".class".length());
    }

    public Class<?> toClass(File classFile) throws Exception {
        return loadClass(toFQN(classFile));
    }

    public void addClass(String className) throws Exception {
        addClass(loadClass(className));
    }

    public void addClass(Class<?> current) {
        if (current == null || current == Object.class)
            return;

        war.addClass(current);

        for (Class<?> iface : current.getInterfaces()) {
            addClass(iface);
        }

        addClass(current.getSuperclass());
    }

    public String getRelativePath(File current) {
        String path = "";
        while (current.equals(root) == false) {
            path = "/" + current.getName() + path;
            current = current.getParentFile();
        }
        return path.length() > 0 ? path.substring(1) : path;
    }

    public WebArchive getWar() {
        return war;
    }

    public File getRoot() {
        return root;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}

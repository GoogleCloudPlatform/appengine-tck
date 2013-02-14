package com.google.appengine.testing.e2e.multisuite.scan;

import java.io.File;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import com.google.appengine.testing.e2e.multisuite.MultiContext;
import com.google.appengine.testing.e2e.multisuite.MultiProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ScanMultiProvider implements MultiProvider {
    private final Pattern classPattern;

    public ScanMultiProvider() {
        this(".+Test\\.class");
    }

    public ScanMultiProvider(String classRegexp) {
        this.classPattern = Pattern.compile(classRegexp);
    }

    public void provide(MultiContext context) throws Exception {
        scan(context, context.getRoot());
    }

    protected void scan(MultiContext context, File current) throws Exception {
        if (current.isFile()) {
            String name = current.getName();
            if (classPattern.matcher(name).matches()) {
                Class<?> clazz = context.toClass(current);
                context.addClass(clazz);
                WebArchive war = readWebArchive(clazz, clazz);
                merge(context, war);
            }
        } else {
            for (File file : current.listFiles()) {
                scan(context, file);
            }
        }
    }

    protected void merge(MultiContext context, WebArchive war) {
        WebArchive uber = context.getWar();
        uber.merge(war);
    }

    protected WebArchive readWebArchive(Class<?> clazz, Class<?> current) throws Exception {
        if (current == null || current == Object.class) {
            throw new IllegalArgumentException("No @Deployment on test class: " + clazz.getName());
        }

        Method[] methods = current.getDeclaredMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(Deployment.class)) {
                m.setAccessible(true); // in case of non-public
                return (WebArchive) m.invoke(null);
            }
        }

        return readWebArchive(clazz, current.getSuperclass());
    }
}

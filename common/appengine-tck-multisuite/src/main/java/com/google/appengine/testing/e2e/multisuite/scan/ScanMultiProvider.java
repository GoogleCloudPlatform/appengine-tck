package com.google.appengine.testing.e2e.multisuite.scan;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.appengine.tck.category.IgnoreMultisuite;
import com.google.appengine.testing.e2e.multisuite.MultiContext;
import com.google.appengine.testing.e2e.multisuite.MultiProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.experimental.categories.Category;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ScanMultiProvider implements MultiProvider {
    private static final Logger log = Logger.getLogger(ScanMultiProvider.class.getName());

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
                if (isIgnore(clazz) == false) {
                    log.info("Adding test class: " + clazz.getName());
                    context.addClass(clazz);
                    WebArchive war = readWebArchive(clazz, clazz);
                    merge(context, war);
                } else {
                    log.info("Ignoring test class: " + clazz.getName());
                }
            }
        } else {
            for (File file : current.listFiles()) {
                scan(context, file);
            }
        }
    }

    protected boolean isIgnore(Class<?> clazz) {
        Category cat = clazz.getAnnotation(Category.class);
        if (cat != null) {
            Set<Class<?>> classes = new HashSet<Class<?>>(Arrays.asList(cat.value()));
            return classes.contains(IgnoreMultisuite.class);
        }
        return false;
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

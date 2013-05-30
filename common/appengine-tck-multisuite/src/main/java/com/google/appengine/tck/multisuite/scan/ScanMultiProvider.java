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

package com.google.appengine.tck.multisuite.scan;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.appengine.tck.category.IgnoreMultisuite;
import com.google.appengine.tck.event.TestLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycles;
import com.google.appengine.tck.multisuite.MultiContext;
import com.google.appengine.tck.multisuite.MultiProvider;
import com.google.appengine.tck.scan.Context;
import com.google.appengine.tck.scan.ScanStrategy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.experimental.categories.Category;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ScanMultiProvider implements MultiProvider, ScanStrategy {
    private static final Logger log = Logger.getLogger(ScanMultiProvider.class.getName());

    private static final Properties DEFAULTS;

    static {
        DEFAULTS = new Properties();
        DEFAULTS.put("pattern", ".+Test\\.class");
    }

    private final Pattern classPattern;
    private final ScanStrategy strategy;
    private final String filterClass;

    public ScanMultiProvider() throws Exception {
        this(null);
    }

    public ScanMultiProvider(Properties overrides) throws Exception {
        Properties properties = new Properties(DEFAULTS);
        if (overrides != null) {
            properties.putAll(overrides);
        }

        String regexp = properties.getProperty("pattern");
        classPattern = Pattern.compile(regexp);

        String strategyClass = properties.getProperty("strategy");
        if (strategyClass != null) {
            strategy = (ScanStrategy) MultiContext.newInstance(ScanStrategy.class.getClassLoader(), strategyClass);
            log.info("New scan strategy: " + strategy);
        } else {
            strategy = this;
        }

        filterClass = properties.getProperty("filter");
    }

    @SuppressWarnings("unchecked")
    protected Filter<ArchivePath> createFilter(WebArchive uber, WebArchive archive) throws Exception {
        if (filterClass != null) {
            return (Filter<ArchivePath>) MultiContext.newInstance(ScanStrategy.class.getClassLoader(), filterClass, new Object[]{uber, archive}, new Class[]{WebArchive.class, WebArchive.class});
        } else {
            return new WarningFilter(uber, archive);
        }
    }

    public boolean doMerge(Context context, Class<?> clazz) {
        return true;
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

                    if (strategy.doMerge(context, clazz)) {
                        TestLifecycleEvent event = TestLifecycles.createMergeLifecycleEvent(clazz, context.getWar());

                        TestLifecycles.before(event);
                        try {
                            WebArchive war = readWebArchive(clazz);
                            merge(context, war);
                        } finally {
                            TestLifecycles.after(event);
                        }
                    }
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

    protected void merge(MultiContext context, WebArchive war) throws Exception {
        WebArchive uber = context.getWar();
        Filter<ArchivePath> filter = createFilter(uber, war);
        uber.merge(war, filter);
    }

    protected WebArchive readWebArchive(Class<?> clazz) throws Exception {
        return readWebArchive(clazz, clazz);
    }

    private WebArchive readWebArchive(Class<?> clazz, Class<?> current) throws Exception {
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

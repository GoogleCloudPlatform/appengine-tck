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

package com.google.appengine.tck.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.tck.category.IgnoreMultisuite;
import com.google.appengine.tck.event.ExecutionLifecycleEvent;
import com.google.appengine.tck.event.Property;
import com.google.appengine.tck.event.PropertyLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycles;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;

/**
 * Base test class for all GAE TCK tests.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestBase {
    protected static final long DEFAULT_SLEEP = 3000L;
    protected static final String TCK_PROPERTIES = "tck.properties";

    protected final Logger log = Logger.getLogger(getClass().getName());

    protected static void enhanceTestContext(TestContext context) {
        TestLifecycleEvent event = TestLifecycles.createTestContextLifecycleEvent(null, context);
        TestLifecycles.before(event);
    }

    protected static WebArchive getTckDeployment() {
        return getTckDeployment(new TestContext());
    }

    protected static WebArchive getTckDeployment(TestContext context) {
        enhanceTestContext(context);

        final WebArchive war;

        String archiveName = context.getArchiveName();
        if (archiveName != null) {
            if (archiveName.endsWith(".war") == false) archiveName += ".war";
            war = ShrinkWrap.create(WebArchive.class, archiveName);
        } else {
            war = ShrinkWrap.create(WebArchive.class);
        }

        // this package
        war.addPackage(TestBase.class.getPackage());
        // categories
        war.addPackage(IgnoreMultisuite.class.getPackage());
        // events
        war.addPackage(TestLifecycles.class.getPackage());

        // web.xml
        if (context.getWebXmlFile() != null) {
            war.setWebXML(context.getWebXmlFile());
        } else {
            war.setWebXML(new StringAsset(context.getWebXmlContent()));
        }

        // context-root
        if (context.getContextRoot() != null) {
            war.addAsWebInfResource(context.getContextRoot().getDescriptor());
        }

        // appengine-web.xml
        if (context.getAppEngineWebXmlFile() != null) {
            war.addAsWebInfResource(context.getAppEngineWebXmlFile(), "appengine-web.xml");
        } else {
            war.addAsWebInfResource("appengine-web.xml");
        }

        if (context.hasCallbacks()) {
            war.addAsWebInfResource("META-INF/datastorecallbacks.xml", "classes/META-INF/datastorecallbacks.xml");
        }

        if (context.getCompatibilityProperties() != null && (context.getProperties().isEmpty() == false || context.isUseSystemProperties())) {
            Properties properties = new Properties();

            if (context.isUseSystemProperties()) {
                properties.putAll(System.getProperties());
            }
            properties.putAll(context.getProperties());

            final StringWriter writer = new StringWriter();
            try {
                properties.store(writer, "GAE TCK testing!");
            } catch (IOException e) {
                throw new RuntimeException("Cannot write compatibility properties.", e);
            }

            final StringAsset asset = new StringAsset(writer.toString());
            war.addAsWebInfResource(asset, "classes/" + context.getCompatibilityProperties());
        }

        return war;
    }

    /**
     * Should work in all envs?
     * A bit complex / overkill ...
     *
     * @return true if in-container, false otherewise
     */
    protected static boolean isInContainer() {
        try {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            Transaction tx = ds.beginTransaction();
            try {
                return (ds.getCurrentTransaction() != null);
            } finally {
                tx.rollback();
            }
        } catch (Throwable ignored) {
            return false;
        }
    }

    protected static void assertRegexpMatches(String regexp, String str) {
        Assert.assertTrue("Expected to match regexp " + regexp + " but was: " + str, str != null && str.matches(regexp));
    }

    protected boolean execute(String context) {
        Boolean result = executeRaw(context);
        return (result != null && result);
    }

    protected Boolean executeRaw(String context) {
        ExecutionLifecycleEvent event = TestLifecycles.createExecutionLifecycleEvent(getClass(), context);
        TestLifecycles.before(event);
        return event.execute();
    }

    protected boolean required(String propertyName) {
        Property result = property(propertyName);
        Boolean required = result.required();
        return (required == null || required); // by default null means it's required
    }

    protected boolean doIgnore(String context) {
        return execute(context) == false;
    }

    protected Property property(String propertyName) {
        PropertyLifecycleEvent event = TestLifecycles.createPropertyLifecycleEvent(getClass(), propertyName);
        TestLifecycles.before(event);
        return event;
    }

    protected static void sync() {
        sync(DEFAULT_SLEEP);
    }

    protected static void sync(final long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    protected <T> T waitOnFuture(Future<T> f) {
        try {
            return f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof RuntimeException) {
                throw RuntimeException.class.cast(cause);
            } else {
                cause = e;
            }
            throw new IllegalStateException(cause);
        }
    }

    protected Properties readProperties(String name) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(name);

        if (is == null) {
            throw new IllegalArgumentException("No such resource: " + name);
        }

        try {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
    }
}

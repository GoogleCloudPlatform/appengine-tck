package com.google.appengine.testing.e2e.common;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import com.google.appengine.api.utils.SystemProperty;
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

    protected final Logger log = Logger.getLogger(getClass().getName());

    protected static void enhanceTestContext(TestContext context) {
        for (TestContextEnhancer enhancer : ServiceLoader.load(TestContextEnhancer.class, TestBase.class.getClassLoader())) {
            enhancer.enhance(context);
        }
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
        // categories -- TODO

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

        if (context.getCompatibilityProperties() != null && context.getProperties().isEmpty() == false) {
            final StringWriter writer = new StringWriter();
            try {
                context.getProperties().store(writer, "GAE TCK testing!");
            } catch (IOException e) {
                throw new RuntimeException("Cannot write compatibility properties.", e);
            }
            final StringAsset asset = new StringAsset(writer.toString());
            war.addAsResource(asset, context.getCompatibilityProperties());
        }

        return war;
    }

    protected static void assertRegexpMatches(String regexp, String str) {
        Assert.assertTrue("Expected to match regexp " + regexp + " but was: " + str, str != null && str.matches(regexp));
    }

    protected boolean isRunningInsideDevServer() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
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

}

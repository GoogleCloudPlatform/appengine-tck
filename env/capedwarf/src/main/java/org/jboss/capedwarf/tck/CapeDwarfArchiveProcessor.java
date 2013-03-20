package org.jboss.capedwarf.tck;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import com.google.appengine.tck.event.TestLifecycle;
import com.google.appengine.tck.util.Utils;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapeDwarfArchiveProcessor implements ApplicationArchiveProcessor {
    private static final String COMPATIBILITY_PROPERTIES = "capedwarf-compatibility.properties";
    private static final Properties COMPATIBILITY;

    static {
        COMPATIBILITY = new Properties();
    }

    @SuppressWarnings("unchecked")
    public void process(Archive<?> archive, TestClass testClass) {
        if (archive instanceof WebArchive) {
            WebArchive war = (WebArchive) archive;
            addService(war, TestLifecycle.class,
                    CapeDwarfTestContextEnhancer.class,
                    CapeDwarfServicesLifecycle.class,
                    CapeDwarfMergeLifecycle.class
            );

            addCompatibility(war, COMPATIBILITY);
        }
    }

    protected <T> void addService(WebArchive archive, Class<T> serviceClass, Class<? extends T>... serviceImpls) {
        archive.addClasses(serviceImpls);
        StringBuilder builder = new StringBuilder();
        for (Class<? extends T> serviceImpl : serviceImpls) {
            builder.append(serviceImpl.getName()).append("\n");
        }
        archive.addAsWebInfResource(new StringAsset(builder.toString()), "classes/META-INF/services/" + serviceClass.getName());
    }

    static void addCompatibility(WebArchive war, Properties extra) {
        final Properties properties = new Properties();

        final Node node = war.get("WEB-INF/classes/" + COMPATIBILITY_PROPERTIES);
        if (node != null) {
            InputStream is = node.getAsset().openStream();
            try {
                properties.load(is);
            } catch (IOException e) {
                throw new RuntimeException("Cannot read compatibility properties.", e);
            } finally {
                Utils.safeClose(is);
            }
            war.delete(node.getPath());
        }

        for (String key : extra.stringPropertyNames()) {
            if (properties.containsKey(key) == false) {
                properties.setProperty(key, extra.getProperty(key));
            }
        }

        final StringWriter writer = new StringWriter();
        try {
            properties.store(writer, "CapeDwarf testing!");
        } catch (IOException e) {
            throw new RuntimeException("Cannot write compatibility properties.", e);
        } finally {
            Utils.safeClose(writer);
        }
        final StringAsset asset = new StringAsset(writer.toString());
        war.addAsResource(asset, COMPATIBILITY_PROPERTIES);
    }
}

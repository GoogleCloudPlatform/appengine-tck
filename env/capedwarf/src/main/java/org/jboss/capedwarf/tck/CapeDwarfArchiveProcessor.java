package org.jboss.capedwarf.tck;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import com.google.appengine.tck.event.TestLifecycle;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapeDwarfArchiveProcessor implements ApplicationArchiveProcessor {
    @SuppressWarnings("unchecked")
    public void process(Archive<?> archive, TestClass testClass) {
        if (archive instanceof WebArchive) {
            WebArchive war = (WebArchive) archive;
            addService(war, TestLifecycle.class,
                    CapeDwarfTestContextEnhancer.class,
                    CapeDwarfServicesLifecycle.class,
                    CapeDwarfMergeLifecycle.class
            );
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

    static void addCompatibility(WebArchive war, Properties properties) {
        final StringWriter writer = new StringWriter();
        try {
            properties.store(writer, "CapeDwarf testing!");
        } catch (IOException e) {
            throw new RuntimeException("Cannot write compatibility properties.", e);
        }
        final StringAsset asset = new StringAsset(writer.toString());
        war.addAsResource(asset, "capedwarf-compatibility.properties");
    }
}

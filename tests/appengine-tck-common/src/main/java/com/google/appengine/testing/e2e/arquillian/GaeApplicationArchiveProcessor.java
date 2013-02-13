package com.google.appengine.testing.e2e.arquillian;

import java.io.File;
import java.util.Set;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GaeApplicationArchiveProcessor implements ApplicationArchiveProcessor {
    public void process(Archive<?> archive, TestClass testClass) {
        final Node lib = archive.get("WEB-INF/lib");
        if (lib != null) {
            final Set<Node> libs = lib.getChildren();
            for (Node jar : libs) {
                if (jar.getPath().get().contains("appengine-api"))
                    return;
            }
        }

        if (archive instanceof WebArchive) {
            WebArchive war = (WebArchive) archive;
            war.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").
                    resolve("com.google.appengine:appengine-api-1.0-sdk").withTransitivity().as(File.class));
        }
    }
}

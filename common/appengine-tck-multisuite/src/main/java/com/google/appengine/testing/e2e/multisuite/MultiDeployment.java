package com.google.appengine.testing.e2e.multisuite;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.google.appengine.testing.e2e.multisuite.scan.ScanMultiProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MultiDeployment {
    @Deployment
    public static WebArchive getDeployment() throws Exception {
        final ClassLoader cl = MultiDeployment.class.getClassLoader();
        final URL arqXml = cl.getResource("multisuite.marker");
        if (arqXml == null) {
            throw new IllegalArgumentException("Missing multisuite.marker?!");
        }

        Properties overrides = new Properties();
        InputStream is = arqXml.openStream();
        try {
            overrides.load(is);
        } finally {
            is.close();
        }

        String name = overrides.getProperty("deployment.name", "gae-multisuite-tck.war");
        WebArchive war = ShrinkWrap.create(WebArchive.class, name);
        File root = new File(arqXml.toURI()).getParentFile();

        MultiContext mc = new MultiContext(war, root, cl);

        MultiProvider provider = new ScanMultiProvider(overrides);
        provider.provide(mc);

        return war;
    }
}

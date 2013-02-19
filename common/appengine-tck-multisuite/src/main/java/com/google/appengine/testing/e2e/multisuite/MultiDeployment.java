package com.google.appengine.testing.e2e.multisuite;

import java.io.File;
import java.net.URL;

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
        WebArchive war = ShrinkWrap.create(WebArchive.class, "gae-multisuite-tck.war");
        ClassLoader cl = MultiDeployment.class.getClassLoader();
        URL arqXml = cl.getResource("multisuite.marker");
        if (arqXml == null) {
            throw new IllegalArgumentException("Missing multisuite.marker?!");
        }
        File root = new File(arqXml.toURI()).getParentFile();

        MultiContext mc = new MultiContext(war, root, cl);

        MultiProvider provider = new ScanMultiProvider();
        provider.provide(mc);

        return war;
    }
}

package com.google.appengine.tck.arquillian;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(LoadableExtension.class)
public class GaeLoadableExtension implements LoadableExtension {
    public void register(ExtensionBuilder builder) {
        builder.service(ApplicationArchiveProcessor.class, GaeApplicationArchiveProcessor.class);
    }
}

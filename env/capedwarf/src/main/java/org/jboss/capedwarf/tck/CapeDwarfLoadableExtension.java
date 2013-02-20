package org.jboss.capedwarf.tck;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class CapeDwarfLoadableExtension implements LoadableExtension {
    public void register(ExtensionBuilder builder) {
        builder.service(ApplicationArchiveProcessor.class, CapeDwarfArchiveProcessor.class);
    }
}

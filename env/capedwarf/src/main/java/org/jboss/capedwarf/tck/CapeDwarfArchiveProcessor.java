package org.jboss.capedwarf.tck;

import com.google.appengine.tck.base.ServicesLifecycle;
import com.google.appengine.tck.base.TestContextEnhancer;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapeDwarfArchiveProcessor implements ApplicationArchiveProcessor {
    public void process(Archive<?> archive, TestClass testClass) {
        if (archive instanceof WebArchive) {
            WebArchive war = (WebArchive) archive;
            addService(war, TestContextEnhancer.class, CapeDwarfTestContextEnhancer.class);
            addService(war, ServicesLifecycle.class, CapeDwarfServicesLifecycle.class);
        }
    }

    protected <T> void addService(WebArchive archive, Class<T> serviceClass, Class<? extends T> serviceImpl) {
        archive.addClass(serviceImpl);
        archive.addAsWebInfResource(new StringAsset(serviceImpl.getName()), "classes/META-INF/services/" + serviceClass.getName());
    }
}

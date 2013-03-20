package org.jboss.capedwarf.tck;

import java.util.Properties;

import com.google.appengine.tck.event.MergeLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycle;
import com.google.appengine.tck.event.TestLifecycleEvent;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class CapeDwarfMergeLifecycle implements TestLifecycle {
    public void before(TestLifecycleEvent event) {
        if (event instanceof MergeLifecycleEvent) {
            MergeLifecycleEvent mcl = (MergeLifecycleEvent) event;
            Class<?> clazz = mcl.getCallerClass();
            if (clazz != null && clazz.getName().contains("tck.datastore") == false) {
                WebArchive war = mcl.getDeployment();
                if (war.get("WEB-INF/classes/capedwarf-compatibility.properties") == null) {
                    Properties properties = new Properties();
                    properties.put("disable.metadata", Boolean.TRUE.toString());
                    CapeDwarfArchiveProcessor.addCompatibility(war, properties);
                }
            }
        }
    }

    public void after(TestLifecycleEvent event) {
    }
}

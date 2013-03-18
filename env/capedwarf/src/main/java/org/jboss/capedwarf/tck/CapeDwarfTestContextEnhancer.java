package org.jboss.capedwarf.tck;

import com.google.appengine.tck.base.ContextRoot;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.event.TestContextLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycle;
import com.google.appengine.tck.event.TestLifecycleEvent;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class CapeDwarfTestContextEnhancer implements TestLifecycle {
    public void before(TestLifecycleEvent event) {
        if (event instanceof TestContextLifecycleEvent) {
            TestContextLifecycleEvent tcle = (TestContextLifecycleEvent) event;
            enhance(tcle.getTestContext());
        }
    }

    public void after(TestLifecycleEvent event) {
    }

    protected void enhance(TestContext context) {
        context.setArchiveName("ROOT.war");
        context.setContextRoot(CapeDwarfContextRoot.INSTANCE);
    }

    private static class CapeDwarfContextRoot implements ContextRoot {
        static ContextRoot INSTANCE = new CapeDwarfContextRoot();

        public String getDeploymentName() {
            return "ROOT.war";
        }

        public String getDescriptor() {
            return "jboss-web.xml";
        }
    }
}

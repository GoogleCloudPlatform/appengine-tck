/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.capedwarf.tck;

import com.google.appengine.tck.base.ContextRoot;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.event.AbstractTestContextLifecycle;
import com.google.appengine.tck.event.TestContextLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycle;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(TestLifecycle.class)
public class CapeDwarfTestContextEnhancer extends AbstractTestContextLifecycle {
    protected void doBefore(TestContextLifecycleEvent event) {
        enhance(event.getTestContext());
    }

    protected void doAfter(TestContextLifecycleEvent event) {
    }

    protected void enhance(TestContext context) {
        if (context.isSubdeployment() == false) {
            context.setArchiveName("ROOT.war");
            context.setContextRoot(CapeDwarfContextRoot.INSTANCE);
        }
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

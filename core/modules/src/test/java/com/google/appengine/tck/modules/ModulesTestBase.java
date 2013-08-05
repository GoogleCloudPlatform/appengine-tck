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

package com.google.appengine.tck.modules;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ModulesTestBase extends TestBase {
    protected static TestContext toSubdeployment(int module) {
        return new TestContext("module" + module).setSubdeployment(true);
    }

    protected static WebArchive getTckSubDeployment(int module) {
        return getTckDeployment(toSubdeployment(module));
    }

    protected static EnterpriseArchive toEarDeployment(WebArchive... wars) {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "gae-tck.ear");
        for (WebArchive war : wars) {
            war.addClass(ModulesTestBase.class);
            ear.addAsModule(war);
        }
        return ear;
    }

    protected static EnterpriseArchive getEarDeployment(WebArchive... wars) {
        EnterpriseArchive ear = toEarDeployment(wars);
        ear.addAsManifestResource("application.xml");
        ear.addAsResource("appengine-application.xml");
        return ear;
    }
}

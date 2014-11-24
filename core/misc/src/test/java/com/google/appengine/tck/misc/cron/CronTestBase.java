/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.appengine.tck.misc.cron;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.env.Environment;
import com.google.appengine.tck.misc.cron.support.ActionData;
import com.google.appengine.tck.misc.cron.support.PingServlet;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CronTestBase extends TestBase {
    protected static WebArchive getBaseDeployment(String cronXml) {
        WebArchive war = getTckDeployment(new TestContext().setWebXmlFile("web-ping-servlet.xml"));
        war.addAsWebInfResource(cronXml + ".xml", "cron.xml");
        war.addClasses(CronTestBase.class, PingServlet.class, ActionData.class);
        return war;
    }

    @AfterClass
    public static void afterClass() throws Exception {
        deleteTempData(ActionData.class);
    }

    protected void doTestAction(String action) {
        assumeEnvironment(Environment.APPSPOT, Environment.CAPEDWARF);

        sync(70 * 1000L); // more then 1min

        ActionData ad = pollForTempData(ActionData.class, 10); // 10sec more
        Assert.assertNotNull(ad);
        Assert.assertEquals(action, ad.getAction());
    }
}

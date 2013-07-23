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

package com.google.appengine.tck.channel;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;


/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public abstract class ChannelTestBase extends TestBase {

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext();
        context.setAppEngineWebXmlFile("channel-appengine-web.xml");

        WebArchive war = getTckDeployment(context);
        war.addClass(TestBase.class);
        war.addClass(ChannelTestBase.class);
        war.addClasses(ChannelTest.class);
        war.addPackage("org.openqa.selenium");

        war.addAsWebInfResource("channelPage.jsp");
        war.addAsWebResource("channelPage.jsp", "channelPage.jsp");

        war.addAsWebInfResource("channelEcho.jsp");
        war.addAsWebResource("channelEcho.jsp", "channelEcho.jsp");

        return war;
    }
}

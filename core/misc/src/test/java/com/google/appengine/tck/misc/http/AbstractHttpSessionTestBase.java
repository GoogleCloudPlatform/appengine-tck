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

package com.google.appengine.tck.misc.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.misc.http.support.Client;
import com.google.appengine.tck.misc.http.support.RequestFilter;
import com.google.appengine.tck.misc.http.support.SessionServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class AbstractHttpSessionTestBase extends TestBase {

    private static Client client;

    protected static WebArchive getBaseDeployment(boolean enabled) {
        System.setProperty("appengine.sessions.enabled", Boolean.toString(enabled));
        try {
            TestContext context = new TestContext();
            context.setWebXmlFile("web-sessions.xml");
            context.setAppEngineWebXmlFile("appengine-web-sessions.xml");
            WebArchive war = getTckDeployment(context);
            war.addClass(AbstractHttpSessionTestBase.class);
            war.addClass(RequestFilter.class);
            war.addClass(SessionServlet.class);
            war.addClass(Client.class);
            return war;
        } finally {
            System.clearProperty("appengine.sessions.enabled");
        }
    }

    protected static synchronized Client getClient() {
        if (client == null) {
            client = new Client();
        }
        return client;
    }

    @AfterClass
    public static void shutdownClient() throws Exception {
        if (client != null) {
            client.close();
        }
    }

    protected static HttpSession getSession() {
        HttpServletRequest request = RequestFilter.getRequest();
        return request.getSession();
    }

    @Test
    public void testSessionCreate() throws Exception {
        HttpSession session = getSession();
        Assert.assertNotNull(session);
        System.out.println("session = " + session);
    }

}

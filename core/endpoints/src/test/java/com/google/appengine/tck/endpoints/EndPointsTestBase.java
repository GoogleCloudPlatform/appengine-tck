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

package com.google.appengine.tck.endpoints;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.endpoints.support.EndPointClient;
import com.google.appengine.tck.event.TestLifecycles;
import com.google.appengine.tck.event.UrlLifecycleEvent;
import com.google.appengine.tck.lib.LibUtils;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EndPointsTestBase extends TestBase {
    protected EndPointClient client;

    @Before
    public void setUp() {
        client = new EndPointClient();
    }

    @After
    public void tearDown() {
        if (client != null) {
            client.shutdown();
        }
    }

    protected static WebArchive getDefaultDeployment(TestContext context) {
        WebArchive war = getTckDeployment(context);
        war.addClass(EndPointsTestBase.class);
        new LibUtils().addLibrary(war, "com.google.appengine:appengine-endpoints");
        return war;
    }

    protected void assertResponse(String expected, String actual) {
        assertTrue("Response was: " + actual + ", where we expected: " + expected, actual.contains(expected));
    }

    protected String invokeEndpointWithGet(URL endPointUrl) throws Exception {
        return client.doGet(endPointUrl);
    }

    protected String invokeEndpointWithPost(URL endPointUrl) throws Exception {
        return client.doPost(endPointUrl);
    }

    protected String createPath(String name, String version, String methodPath) {
        return "/_ah/api/" + name + "/" + version + "/" + methodPath;
    }

    protected URL toHttps(URL url) throws MalformedURLException {
        UrlLifecycleEvent event = TestLifecycles.createUrlLifecycleEvent(getClass(), url);
        TestLifecycles.before(event);
        return event.getHttps();
    }

}

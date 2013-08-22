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

import java.net.URL;

import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.endpoints.support.TestEndPoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class EndPointsTest extends EndPointsTestBase {

    public static final String PARAM_VALUE = "hello";

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext().setWebXmlFile("endpoints-web.xml");
        WebArchive war = getDefaultDeployment(context);
        war.addPackage(TestEndPoint.class.getPackage());
        war.addAsWebInfResource("testEndPoint-v2.api");
        war.addAsWebInfResource("endPointWithoutVersion-v1.api");
        war.addAsWebInfResource("myapi-v2.api");
        return war;
    }

    @Test
    @RunAsClient
    public void testMethodWithoutParams(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("withoutParameters")));
        String response = invokeEndpointWithGet(endPointUrl);
        assertResponse("method withoutParameters was invoked", response);
    }

    @Test
    @RunAsClient
    public void testMethodWithParameterInQueryString(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("withParameterInQueryString") + "?param=" + PARAM_VALUE));
        String response = invokeEndpointWithGet(endPointUrl);
        assertResponse("The param was " + PARAM_VALUE, response);
    }

    @Test
    @RunAsClient
    public void testMethodWithParameterInPath(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("withParameterInPath") + "/" + PARAM_VALUE));
        String response = invokeEndpointWithGet(endPointUrl);
        assertResponse("The param was " + PARAM_VALUE, response);
    }

    @Test
    @RunAsClient
    public void testPost(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("post")));
        String response = client.doPost(endPointUrl);
        assertResponse("method post was invoked", response);
    }

    @Test
    @RunAsClient
    public void testPut(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("put")));
        String response = client.doPut(endPointUrl);
        assertResponse("method put was invoked", response);
    }

    @Test
    @RunAsClient
    public void testDelete(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("delete")));
        String response = client.doDelete(endPointUrl);
        assertResponse("method delete was invoked", response);
    }

    @Ignore("Not working on appspot")
    @Test
    @RunAsClient
    public void testEndPointWithoutName(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("myApi", "v2", "withoutParameters")));
        String response = invokeEndpointWithGet(endPointUrl);
        assertResponse("method withoutParameters was invoked", response);
    }

    @Test
    @RunAsClient
    public void testEndPointWithoutVersion(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("endPointWithoutVersion", "v1", "withoutParameters")));
        String response = invokeEndpointWithGet(endPointUrl);
        assertResponse("method withoutParameters was invoked", response);
    }

    protected String createPath(String methodPath) {
        return createPath(TestEndPoint.NAME, TestEndPoint.VERSION, methodPath);
    }
}

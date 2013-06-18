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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class EndPointsTest extends TestBase {

    public static final String PARAM_VALUE = "hello";

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getTckDeployment(new TestContext().setWebXmlFile("endpoints-web.xml"));
        war.addClasses(FooEndPoint.class, EndPointWithoutName.class, EndPointWithoutVersion.class);
        war.addClasses(EndPointClient.class, EndPointResponse.class);
        war.addAsWebInfResource("someEndPoint-v1.api", FooEndPoint.NAME + "-" + FooEndPoint.VERSION + ".api");
        war.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("com.google.appengine:appengine-endpoints").withoutTransitivity().asFile());
        return war;
    }

    @Test
    @RunAsClient
    public void testMethodWithoutParams(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("withoutParameters")));
        String response = invokeEndpointWithGet(endPointUrl);
        assertEquals("{\"response\":\"method withoutParameters was invoked\"}", response);
    }

    @Test
    @RunAsClient
    public void testMethodWithParameterInQueryString(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("withParameterInQueryString") + "?param=" + PARAM_VALUE));
        String response = invokeEndpointWithGet(endPointUrl);
        assertEquals("{\"response\":\"The param was " + PARAM_VALUE + "\"}", response);
    }

    @Test
    @RunAsClient
    public void testMethodWithParameterInPath(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("withParameterInPath") + "/" + PARAM_VALUE));
        String response = invokeEndpointWithGet(endPointUrl);
        assertEquals("{\"response\":\"The param was " + PARAM_VALUE + "\"}", response);
    }

    @Test
    @RunAsClient
    public void testPost(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("post")));
        EndPointClient client = new EndPointClient(endPointUrl);
        String response = client.doPost();
        assertEquals("{\"response\":\"method post was invoked\"}", response);
    }

    @Test
    @RunAsClient
    public void testPut(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("put")));
        EndPointClient client = new EndPointClient(endPointUrl);
        String response = client.doPut();
        assertEquals("{\"response\":\"method put was invoked\"}", response);
    }

    @Test
    @RunAsClient
    public void testDelete(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("delete")));
        EndPointClient client = new EndPointClient(endPointUrl);
        String response = client.doDelete();
        assertEquals("{\"response\":\"method delete was invoked\"}", response);
    }

    @Test
    @RunAsClient
    public void testEndPointWithoutName(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("myApi", "v1", "withoutParameters")));
        String response = invokeEndpointWithGet(endPointUrl);
        assertEquals("{\"response\":\"method withoutParameters was invoked\"}", response);
    }

    @Test
    @RunAsClient
    public void testEndPointWithoutVersion(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("endPointWithoutVersion", "v1", "withoutParameters")));
        String response = invokeEndpointWithGet(endPointUrl);
        assertEquals("{\"response\":\"method withoutParameters was invoked\"}", response);
    }

    private String invokeEndpointWithGet(URL endPointUrl) throws Exception {
        EndPointClient client = new EndPointClient(endPointUrl);
        return client.doGet();
    }

    private String createPath(String methodPath) {
        return createPath(FooEndPoint.NAME, FooEndPoint.VERSION, methodPath);
    }

    private String createPath(String name, String version, String methodPath) {
        return "/_ah/api/" + name + "/" + version + "/" + methodPath;
    }

    private URL toHttps(URL url) throws MalformedURLException {
        return url;
//        return new URL("https", url.getHost(), url.getFile());
    }

}

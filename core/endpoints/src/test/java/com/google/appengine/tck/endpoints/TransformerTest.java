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
import com.google.appengine.tck.endpoints.support.TransformerEndPoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class TransformerTest extends EndPointsTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext().setWebXmlFile("endpoints-web.xml");
        WebArchive war = getDefaultDeployment(context);
        war.addPackage(TransformerEndPoint.class.getPackage());
        return war;
    }

    @Test
    @RunAsClient
    public void testApiTransformer(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("bar")));
        String response = invokeEndpointWithGet(endPointUrl);
        assertResponse("\"bar\": \"1,2\"", response);
    }

    @Test
    @RunAsClient
    public void testApiTransformerDeclaredInApiAnnotation(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("baz")));
        String response = invokeEndpointWithGet(endPointUrl);
        assertResponse("\"baz\": \"3,4\"", response);
    }

    @Test
    @RunAsClient
    public void testApiResourceProperty(@ArquillianResource URL url) throws Exception {
        URL endPointUrl = toHttps(new URL(url, createPath("foo")));
        String response = invokeEndpointWithPost(endPointUrl);
        assertResponse("\"x\": 1,", response);   // not annotated
        assertNotResponse("\"y\": 2", response); // annotated w/ignored=true
        assertResponse("qwerty\": 3", response); // annotated w/name="querty"

    }

    protected String createPath(String methodPath) {
        return createPath(TransformerEndPoint.NAME, TransformerEndPoint.VERSION, methodPath);
    }
}

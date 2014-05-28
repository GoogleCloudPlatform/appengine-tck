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

package com.google.appengine.tck.misc.servlet;

import java.net.URL;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.misc.servlet.support.Exceptions;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * We're testing $ vs . in classloader' inner classes lookup.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class LoaderTest extends TestBase {
    private static final String content = "<html><body>Error!</body></html>";

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext();
        context.setWebXmlFile("web-error-page.xml");
        WebArchive war = getTckDeployment(context);
        war.addClass(Exceptions.class);
        war.add(new StringAsset(content), "error.html");
        return war;
    }

    private void doPing(HttpUriRequest request) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(request);
            Assert.assertEquals(content, EntityUtils.toString(response.getEntity()));
        }
    }

    @Test
    @RunAsClient
    public void testGet(@ArquillianResource URL root) throws Exception {
        doPing(new HttpGet(new URL(root, "/bang").toURI()));
    }

    @Test
    @RunAsClient
    public void testPost(@ArquillianResource URL root) throws Exception {
        doPing(new HttpPost(new URL(root, "/bang").toURI()));
    }
}

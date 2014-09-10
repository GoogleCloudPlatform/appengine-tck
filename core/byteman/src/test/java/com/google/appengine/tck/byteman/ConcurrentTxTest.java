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

package com.google.appengine.tck.byteman;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.byteman.support.ConcurrentTxServlet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.BMRules;
import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@BMRules({
    @BMRule(
        name = "put",
        targetClass = "com.google.appengine.tck.byteman.support.ConcurrentTxServlet",
        targetMethod = "putEntity",
        targetLocation = "EXIT",
        action = "waitFor(\"cleanup\")",
        exec = ExecType.CLIENT_CONTAINER),
    @BMRule(
        name = "cleanup",
        targetClass = "com.google.appengine.tck.byteman.support.ConcurrentTxServlet",
        targetMethod = "cleanup",
        action = "signalWake(\"cleanup\")",
        exec = ExecType.CLIENT_CONTAINER),
    @BMRule(
        name = "error",
        targetClass = "com.google.appengine.tck.byteman.support.ConcurrentTxServlet",
        targetMethod = "error",
        action = "signalWake(\"cleanup\", true)",
        exec = ExecType.CLIENT_CONTAINER),
}
)
public class ConcurrentTxTest extends TestBase {
    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getTckDeployment(new TestContext().setWebXmlFile("bm-web.xml"));
        war.addClass(ConcurrentTxServlet.class);
        return war;
    }

    private static Thread execute(final CloseableHttpClient client, final HttpUriRequest request, final Holder holder) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    try (CloseableHttpResponse response = client.execute(request)) {
                        holder.out = EntityUtils.toString(response.getEntity());
                    }
                } catch (IOException ignore) {
                }
            }
        });
        thread.start();
        return thread;
    }

    @Test
    @RunAsClient
    public void testDup(@ArquillianResource URI root) throws Exception {
        try (CloseableHttpClient client = HttpClients.createMinimal()) {

            List<Thread> threads = new ArrayList<>();

            Holder h1 = new Holder();
            URIBuilder builder = new URIBuilder(root + "/ctx");
            builder.addParameter("eg", "EG1");
            builder.addParameter("c", "1");
            threads.add(execute(client, new HttpPost(builder.build()), h1));

            Holder h2 = new Holder();
            builder = new URIBuilder(root + "/ctx");
            builder.addParameter("eg", "EG1");
            builder.addParameter("c", "2");
            threads.add(execute(client, new HttpPost(builder.build()), h2));

            for (Thread thread : threads) {
                thread.join();
            }

            System.out.println("h1 = " + h1);
            System.out.println("h2 = " + h2);

            if (h1.out.startsWith("ERROR1")) {
                Assert.assertTrue("Expected ok: " + h2, h2.out.startsWith("OK2"));
                Assert.assertTrue("Expected CME: " + h2, h1.out.contains(ConcurrentModificationException.class.getName()));
            } else {
                Assert.assertTrue("Expected ok: " + h1, h1.out.startsWith("OK1"));
                Assert.assertTrue("Expected error: " + h2, h2.out.startsWith("ERROR2"));
                Assert.assertTrue("Expected CME: " + h2, h2.out.contains(ConcurrentModificationException.class.getName()));
            }
        }
    }

    private static class Holder {
        private String out;

        @Override
        public String toString() {
            return String.valueOf(out);
        }
    }
}

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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
        name = "ctor",
        targetClass = "com.google.appengine.tck.byteman.support.ConcurrentTxServlet",
        targetMethod = "<init>",
        action = "createRendezvous(\"tx\", 2)",
        exec = ExecType.CLIENT_CONTAINER),
    @BMRule(
        name = "put",
        targetClass = "com.google.appengine.tck.byteman.support.ConcurrentTxServlet",
        targetMethod = "putEntity",
        targetLocation = "EXIT",
        action = "rendezvous(\"tx\")",
        exec = ExecType.CLIENT_CONTAINER)
}
)
public class DiffEntityGroupTest extends ConcurrentTxTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        return getBaseDeployment();
    }

    @Test
    @RunAsClient
    public void testOK(@ArquillianResource URI root) throws Exception {
        try (CloseableHttpClient client = HttpClients.createMinimal()) {

            List<Thread> threads = new ArrayList<>();

            Holder h1 = new Holder();
            URIBuilder builder = new URIBuilder(root + "/ctx");
            builder.addParameter("eg", "EG1");
            builder.addParameter("c", "1");
            builder.addParameter("p", "1");
            threads.add(execute(client, new HttpPost(builder.build()), h1));

            Holder h2 = new Holder();
            builder = new URIBuilder(root + "/ctx");
            builder.addParameter("eg", "EG2");
            builder.addParameter("c", "2");
            builder.addParameter("p", "2");
            threads.add(execute(client, new HttpPost(builder.build()), h2));

            join(threads);

            System.out.println("h1 = " + h1);
            System.out.println("h2 = " + h2);

            Assert.assertTrue("Expected ok: " + h1, h1.out.startsWith("OK1"));
            Assert.assertTrue("Expected ok: " + h2, h2.out.startsWith("OK2"));
        }
    }
}

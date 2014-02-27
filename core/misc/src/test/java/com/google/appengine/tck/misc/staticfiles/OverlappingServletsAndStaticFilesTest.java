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

package com.google.appengine.tck.misc.staticfiles;

import java.net.URL;

import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.misc.staticfiles.support.FooServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class OverlappingServletsAndStaticFilesTest extends StaticFilesTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        return getTckDeployment(new TestContext()
            .setWebXmlFile("staticfiles_web.xml")
            .setAppEngineWebXmlFile("appengine-web-staticfiles.xml"))
            .addClass(FooServlet.class)
            .addAsWebResource(new StringAsset("This is /fooservlet/static/foo.html"), "/fooservlet/static/foo.html")
            .addAsWebResource(new StringAsset("This is /fooservlet/nonstatic/foo.html"), "/fooservlet/nonstatic/foo.html")
            .addAsWebResource(new StringAsset("This is /noservlet/static/foo.html"), "/noservlet/static/foo.html")
            .addAsWebResource(new StringAsset("This is /noservlet/nonstatic/foo.html"), "/noservlet/nonstatic/foo.html");
    }

    @Test
    @RunAsClient
    public void testResponseIs404IfFileNotExists(@ArquillianResource URL url) throws Exception {
        assertPageNotFound(url, "noservlet/static/nonExisting.html");
        assertPageNotFound(url, "noservlet/nonstatic/nonExisting.html");
    }

    @Test
    @RunAsClient
    public void testStaticFileIsServedOnlyIfItIsUnderStaticPath(@ArquillianResource URL url) throws Exception {
        assertResponseEquals("This is /noservlet/static/foo.html", url, "noservlet/static/foo.html");
        assertPageNotFound(url, "noservlet/nonstatic/foo.html");
    }

    @Test
    @RunAsClient
    public void testServletMappedUnderStaticPathIsInvokedOnlyIfFileWithSamePathDoesNotActuallyExist(@ArquillianResource URL url) throws Exception {
        assertResponseEquals("Request handled by FooServlet", url, "fooservlet/static/nonExisting.html");
        assertResponseEquals("This is /fooservlet/static/foo.html", url, "fooservlet/static/foo.html");
    }

    @Test
    @RunAsClient
    public void testServletIsInvokedEvenIfFileExistsButIsNotUnderStaticPath(@ArquillianResource URL url) throws Exception {
        assertResponseEquals("Request handled by FooServlet", url, "fooservlet/nonstatic/foo.html");
    }

    @Test
    @RunAsClient
    public void testServletIsInvokedWhenThereIsNoFileUnderSamePath(@ArquillianResource URL url) throws Exception {
        assertResponseEquals("Request handled by FooServlet", url, "fooservlet/nonstatic/nonExisting.html");
    }

}

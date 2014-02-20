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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.misc.staticfiles.support.FooServlet;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class StaticFilesTest extends TestBase {

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



    private void assertResponseEquals(String expectedResponse, URL url, String path) throws URISyntaxException, IOException {
        assertEquals(expectedResponse, getResponseAsString(url, path).trim());
    }

    private String getResponseAsString(URL url, String path) throws URISyntaxException, IOException {
        HttpResponse response = getResponse(url, path);
        return EntityUtils.toString(response.getEntity());
    }

    private void assertPageNotFound(URL url, String path) throws IOException, URISyntaxException {
        HttpResponse response = getResponse(url, path);
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    private HttpResponse getResponse(URL url, String path) throws URISyntaxException, IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(new URL(url, path).toURI());
        return client.execute(get);
    }
}

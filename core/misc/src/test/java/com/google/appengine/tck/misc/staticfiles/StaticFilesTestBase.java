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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.appengine.tck.base.TestBase;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.protocol.modules.ModulesApi;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class StaticFilesTestBase extends TestBase {
    protected static WebArchive createFile(WebArchive archive, String path) {
        return archive.addAsWebResource(new StringAsset("This is " + path), path);
    }

    protected void assertPageFound(URL url, final String path) throws URISyntaxException, IOException {
        assertPageFound(url, path, path);
    }

    protected void assertPageFound(URL url, final String path, String expectedPath) throws URISyntaxException, IOException {
        assertResponseEquals("This is /" + expectedPath, url, path);
    }

    protected void assertDifferentPageFound(URL url, final String path) throws URISyntaxException, IOException {
        assertDifferentPageFound(url, path, path);
    }

    protected void assertDifferentPageFound(URL url, final String path, final String expectedPath) throws URISyntaxException, IOException {
        assertResponse(url, path, new Tester() {
            public void doAssert(HttpResponse response) throws IOException {
                Assert.assertNotEquals("This is /" + expectedPath, EntityUtils.toString(response.getEntity()));
            }
        });
    }

    protected void assertResponseEquals(final String expectedResponse, URL url, String path) throws URISyntaxException, IOException {
        assertResponse(url, path, new Tester() {
            public void doAssert(HttpResponse response) throws IOException {
                assertEquals(expectedResponse, EntityUtils.toString(response.getEntity()).trim());
            }
        });
    }

    protected void assertPageNotFound(URL url, String path) throws IOException, URISyntaxException {
        assertResponse(url, path, new Tester() {
            public void doAssert(HttpResponse response) throws IOException {
                assertEquals(404, response.getStatusLine().getStatusCode());
            }
        });
    }

    protected void assertResponse(URL url, String path, Tester tester) throws URISyntaxException, IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(new URL(url, path).toURI());
            if (ModulesApi.hasCookies()) {
                get.addHeader("Cookie", ModulesApi.getCookies());
            }
            HttpResponse response = client.execute(get);
            tester.doAssert(response);
        }
    }

    protected static interface Tester {
        void doAssert(HttpResponse response) throws IOException;
    }
}

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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class StaticFilesTestBase extends TestBase {
    protected static WebArchive createFile(WebArchive archive, String path) {
        return archive.addAsWebResource(new StringAsset("This is " + path), path);
    }

    protected void assertPageFound(URL url, final String path) throws URISyntaxException, IOException {
        assertResponseEquals("This is /" + path, url, path);
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
        final HttpClient client = new DefaultHttpClient();
        try {
            HttpGet get = new HttpGet(new URL(url, path).toURI());
            HttpResponse response = client.execute(get);
            tester.doAssert(response);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    protected static interface Tester {
        void doAssert(HttpResponse response) throws IOException;
    }
}

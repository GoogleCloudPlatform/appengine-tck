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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.appengine.tck.base.TestContext;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class HeadersStaticFilesTest extends StaticFilesTestBase {

    public static final long _3D_4H_5M_6S_SECONDS = (long) (3 * 24 * 3600 + 4 * 3600 + 5 * 60 + 6);
    public static final long DEFAULT_EXPIRATION_SECONDS = 600L; // 10 minutes

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive archive = getTckDeployment(new TestContext()
            .setAppEngineWebXmlFile("appengine-web-headers-static-files.xml"));
        createFile(archive, "/with-header/foo.txt");
        createFile(archive, "/with-expiration/foo.txt");
        createFile(archive, "/with-default-expiration/foo.txt");
        return archive;
    }

    @Test
    @RunAsClient
    public void testHttpHeaders(@ArquillianResource URL url) throws Exception {
        assertResponse(url, "with-header/foo.txt", new Tester() {
            public void doAssert(HttpResponse response) throws IOException {
                assertEquals("This is /with-header/foo.txt", EntityUtils.toString(response.getEntity()).trim());
                assertHeaderPresent("X-Additional-Header", response);
                assertEquals("This header added through <static-files> in appengine-web.xml",
                    response.getFirstHeader("X-Additional-Header").getValue());
            }
        });
    }

    @Test
    @RunAsClient
    public void testDefaultExpiration(@ArquillianResource URL url) throws Exception {
        assertPageExpiresIn(DEFAULT_EXPIRATION_SECONDS, url, "with-default-expiration/foo.txt");
    }

    @Test
    @RunAsClient
    public void testExpiration(@ArquillianResource URL url) throws Exception {
        assertPageExpiresIn(_3D_4H_5M_6S_SECONDS, url, "with-expiration/foo.txt");
    }

    private void assertPageExpiresIn(final long seconds, URL url, String path) throws URISyntaxException, IOException {
        assertResponse(url, path, new Tester() {
            public static final long EXPIRES_TOLERANCE_SECONDS = 5;

            public void doAssert(HttpResponse response) throws IOException {
                assertHeaderPresent("Expires", response);
                assertHeaderPresent("Cache-Control", response);
                assertEquals("public, max-age=" + seconds, response.getFirstHeader("Cache-Control").getValue());

                Date expires = getDateHeader(response, "Expires");
                Date serverDate = getDateHeader(response, "Date");
                if (serverDate == null) {
                    long expectedExpiresMillis = (System.currentTimeMillis() / 1000 + seconds) * 1000;
                    Date expectedApproximateExpiresDate = new Date(expectedExpiresMillis);
                    long diff = Math.abs(expires.getTime() - expectedExpiresMillis);
                    assertTrue(
                        "Expected Expires to be within " + EXPIRES_TOLERANCE_SECONDS + "s of " + expectedApproximateExpiresDate + " but it was " + expires,
                        diff < EXPIRES_TOLERANCE_SECONDS * 1000);
                } else {
                    Date expectedExpiresDate = new Date(serverDate.getTime() + 1000 * seconds);
                    assertEquals(expectedExpiresDate, expires);
                }
            }
        });
    }

    private Date getDateHeader(HttpResponse response, String headerName) {
        Header header = response.getFirstHeader(headerName);
        return header == null ? null : parseDate(header.getValue());
    }

    private void assertHeaderPresent(String headerName, HttpResponse response) {
        assertNotNull("Expected to find header " + headerName + " in server response, but it was not there",
            response.getFirstHeader(headerName));
    }

    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cannot parse as date: " + date);
        }
    }

}

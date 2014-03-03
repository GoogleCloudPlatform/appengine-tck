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
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.appengine.tck.base.TestContext;
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

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class HeadersStaticFilesTest extends StaticFilesTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive archive = getTckDeployment(new TestContext()
            .setAppEngineWebXmlFile("appengine-web-headers-static-files.xml"));
        createFile(archive, "/with-header/foo.txt");
        createFile(archive, "/with-expiration/foo.txt");
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
    public void testExpiration(@ArquillianResource URL url) throws Exception {
        assertResponse(url, "with-expiration/foo.txt", new Tester() {
            public void doAssert(HttpResponse response) throws IOException {
                assertEquals("This is /with-expiration/foo.txt", EntityUtils.toString(response.getEntity()).trim());
                assertHeaderPresent("Date", response);
                assertHeaderPresent("Expires", response);
                assertHeaderPresent("Cache-Control", response);
                Date date = parseDate(response.getFirstHeader("Date").getValue());
                Date expires = parseDate(response.getFirstHeader("Expires").getValue());
                long seconds_3D_4H_5M_6S = (long) (3 * 24 * 3600 + 4 * 3600 + 5 * 60 + 6);
                assertEquals(new Date(date.getTime() + 1000 * seconds_3D_4H_5M_6S), expires);
                assertEquals("public, max-age=" + seconds_3D_4H_5M_6S, response.getFirstHeader("Cache-Control").getValue());
            }

            private Date parseDate(String date) {
                try {
                    return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH).parse(date);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Cannot parse as date: " + date);
                }
            }

        });
    }

    private void assertHeaderPresent(String headerName, HttpResponse response) {
        assertNotNull("Expected to find header " + headerName + " in server response, but it was not there",
            response.getFirstHeader(headerName));
    }

}

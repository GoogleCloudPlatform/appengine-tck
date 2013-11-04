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

package com.google.appengine.tck.urlfetch;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Charsets;
import com.google.appengine.tck.urlfetch.support.FetchServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class URLFetchTest extends URLFetchTestBase {

    @Test
    public void testAsyncOps() throws Exception {
        URLFetchService service = URLFetchServiceFactory.getURLFetchService();

        URL adminConsole = findAvailableUrl(URLS);
        Future<HTTPResponse> response = service.fetchAsync(adminConsole);
        printResponse(response.get(5, TimeUnit.SECONDS));

        response = service.fetchAsync(new HTTPRequest(adminConsole));
        printResponse(response.get(5, TimeUnit.SECONDS));

        URL jbossOrg = new URL("http://www.jboss.org");
        if (available(jbossOrg)) {
            response = service.fetchAsync(jbossOrg);
            printResponse(response.get(30, TimeUnit.SECONDS));
        }

        sync(5000L); // wait a bit for async to finish
    }

    @Test
    public void testBasicOps() throws Exception {
        URLFetchService service = URLFetchServiceFactory.getURLFetchService();

        URL adminConsole = findAvailableUrl(URLS);
        HTTPResponse response = service.fetch(adminConsole);
        printResponse(response);

        URL jbossOrg = new URL("http://www.jboss.org");
        if (available(jbossOrg)) {
            response = service.fetch(jbossOrg);
            printResponse(response);
        }
    }

    @Test
    public void testPayload() throws Exception {
        URLFetchService service = URLFetchServiceFactory.getURLFetchService();

        URL url = getFetchUrl();

        HTTPRequest req = new HTTPRequest(url, HTTPMethod.POST);
        req.setHeader(new HTTPHeader("Content-Type", "application/octet-stream"));
        req.setPayload("Tralala".getBytes(Charsets.UTF_8));

        HTTPResponse response = service.fetch(req);
        String content = new String(response.getContent());
        Assert.assertEquals("Hopsasa", content);
    }

    @Test
    public void testHeaders() throws Exception {
        URLFetchService service = URLFetchServiceFactory.getURLFetchService();

        URL url = getFetchUrl();

        HTTPRequest req = new HTTPRequest(url, HTTPMethod.POST);
        req.setHeader(new HTTPHeader("Content-Type", "application/octet-stream"));
        req.setPayload("Headers!".getBytes(Charsets.UTF_8));

        HTTPResponse response = service.fetch(req);

        boolean found = false;
        List<HTTPHeader> headers = response.getHeadersUncombined();
        for (HTTPHeader h : headers) {
            if (h.getName().equals("ABC")) {
                Assert.assertEquals("123", h.getValue());
                found = true;
                break;
            }
        }
        Assert.assertTrue("Cannot find matching header <ABC : 123>: " + headers, found);

        found = false;
        headers = response.getHeaders();
        for (HTTPHeader h : headers) {
            if (h.getName().equals("XYZ")) {
                Assert.assertEquals("1, 2, 3", h.getValue());
                found = true;
                break;
            }
        }
        Assert.assertTrue("Cannot find matching header <XYZ : 1,2,3>: " + headers, found);
    }

    @Test
    public void testURLConnection() throws Exception {
        URL fetch = getFetchUrl();
        URLConnection conn = fetch.openConnection();
        Assert.assertTrue(conn instanceof HttpURLConnection);
        HttpURLConnection huc = (HttpURLConnection) conn;
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.addRequestProperty("Content-Type", "application/octet-stream");
        huc.connect();
        try {
            OutputStream out = conn.getOutputStream();
            out.write("Juhuhu".getBytes());
            out.flush();

            String content = new String(FetchServlet.toBytes(conn.getInputStream()));
            Assert.assertEquals("Bruhuhu", content);
            Assert.assertEquals(200, huc.getResponseCode());
        } finally {
            huc.disconnect();
        }
    }
}

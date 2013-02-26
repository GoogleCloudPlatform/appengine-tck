/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.google.appengine.tck.urlfetch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Charsets;
import com.google.apphosting.api.ApiProxy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.urlfetch.FetchServlet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class URLFetchTest extends TestBase {
    static final String[] URLS = {"http://localhost:9990", "http://localhost:8080/_ah/admin", "http://capedwarf-test.appspot.com/index.html"};

    @Deployment
    public static Archive getDeployment() {
      TestContext context = new TestContext();
      context.setWebXmlFile("uf-web.xml");
      WebArchive war = getTckDeployment(context);
      war.addClass(FetchServlet.class);
      return war;
    }

    /**
     * Dummy check if we're available.
     *
     * @param url the url to check against
     * @return true if available, false otherwise
     */
    private static boolean available(URL url) {
        InputStream stream = null;
        try {
            stream = url.openStream();
            int x = stream.read();
            return (x != -1);
        } catch (Exception e) {
            return false;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static URL findAvailableUrl(String... urls) throws Exception {
        for (String s : urls) {
            URL url = new URL(s);
            if (available(url))
                return url;
        }
        throw new IllegalArgumentException("No available url: " + Arrays.toString(urls));
    }

    private static URL getFetchUrl() throws MalformedURLException {
        ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
        Object localhost = env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
        return new URL("http://" + localhost + "/fetch");
    }

    @Test
    public void testAsyncOps() throws Exception {
        URLFetchService service = URLFetchServiceFactory.getURLFetchService();

        URL adminConsole = findAvailableUrl(URLS);
        Future<HTTPResponse> response = service.fetchAsync(adminConsole);
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
        req.setHeader(new HTTPHeader("Content-Type", "application/x-www-form-urlencoded"));
        req.setPayload("Tralala".getBytes(Charsets.UTF_8));

        HTTPResponse response = service.fetch(req);
        String content = new String(response.getContent());
        Assert.assertEquals("Hopsasa", content);
    }

    @Test
    public void testURLConnection() throws Exception {
        URL fetch = getFetchUrl();
        URLConnection conn = fetch.openConnection();
        Assert.assertTrue(conn instanceof HttpURLConnection);
        HttpURLConnection huc = (HttpURLConnection) conn;
        conn.setDoOutput(true);
        conn.addRequestProperty("key", "value");
        huc.connect();
        try {
            OutputStream out = conn.getOutputStream();
            out.write("Juhuhu".getBytes());
            String content = new String(FetchServlet.toBytes(conn.getInputStream()));
            Assert.assertEquals("Bruhuhu", content);
            Assert.assertEquals(200, huc.getResponseCode());
        } finally {
            huc.disconnect();
        }
    }

    private void printResponse(HTTPResponse response) throws Exception {
        System.out.println("response = " + new String(response.getContent()));
    }
}

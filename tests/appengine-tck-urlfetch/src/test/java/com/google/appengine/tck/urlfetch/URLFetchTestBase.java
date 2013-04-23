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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.urlfetch.support.FetchServlet;
import com.google.apphosting.api.ApiProxy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class URLFetchTestBase extends TestBase {
    static final String[] URLS = {"http://localhost:9990", "http://localhost:8080/_ah/admin", "http://capedwarf-test.appspot.com/index.html"};

    @Deployment
    public static Archive getDeployment() {
      TestContext context = new TestContext();
      context.setWebXmlFile("uf-web.xml");
      WebArchive war = getTckDeployment(context);
      war.addClasses(URLFetchTestBase.class, FetchOptionsTestBase.class);
      war.addPackage(FetchServlet.class.getPackage());
      return war;
    }

    /**
     * Dummy check if we're available.
     *
     * @param url the url to check against
     * @return true if available, false otherwise
     */
    protected static boolean available(URL url) {
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

    protected static URL findAvailableUrl(String... urls) throws Exception {
        for (String s : urls) {
            URL url = new URL(s);
            if (available(url))
                return url;
        }
        throw new IllegalArgumentException("No available url: " + Arrays.toString(urls));
    }

    protected static URL getFetchUrl() throws MalformedURLException {
        ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
        Object localhost = env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
        return new URL("http://" + localhost + "/fetch");
    }

    protected void printResponse(HTTPResponse response) throws Exception {
        System.out.println("response = " + new String(response.getContent()));
    }
}

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
import org.jboss.shrinkwrap.api.asset.StringAsset;
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
        war.add(new StringAsset("<html><body>Google AppEngine TCK</body></html>"), "index.html");
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

    protected static URL getUrl(String path) throws MalformedURLException {
        ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
        Object localhost = env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
        return new URL("http://" + localhost + "/" + path);
    }

    protected static URL getFetchUrl() throws MalformedURLException {
        return getUrl("fetch");
    }

    protected void printResponse(HTTPResponse response) throws Exception {
        System.out.println("response = " + new String(response.getContent()));
    }
}

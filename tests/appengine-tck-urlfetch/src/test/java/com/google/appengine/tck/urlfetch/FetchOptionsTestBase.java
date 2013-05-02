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

import java.net.URL;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import org.junit.Assert;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class FetchOptionsTestBase extends URLFetchTestBase {
    protected static final ResponseHandler NOOP = new NoopResponseHandler();

    protected void testOptions(FetchOptions options) throws Exception {
        testOptions(options, NOOP);
    }

    protected void testOptions(FetchOptions options, ResponseHandler handler) throws Exception {
        URL url = getFetchUrl();
        testOptions(url, options, handler);
    }

    protected void testOptions(URL url, FetchOptions options, ResponseHandler handler) throws Exception {
        testOptions(url, HTTPMethod.GET, options, handler);
    }

    protected void testOptions(URL url, HTTPMethod method, FetchOptions options, ResponseHandler handler) throws Exception {
        HTTPRequest request = new HTTPRequest(url, method, options);
        URLFetchService service = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse response = service.fetch(request);
        handler.handle(response);
    }

    protected static interface ResponseHandler {
        void handle(HTTPResponse response) throws Exception;
    }

    private static class NoopResponseHandler implements ResponseHandler {
        public void handle(HTTPResponse response) {
            Assert.assertNotNull(response);
        }
    }
}

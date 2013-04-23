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

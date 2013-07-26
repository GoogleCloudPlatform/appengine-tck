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
import java.net.URL;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tck.urlfetch.support.AppUrlBaseFilter;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * URLFetchService tests
 */
@RunWith(Arquillian.class)
public class URLFetchServiceTest extends URLFetchTestBase {
    public String appUrlBase;

    @Test
    public void fetchExistingPage() throws Exception {
        fetchUrl("http://www.google.org/", 200);
    }

    @Test
    public void fetchNonExistentPage() throws Exception {
        fetchUrl("http://www.google.com/404", 404);
    }

    @Test(expected = IOException.class)
    public void fetchNonExistentSite() throws Exception {
        fetchUrl("http://i.do.not.exist/", 503);
    }

    protected String fetchUrl(String url, int expectedResponse) throws IOException {
        URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse httpResponse = urlFetchService.fetch(new URL(url));
        assertEquals(url, expectedResponse, httpResponse.getResponseCode());
        return new String(httpResponse.getContent());
    }

    // N.B. Tests below this point bypass FastNet

    @Before
    public void setUp() {
        appUrlBase = System.getProperty(AppUrlBaseFilter.APP_URL_BASE);
    }

//  @Test
//  public void verifyFetchingFromOurselves() throws Exception {
//    URL selfURL = new URL(appUrlBase + "/respond/");
//    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults()
//        // N.B. Turning off redirects has the (barely documented) side-effect of
//        //  bypassing FastNet by using http_over_rpc
//        .doNotFollowRedirects()
//        .setDeadline(10.0);
//    HTTPRequest httpRequest = new HTTPRequest(selfURL, HTTPMethod.GET, fetchOptions);
//    URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
//    HTTPResponse httpResponse = urlFetchService.fetch(httpRequest);
//    assertEquals(200, httpResponse.getResponseCode());
//    assertEquals(ResponderServlet.DEFAULT_CONTENT, new String(httpResponse.getContent()));
//  }

    @Test(expected = IOException.class)
    public void fetchNonExistentLocalAppThrowsException() throws Exception {
        URL selfURL = new URL("BOGUS-" + appUrlBase + "/");
        FetchOptions fetchOptions = FetchOptions.Builder.withDefaults()
            .doNotFollowRedirects()
            .setDeadline(10.0);
        HTTPRequest httpRequest = new HTTPRequest(selfURL, HTTPMethod.GET, fetchOptions);
        URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse httpResponse = urlFetchService.fetch(httpRequest);
        fail("expected exception, got " + httpResponse.getResponseCode());
    }

//  @Test
//  public void timeoutRaiseSocketTimeoutException() throws Exception {
//    thrown.expect(SocketTimeoutException.class);
//    URL selfURL = new URL(appUrlBase + "/respond/?action=sleep10");
//    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults()
//      .doNotFollowRedirects()
//      .setDeadline(1.0);
//    HTTPRequest httpRequest = new HTTPRequest(selfURL, HTTPMethod.GET, fetchOptions);
//    URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
//    HTTPResponse httpResponse = urlFetchService.fetch(httpRequest);
//    fail("expected a timeout exception, but got " + httpResponse.getResponseCode());
//  }
}

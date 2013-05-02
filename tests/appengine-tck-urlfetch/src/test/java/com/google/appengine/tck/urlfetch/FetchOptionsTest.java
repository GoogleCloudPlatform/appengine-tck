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
import com.google.appengine.api.urlfetch.HTTPResponse;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class FetchOptionsTest extends FetchOptionsTestBase {
    protected FetchOptions buildFetchOptions() {
        return FetchOptions.Builder.withDefaults();
    }

    @Test
    public void testFollowRedirects() throws Exception {
        final URL redirect = getUrl("redirect");
        FetchOptions options = buildFetchOptions();
        options.followRedirects();
        testOptions(redirect, options, new ResponseHandler() {
            public void handle(HTTPResponse response) throws Exception {
                URL finalURL = response.getFinalUrl();
                Assert.assertEquals(getUrl(""), finalURL);
            }
        });
    }

    @Test
    public void testDoNotFollowRedirects() throws Exception {
        final URL redirect = getUrl("redirect");
        FetchOptions options = buildFetchOptions();
        options.doNotFollowRedirects();
        testOptions(redirect, options, new ResponseHandler() {
            public void handle(HTTPResponse response) throws Exception {
                Assert.assertEquals(302, response.getResponseCode());
            }
        });
    }

    @Test
    public void testAllowTruncate() throws Exception {
        FetchOptions options = buildFetchOptions();
        options.allowTruncate();
        testOptions(options);
    }

    @Test
    public void testDisallowTruncate() throws Exception {
        FetchOptions options = buildFetchOptions();
        options.disallowTruncate();
        testOptions(options);
    }

    @Test
    public void testValidateCertificate() throws Exception {
        FetchOptions options = buildFetchOptions();
        options.validateCertificate();
        testOptions(options);
    }

    @Test
    public void testDoNotValidateCertificate() throws Exception {
        FetchOptions options = buildFetchOptions();
        options.doNotValidateCertificate();
        testOptions(options);
    }

    @Test
    public void testWithDeadline() throws Exception {
        FetchOptions options = buildFetchOptions();
        options.setDeadline(10 * 1000.0);
        testOptions(options);
    }

}

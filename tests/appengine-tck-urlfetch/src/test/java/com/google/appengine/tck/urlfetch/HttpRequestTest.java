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

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class HttpRequestTest extends URLFetchTestBase {

    @Test
    public void testGetters() throws Exception {
        HTTPRequest request = new HTTPRequest(getFetchUrl(), HTTPMethod.PATCH, FetchOptions.Builder.withDefaults());
        request.addHeader(new HTTPHeader("foo", "bar"));
        request.setPayload("qwerty".getBytes());

        Assert.assertEquals(getFetchUrl(), request.getURL());
        Assert.assertEquals(HTTPMethod.PATCH, request.getMethod());
        Assert.assertNotNull(request.getFetchOptions());
        Assert.assertNotNull(request.getHeaders());
        Assert.assertEquals(1, request.getHeaders().size());
        assertEquals(new HTTPHeader("foo", "bar"), request.getHeaders().get(0));
        Assert.assertArrayEquals("qwerty".getBytes(), request.getPayload());
    }

    protected void assertEquals(HTTPHeader expected, HTTPHeader received) {
        Assert.assertEquals(expected.getName(), received.getName());
        Assert.assertEquals(expected.getValue(), received.getValue());
    }
}

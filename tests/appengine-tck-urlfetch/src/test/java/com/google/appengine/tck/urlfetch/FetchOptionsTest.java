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

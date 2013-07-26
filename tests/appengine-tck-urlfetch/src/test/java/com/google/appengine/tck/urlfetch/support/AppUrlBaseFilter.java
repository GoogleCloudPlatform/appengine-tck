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

package com.google.appengine.tck.urlfetch.support;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A filter to capture the base URL of the app, so that it can be available
 * to a unit test (since tests have no direct visibility onto the request).
 */
public class AppUrlBaseFilter implements Filter {

    public static final String APP_URL_BASE = "com.google.watr.appUrlBase";

    public AppUrlBaseFilter() {
    }

    @SuppressWarnings("unused")
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String appUrlBase =
            request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        System.setProperty(APP_URL_BASE, appUrlBase);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}

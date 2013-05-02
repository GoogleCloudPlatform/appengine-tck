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
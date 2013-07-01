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

package com.google.appengine.tck.taskqueue.support;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class RetryTestServlet extends HttpServlet {

    private static List<RequestData> requests = new ArrayList<RequestData>();

//    private static int numberOfTimesToFail;
//    private static int invocationCount;

    protected static final Logger log = Logger.getLogger(RetryTestServlet.class.getName());
    private static MemcacheService cache = MemcacheServiceFactory.getMemcacheService();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String key = req.getParameter("testdata-key");
        int numberOfTimesToFail = Integer.parseInt(req.getParameter("times-to-fail"));

        long count = incInvocationCount(key);
        addRequest(key, new RequestData(req), count);
        if (count <= numberOfTimesToFail) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);   // can be anything outside of 2xx
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

    public static void reset() {
//        invocationCount = 0;
        requests.clear();
    }

    public static void setNumberOfTimesToFail(int failCount) {
//        RetryTestServlet.numberOfTimesToFail = failCount;
    }

    public static long getInvocationCount() {
        return -1;
    }

    public static long getInvocationCount(String testDataKey) {
        String key = getInvocationCountKey(testDataKey);
        return (Long) cache.get(key);
    }

    public static long incInvocationCount(String testDataKey) {
        String key = getInvocationCountKey(testDataKey);
        if (!cache.contains(key)) {
            cache.put(key, 1L);
            return 1L;
        }
        return cache.increment(key, 1L);
    }

    public static void addRequest(String testDataKey, RequestData data, long index) {
        String key = getRequestDataKey(testDataKey, index);
        cache.put(key, data);
        log.info("Adding Request: " + key);
    }

    public static RequestData getRequest(int index) {
        return requests.get(index);
    }

    public static String getInvocationCountKey(String testDataKey) {
        return testDataKey + "-call-count";
    }

    public static String getRequestDataKey(String testDataKey, long index) {
        return testDataKey + "-request-data-" + index;
    }

}

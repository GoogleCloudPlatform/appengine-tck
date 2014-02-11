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

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;

/**
 * This is a test servlet used to execute deferred task queue.
 *
 * @author hchen@google.com (Hannah Chen)
 * @author ales.justin@jboss.org (Ales Justin)
 */
public class ExecDeferred implements DeferredTask {
    private final Map<String, String> testParams;
    private final DatastoreUtil dsUtil;
    private String marker; // retry marker

    public ExecDeferred(DatastoreUtil dsUtil, Map<String, String> testParameters) {
        this(dsUtil, testParameters, null);
    }

    public static ExecDeferred markForRetry(DatastoreUtil dsUtil, Map<String, String> testParameters) {
        return new ExecDeferred(dsUtil, testParameters, UUID.randomUUID().toString());
    }

    private ExecDeferred(DatastoreUtil dsUtil, Map<String, String> testParameters, String marker) {
        this.dsUtil = dsUtil;
        this.testParams = testParameters;
        this.marker = marker;
    }

    @Override
    public void run() {
        Entity entity = null;
        if (marker != null) {
            entity = dsUtil.getMarker(marker);
        }

        HttpServletRequest req = DeferredTaskContext.getCurrentRequest();
        if (marker == null || entity != null) {
            dsUtil.addRequestToDataStore(req, testParams);
        }

        HttpServletResponse resp = DeferredTaskContext.getCurrentResponse();
        resp.setHeader("foo", "bar"); // try to do something more useful with response

        HttpServlet servlet = DeferredTaskContext.getCurrentServlet();
        String sn = servlet.getServletName();
        System.out.println("sn = " + sn);

        if (marker != null && entity == null) {
            DeferredTaskContext.markForRetry();
            dsUtil.putMarker(marker);
        } else {
            DeferredTaskContext.setDoNotRetry(true);
        }
    }
}

package com.google.appengine.tck.taskqueue.support;

import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    public ExecDeferred(DatastoreUtil dsUtil, Map<String, String> testParameters) {
        this.dsUtil = dsUtil;
        this.testParams = testParameters;
    }

    @Override
    public void run() {
        HttpServletRequest req = DeferredTaskContext.getCurrentRequest();
        dsUtil.addRequestToDataStore(req, testParams);

        HttpServletResponse resp = DeferredTaskContext.getCurrentResponse();
        resp.setHeader("foo", "bar"); // try to do something more useful with response

        HttpServlet servlet = DeferredTaskContext.getCurrentServlet();
        String sn = servlet.getServletName();
        System.out.println("sn = " + sn);

        DeferredTaskContext.setDoNotRetry(true);
    }
}

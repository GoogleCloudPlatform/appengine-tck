package com.google.appengine.tck.taskqueue.support;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.appengine.tck.taskqueue.support.Constants.ENTITY_TASK_QUEUE_TEST;

/**
 * This is a test servlet used to execute task queue.
 *
 * @author hchen@google.com (Hannah Chen)
 */
public class ExecTask extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        DatastoreUtil dsUtil = new DatastoreUtil(ENTITY_TASK_QUEUE_TEST, null);
        dsUtil.addRequestToDataStore(req, null);
    }
}

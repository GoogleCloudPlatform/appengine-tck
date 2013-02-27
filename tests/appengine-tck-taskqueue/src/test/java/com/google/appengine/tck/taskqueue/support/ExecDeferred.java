package com.google.appengine.tck.taskqueue.support;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;
import com.google.appengine.tck.taskqueue.support.DatastoreUtil;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This is a test servlet used to execute deferred task queue.
 *
 * @author hchen@google.com (Hannah Chen)
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
  }
}

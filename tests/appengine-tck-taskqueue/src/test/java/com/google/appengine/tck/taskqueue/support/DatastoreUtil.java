package com.google.appengine.tck.taskqueue.support;

import static com.google.appengine.tck.taskqueue.support.Constants.TEST_METHOD_TAG;
import static com.google.appengine.tck.taskqueue.support.Constants.TEST_RUN_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Taskqueue tests store results in Datastore.  This class has helper methods
 * to read/write for test verification.
 */
public class DatastoreUtil implements Serializable {

  private final String testRunId;
  private final String entityName;

  public DatastoreUtil(String entityName, String testRunId) {
    this.entityName = entityName;
    this.testRunId = testRunId;
  }

  public Map<String, String> createParamMap(String testMethodTag) {
    Map<String, String> map = new HashMap<String, String>();
    map.put(TEST_RUN_ID, this.testRunId);
    map.put(TEST_METHOD_TAG, testMethodTag);
    return map;
  }

  public void addRequestToDataStore(HttpServletRequest req,
      Map<String, String> testParameters) {

    Entity queueRec = new Entity(entityName);

    // Set Request headers
    Enumeration headerNames = req.getHeaderNames();
    String headerName;
    while (headerNames.hasMoreElements()) {
      headerName = (String) headerNames.nextElement();
      queueRec.setProperty(headerName, req.getHeader(headerName));
    }

    // Set Request parameters
    Enumeration paramNames = req.getParameterNames();
    String paramName;
    while (paramNames.hasMoreElements()) {
      paramName = (String) paramNames.nextElement();
      queueRec.setProperty(paramName, req.getParameter(paramName));
    }

    // Set Test specific parameters
    if (testParameters != null) {
      for (Map.Entry<String, String> entry : testParameters.entrySet()) {
        queueRec.setProperty(entry.getKey(), entry.getValue());
      }
    }

    DatastoreServiceFactory.getDatastoreService().put(queueRec);
  }

  public void purgeTestRunRecords() {
   DatastoreService datastoreService = DatastoreServiceFactory.
       getDatastoreService();
    FilterPredicate testRunFilter = new FilterPredicate(TEST_RUN_ID,
        FilterOperator.EQUAL, testRunId);
    Query query = new Query(entityName)
        .setFilter(testRunFilter);
    for (Entity readRec : datastoreService.prepare(query).asIterable()) {
      datastoreService.delete(readRec.getKey());
    }
  }

  private CompositeFilter getTestMethodFilter(String testMethodTag) {
    FilterPredicate method = new FilterPredicate(TEST_METHOD_TAG,
        FilterOperator.EQUAL, testMethodTag);
    FilterPredicate testRunFilter = new FilterPredicate(TEST_RUN_ID,
        FilterOperator.EQUAL, testRunId);
    CompositeFilter runMethod = CompositeFilterOperator.and(testRunFilter,
        method);
    return runMethod;
  }

  public Entity waitForTaskThenFetchEntity(int waitIntervalSecs, int retryMax,
      String testMethodTag) {

    long waitIntervalMilliSecs = waitIntervalSecs * 1000;
    Entity entity;

    for (int i = 0; i < retryMax; i++) {
      sleep(waitIntervalMilliSecs);
      entity = fetchEntity(testMethodTag);
      if (entity != null) {
        return entity;
      }
    }
    return null;
  }

  private Entity fetchEntity(String testMethodTag) {
   DatastoreService datastoreService = DatastoreServiceFactory.
       getDatastoreService();
    Query query = new Query(entityName);
    query.setFilter(getTestMethodFilter(testMethodTag));
    FetchOptions fo = FetchOptions.Builder.withDefaults();
    return datastoreService.prepare(query).asSingleEntity();
  }

  private void sleep(long milliSecs) {
    try {
      Thread.sleep(milliSecs);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void assertTaskParamsMatchEntityProperties(Map<String, String> paramMap,
      Entity entity) {
    String paramName;
    String expectedParamValue;
    assertNotNull("Entity doesn't exist. Task probably didn't execute.", entity);
    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
      paramName = entry.getKey();
      expectedParamValue = entry.getValue();
      String errMsg = "Parameter or Header passed to Task not expected.";
      assertEquals(errMsg, expectedParamValue, entity.getProperty(paramName));
    }
  }
}

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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import static com.google.appengine.tck.taskqueue.support.Constants.EXECUTED_AT;
import static com.google.appengine.tck.taskqueue.support.Constants.TEST_METHOD_TAG;
import static com.google.appengine.tck.taskqueue.support.Constants.TEST_RUN_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Taskqueue tests store results in Datastore.  This class has helper methods
 * to read/write for test verification.
 *
 * @author Hannah
 * @author Ales Justin
 */
public class DatastoreUtil implements Serializable {
    private final String testRunId;
    private final String entityName;

    public DatastoreUtil(String entityName, String testRunId) {
        this.entityName = entityName;
        this.testRunId = testRunId;
    }

    public Map<String, String> createParamMap(String testMethodTag) {
        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.put(TEST_RUN_ID, this.testRunId);
        map.put(TEST_METHOD_TAG, testMethodTag);
        return map;
    }

    public void addRequestToDataStore(HttpServletRequest req, Map<String, String> testParameters) {
        Entity queueRec = new Entity(entityName);
        queueRec.setProperty(EXECUTED_AT, System.currentTimeMillis());

        // Set Request headers
        Enumeration headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            queueRec.setProperty(headerName, req.getHeader(headerName));
        }

        // Set Request parameters
        Enumeration paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
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
        DatastoreService datastoreService = DatastoreServiceFactory. getDatastoreService();
        FilterPredicate testRunFilter = new FilterPredicate(TEST_RUN_ID, FilterOperator.EQUAL, testRunId);
        Query query = new Query(entityName).setFilter(testRunFilter);
        for (Entity readRec : datastoreService.prepare(query).asIterable()) {
            datastoreService.delete(readRec.getKey());
        }
    }

    private CompositeFilter getTestMethodFilter(String testMethodTag) {
        FilterPredicate method = new FilterPredicate(TEST_METHOD_TAG, FilterOperator.EQUAL, testMethodTag);
        FilterPredicate testRunFilter = new FilterPredicate(TEST_RUN_ID, FilterOperator.EQUAL, testRunId);
        return CompositeFilterOperator.and(testRunFilter, method);
    }

    public Entity waitForTaskThenFetchEntity(int waitIntervalSecs, int retryMax, String testMethodTag) {
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

    private void sleep(long milliSecs) {
        try {
            Thread.sleep(milliSecs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private Entity fetchEntity(String testMethodTag) {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query(entityName);
        query.setFilter(getTestMethodFilter(testMethodTag));
        return datastoreService.prepare(query).asSingleEntity();
    }

    public void assertTaskParamsMatchEntityProperties(Map<String, String> paramMap, Entity entity) {
        assertNotNull("Entity doesn't exist. Task probably didn't execute.", entity);
        final String errMsg = "Parameter or Header passed to Task not expected.";
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            String paramName = entry.getKey();
            String expectedParamValue = entry.getValue();
            Object actualValue = entity.getProperty(paramName);
            if (actualValue == null) {
                actualValue = entity.getProperty(paramName.toLowerCase());
            }
            assertEquals(errMsg, expectedParamValue, actualValue);
        }
    }
}

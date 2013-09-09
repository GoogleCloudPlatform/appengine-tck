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

package com.google.appengine.tck.taskqueue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tck.taskqueue.support.DatastoreUtil;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING;
import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING_EXEC;
import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING_RETRY;
import static com.google.appengine.tck.taskqueue.support.Constants.ENTITY_TASK_QUEUE_TEST;
import static com.google.appengine.tck.taskqueue.support.Constants.EXECUTED_AT;
import static com.google.appengine.tck.taskqueue.support.Constants.TEST_METHOD_TAG;
import static com.google.appengine.tck.taskqueue.support.Constants.TEST_RUN_ID;

/**
 * Push Task Queues Tests
 *
 * @author terryok@google.com (Terry Okamoto)
 * @author mluksa@redhat.com (Marko Luksa)
 * @author ales.justin@jboss.org (Ales Justin)
 */
@RunWith(Arquillian.class)
public class TaskQueueTest extends QueueTestBase {

    private String testRunId;
    private DatastoreUtil dsUtil;
    private int waitInterval;
    private int retryMax;


    @Before
    public void setUp() {
        testRunId = Long.toString(System.currentTimeMillis());
        dsUtil = new DatastoreUtil(ENTITY_TASK_QUEUE_TEST, testRunId);
        waitInterval = 4;
        retryMax = 52 / waitInterval;  // 60 sec limit, leave buffer time.

        purgeAndPause(QueueFactory.getQueue(E2E_TESTING),  QueueFactory.getQueue(E2E_TESTING_EXEC));
    }

    @After
    public void tearDown() {
        dsUtil.purgeTestRunRecords();
        purgeAndPause(QueueFactory.getQueue(E2E_TESTING),  QueueFactory.getQueue(E2E_TESTING_EXEC));
    }

    @Test
    public void testExecQueue() {
        String testMethodTag = "testDefaultTag";  // Each test tagged for DS entity.
        TaskOptions taskoptions = TaskOptions.Builder
            .withMethod(TaskOptions.Method.POST)
            .param(TEST_RUN_ID, testRunId)  // testRunId used to track test in DS.
            .param(TEST_METHOD_TAG, testMethodTag)
            .etaMillis(0);

        QueueFactory.getQueue(E2E_TESTING_EXEC).add(taskoptions);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
            testMethodTag);
        Map<String, String> expectedParams = dsUtil.createParamMap(testMethodTag);
        dsUtil.assertTaskParamsMatchEntityProperties(expectedParams, entity);
    }

    @Test
    public void testAddTaskWithNoName() {
        String testMethodTag = "testAddTaskWithNoName";
        TaskOptions optionsNoName = TaskOptions.Builder
            .withMethod(TaskOptions.Method.POST)
            .param(TEST_RUN_ID, testRunId)
            .param(TEST_METHOD_TAG, testMethodTag)
            .etaMillis(0)
            .url("/queuetask/addentity");

        QueueFactory.getQueue(E2E_TESTING).add(optionsNoName);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax, testMethodTag);
        Map<String, String> expectedParams = dsUtil.createParamMap(testMethodTag);
        dsUtil.assertTaskParamsMatchEntityProperties(expectedParams, entity);
    }

    @Test
    public void testTaskNameSpecified() {
        // Add Task with specified name.
        String taskName = "This_is_my_task_name_" + testRunId;
        String testMethodTag = "testTaskNameSpecified";
        TaskOptions optionsHasName = TaskOptions.Builder
            .withMethod(TaskOptions.Method.POST)
            .param(TEST_RUN_ID, testRunId)
            .param(TEST_METHOD_TAG, testMethodTag)
            .taskName(taskName)
            .url("/queuetask/addentity")
            .etaMillis(0);

        QueueFactory.getQueue(E2E_TESTING).add(optionsHasName);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax, testMethodTag);
        Map<String, String> expectedParams = dsUtil.createParamMap(testMethodTag);
        expectedParams.put("X-AppEngine-TaskName", taskName);
        dsUtil.assertTaskParamsMatchEntityProperties(expectedParams, entity);
    }

    @Test
    public void testRetryOption() {
        String testMethodTag = "testRetryOption";
        RetryOptions retryOptions = new RetryOptions(RetryOptions.Builder.withDefaults())
            .taskRetryLimit(5)
            .taskAgeLimitSeconds(10)
            .maxBackoffSeconds(10)
            .maxDoublings(10)
            .minBackoffSeconds(10);

        TaskOptions taskOptions = TaskOptions.Builder
            .withMethod(TaskOptions.Method.POST)
            .param(TEST_RUN_ID, testRunId)
            .param(TEST_METHOD_TAG, testMethodTag)
            .retryOptions(retryOptions)
            .url("/queuetask/addentity");

        QueueFactory.getQueue(E2E_TESTING).add(taskOptions);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax, testMethodTag);
        Map<String, String> expectedParams = dsUtil.createParamMap(testMethodTag);
        dsUtil.assertTaskParamsMatchEntityProperties(expectedParams, entity);
    }

    @Test
    public void testRetryOptionViaConfigFile() {
        String testMethodTag = "testRetryOptionViaConfigFile";

        TaskOptions taskOptions = TaskOptions.Builder
            .withMethod(TaskOptions.Method.POST)
            .param(TEST_RUN_ID, testRunId)
            .param(TEST_METHOD_TAG, testMethodTag)
            .url("/queuetask/addentity");

        // retry param. are defined in queue.xml
        QueueFactory.getQueue(E2E_TESTING_RETRY).add(taskOptions);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax, testMethodTag);
        Map<String, String> expectedParams = dsUtil.createParamMap(testMethodTag);
        dsUtil.assertTaskParamsMatchEntityProperties(expectedParams, entity);
    }

    @Test
    public void testUserNameSpace() {
        String testMethodTag = "testUserNameSpace";
        NamespaceManager.set("junittest");

        TaskOptions taskOptions = TaskOptions.Builder
            .withMethod(TaskOptions.Method.POST)
            .param(TEST_RUN_ID, testRunId)
            .param(TEST_METHOD_TAG, testMethodTag)
            .url("/queuetask/addentity");
        // task name explicitly not specified.

        QueueFactory.getQueue(E2E_TESTING).add(taskOptions);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax, testMethodTag);
        Map<String, String> expectedParams = dsUtil.createParamMap(testMethodTag);
        dsUtil.assertTaskParamsMatchEntityProperties(expectedParams, entity);
    }

    @Test
    public void testEtaMillis() {
        String testMethodTag = "testEtaMillis";
        long etaMillis = System.currentTimeMillis() + 10000;
        TaskOptions taskoptions = TaskOptions.Builder
            .withMethod(TaskOptions.Method.POST)
            .param(TEST_RUN_ID, testRunId)
            .param(TEST_METHOD_TAG, testMethodTag)
            .etaMillis(etaMillis);

        QueueFactory.getQueue(E2E_TESTING_EXEC).add(taskoptions);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax, testMethodTag);
        long executedAt = (Long) entity.getProperty(EXECUTED_AT);
        Assert.assertTrue(executedAt >= etaMillis);
    }

    @Test
    public void testCountdownMillis() {
        String testMethodTag = "testCountdownMillis";
        int countdownMillis = 10000;
        TaskOptions taskoptions = TaskOptions.Builder
            .withMethod(TaskOptions.Method.POST)
            .param(TEST_RUN_ID, testRunId)
            .param(TEST_METHOD_TAG, testMethodTag)
            .countdownMillis(countdownMillis);

        long etaMillis = System.currentTimeMillis() + countdownMillis;
        QueueFactory.getQueue(E2E_TESTING_EXEC).add(taskoptions);

        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax, testMethodTag);
        long executedAt = (Long) entity.getProperty(EXECUTED_AT);
        Assert.assertTrue("Expected executed_at to be >= " + etaMillis + ", but was: " + executedAt, executedAt >= etaMillis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUrlNotAllowed() {
        getDefaultQueue().add(TaskOptions.Builder.withUrl(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExternalUrlNotAllowed() {
        getDefaultQueue().add(TaskOptions.Builder.withUrl("http://www.google.com/foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativeUrlNotAllowed() {
        getDefaultQueue().add(TaskOptions.Builder.withUrl("someRelativeUrl"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFragmentNotAllowedInUrl() {
        getDefaultQueue().add(TaskOptions.Builder.withUrl("/foo#fragment"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBothQueryStringAndParameterNotAllowed() {
        getDefaultQueue().add(TaskOptions.Builder.withUrl("/someUrl?withQueryString=foo").param("andParam", "bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPostMethodTaskCannotContainQueryString() {
        getDefaultQueue().add(TaskOptions.Builder.withMethod(TaskOptions.Method.POST).url("/someUrl?withQueryString=foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteMethodTaskCannotHavePayload() {
        getDefaultQueue().add(TaskOptions.Builder.withMethod(TaskOptions.Method.DELETE).payload("payload"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMethodTaskCannotHavePayload() {
        getDefaultQueue().add(TaskOptions.Builder.withMethod(TaskOptions.Method.GET).payload("payload"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeadMethodTaskCannotHavePayload() {
        getDefaultQueue().add(TaskOptions.Builder.withMethod(TaskOptions.Method.HEAD).payload("payload"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPostMethodTaskCannotHaveBothPayloadAndParams() {
        getDefaultQueue().add(
            TaskOptions.Builder.withMethod(TaskOptions.Method.POST)
                .payload("payload")
                .param("someParam", "foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransactionalTasksMustBeNamelessSingle() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        try {
            TaskOptions options = TaskOptions.Builder.withTaskName("foo");
            getDefaultQueue().add(tx, new TaskOptions(options));
        } finally {
            tx.rollback();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransactionalTasksMustBeNamelessIterable() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        try {
            getDefaultQueue().add(tx, Collections.singleton(TaskOptions.Builder.withTaskName("foo")));
        } finally {
            tx.rollback();
        }
    }

    @Test(expected = TaskAlreadyExistsException.class)
    public void testAddingTwoTasksWithSameNameThrowsException() {
        String taskName = "sameName";
        Queue queue = getDefaultQueue();
        // TODO -- perhaps change this with delay on servlet side?
        queue.add(TaskOptions.Builder.withTaskName(taskName).countdownMillis(10 * 1000L));
        queue.add(TaskOptions.Builder.withTaskName(taskName));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddingTwoTasksWithSameNameInSingleRequestThrowsException() {
        String taskName = "sameName2";
        getDefaultQueue().add(
            Arrays.asList(
                TaskOptions.Builder.withTaskName(taskName),
                TaskOptions.Builder.withTaskName(taskName)));
    }

    private Queue getDefaultQueue() {
        return QueueFactory.getDefaultQueue();
    }

}

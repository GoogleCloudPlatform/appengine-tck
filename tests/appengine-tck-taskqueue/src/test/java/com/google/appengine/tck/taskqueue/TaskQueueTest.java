package com.google.appengine.tck.taskqueue;

import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING;
import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING_RETRY;
import static com.google.appengine.tck.taskqueue.support.Constants.ENTITY_TASK_QUEUE_TEST;
import static com.google.appengine.tck.taskqueue.support.Constants.TEST_METHOD_TAG;
import static com.google.appengine.tck.taskqueue.support.Constants.TEST_RUN_ID;
import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tck.taskqueue.support.DatastoreUtil;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

/**
 * Push Task Queues Tests
 *
 * @author terryok@google.com (Terry Okamoto)
 */

@RunWith(Arquillian.class)
public class TaskQueueTest extends TaskqueueTestBase {

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
  }

  @After
  public void tearDown() {
    dsUtil.purgeTestRunRecords();
  }

  @Test
  public void testDefaultQueue() {
    String testMethodTag = "testDefaultTag";  // Each test tagged for DS entity.
    TaskOptions taskoptions = TaskOptions.Builder
        .withMethod(TaskOptions.Method.POST)
        .param(TEST_RUN_ID, testRunId)  // testRunId used to track test in DS.
        .param(TEST_METHOD_TAG, testMethodTag)
        .etaMillis(0);

    QueueFactory.getDefaultQueue().add(taskoptions);
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
    Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
        testMethodTag);
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
    Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
        testMethodTag);
    Map<String, String> expectedParams = dsUtil.createParamMap(testMethodTag);
    expectedParams.put("X-AppEngine-TaskName", taskName);
    dsUtil.assertTaskParamsMatchEntityProperties(expectedParams, entity);
  }

  @Test
  public void testRetryOption() {
    String testMethodTag = "testRetryOption";
    RetryOptions retryOptions = RetryOptions.Builder
        .withTaskRetryLimit(5)
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
    Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
        testMethodTag);
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
    Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
        testMethodTag);
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
    Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
        testMethodTag);
    Map<String, String> expectedParams = dsUtil.createParamMap(testMethodTag);
    dsUtil.assertTaskParamsMatchEntityProperties(expectedParams, entity);
  }
}

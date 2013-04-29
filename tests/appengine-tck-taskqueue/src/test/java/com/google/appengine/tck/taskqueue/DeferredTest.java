package com.google.appengine.tck.taskqueue;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tck.taskqueue.support.DatastoreUtil;
import com.google.appengine.tck.taskqueue.support.ExecDeferred;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING_DEFERRED;
import static com.google.appengine.tck.taskqueue.support.Constants.ENTITY_DEFERRED_TEST;

/**
 * Deferred Task Queue Test.
 *
 * @author hchen@google.com (Hannah Chen)
 */

@RunWith(Arquillian.class)
public class DeferredTest extends QueueTestBase {
    private String testRunId;
    private DatastoreUtil dsUtil;
    private int waitInterval;
    private int retryMax;

    @Before
    public void setUp() {
        testRunId = Long.toString(System.currentTimeMillis());
        dsUtil = new DatastoreUtil(ENTITY_DEFERRED_TEST, testRunId);
        waitInterval = 4;
        retryMax = 52 / waitInterval;  // Must finish in 60 sec, leave buffer time.
    }

    @After
    public void tearDown() {
        dsUtil.purgeTestRunRecords();
    }

    @Test
    public void testDeferredDefault() {
        String testMethodTag = "testDefault";
        Map<String, String> paramMap = dsUtil.createParamMap(testMethodTag);

        TaskOptions taskOptions = TaskOptions.Builder
                .withPayload(new ExecDeferred(dsUtil, paramMap));

        QueueFactory.getDefaultQueue().add(taskOptions);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
                testMethodTag);

        dsUtil.assertTaskParamsMatchEntityProperties(paramMap, entity);
    }

    @Test
    public void testDeferredTaskWithNoName() {
        String testMethodTag = "testDeferredTaskWithNoName";
        Map<String, String> paramMap = dsUtil.createParamMap(testMethodTag);

        TaskOptions taskOptions = TaskOptions.Builder
                .withPayload(new ExecDeferred(dsUtil, paramMap));

        QueueFactory.getQueue(E2E_TESTING_DEFERRED).add(taskOptions);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
                testMethodTag);

        dsUtil.assertTaskParamsMatchEntityProperties(paramMap, entity);
    }

    @Test
    public void testDeferredTaskNameSpecified() {
        String taskName = "This_is_my_deferred_task_name_" + testRunId;
        String testMethodTag = "testDeferredTaskNameSpecified";
        Map<String, String> paramMap = dsUtil.createParamMap(testMethodTag);

        TaskOptions taskOptions = TaskOptions.Builder
                .withPayload(new ExecDeferred(dsUtil, paramMap))
                .taskName(taskName);

        QueueFactory.getQueue(E2E_TESTING_DEFERRED).add(taskOptions);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
                testMethodTag);

        Map<String, String> expectedMap = new HashMap<String, String>(paramMap);
        expectedMap.put("X-AppEngine-TaskName", taskName);
        dsUtil.assertTaskParamsMatchEntityProperties(expectedMap, entity);
    }

    @Test
    public void testDeferredUserNS() {
        String testMethodTag = "testDeferredUserNS";
        String specifiedNameSpace = "the_testDeferredUserNS";
        Map<String, String> paramMap = dsUtil.createParamMap(testMethodTag);

        NamespaceManager.set(specifiedNameSpace);

        TaskOptions taskOptions = TaskOptions.Builder
                .withPayload(new ExecDeferred(dsUtil, paramMap));

        // no task name specified.
        QueueFactory.getQueue(E2E_TESTING_DEFERRED).add(taskOptions);
        Entity entity = dsUtil.waitForTaskThenFetchEntity(waitInterval, retryMax,
                testMethodTag);

        Map<String, String> expectedMap = new HashMap<String, String>(paramMap);
        expectedMap.put("X-AppEngine-Current-Namespace", specifiedNameSpace);
        dsUtil.assertTaskParamsMatchEntityProperties(expectedMap, entity);
    }
}

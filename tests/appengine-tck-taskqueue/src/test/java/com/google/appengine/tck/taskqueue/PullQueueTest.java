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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.appengine.api.taskqueue.InvalidQueueModeException;
import com.google.appengine.api.taskqueue.LeaseOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TransientFailureException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withMethod;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.PULL;
import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING_PULL;
import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING_REMOTE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Pull Queue Test.
 *
 * @author terryok@google.com (Terry Okamoto)
 * @author mluksa@redhat.com (Marko Luksa)
 */
@RunWith(Arquillian.class)
public class PullQueueTest extends QueueTestBase {
    private static final int MAX_LEASE_COUNT = 1000;
    private Queue queue;
    private String timeStamp;  // make task names unique for test run.
    private String payload;

    // Create tasks with addTasks() tracking.  They will be deleted
    // during tearDown().
    private final List<String> taskTags = new ArrayList<>();
    private final List<String> deleteOnTearDownTags = new ArrayList<>();

    private static final Logger log =
        Logger.getLogger(PullQueueTest.class.getName());

    @Before
    public void setUp() {
        queue = QueueFactory.getQueue(E2E_TESTING_PULL);
        purgeAndPause(queue);

        timeStamp = Long.toString(System.currentTimeMillis());
        payload = "mypayload";
    }

    @After
    public void tearDown() {
        List<TaskHandle> tasks;
        for (String taskGroupTag : deleteOnTearDownTags) {
            tasks = leaseTasksByTag60Secs(taskGroupTag, MAX_LEASE_COUNT, false);
            deleteMultipleTasks(tasks);
        }
        purgeAndPause(queue);
    }

    @Test
    public void testBasicLease() {
        String taskGroupTag = "testBasicLease";
        String taskBaseName = taskGroupTag + "_" + getTimeStampRandom();
        int count = 10;

        addTasks(count, taskBaseName, taskGroupTag, payload);

        // lease 1 task from pull queue
        List<TaskHandle> handleList = leaseTasksByTag60Secs(taskGroupTag, 1, false);
        assertEquals(1, handleList.size());
        deleteTaskByName(handleList.get(0).getName());

        // lease the rest from pull queue
        handleList = leaseTasksByTag60Secs(taskGroupTag, MAX_LEASE_COUNT, false);
        assertEquals(count - 1, handleList.size());

        for (TaskHandle t : handleList) {
            assertTrue(t.getName().startsWith(taskBaseName));
            assertEquals(E2E_TESTING_PULL, t.getQueueName());
            assertEquals(payload, new String(t.getPayload()));
            deleteTaskByName(t.getName());
        }
    }

    @Test(expected = InvalidQueueModeException.class)
    public void testLeaseFromNonPullQueue() {
        Queue remoteQueue = QueueFactory.getQueue(E2E_TESTING_REMOTE);
        remoteQueue.leaseTasks(1, TimeUnit.MILLISECONDS, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void testLeaseNonExistQueue() {
        Queue nonExistQueue = QueueFactory.getQueue("nonExistQueue");
        nonExistQueue.leaseTasks(1, TimeUnit.MILLISECONDS, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void testDeleteNonExist() {
        Queue nonExistQueue = QueueFactory.getQueue("nonExistQueue");
        nonExistQueue.deleteTask("nonexist");
    }

    @Test
    public void testDelete() {
        String groupTag = "testDelete";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();

        // delete non exist task
        assertEquals("Deleting non-existing task.", false, queue.deleteTask("nonexisttask"));

        // delete tasks by list of taskHandles
        int count = 2;
        addTasks(count, taskBaseName, groupTag, payload);
        List<TaskHandle> handleList = leaseTasksByTag60Secs(groupTag, count, false);
        assertEquals(count, handleList.size());
        queue.deleteTask(handleList);
        sync(7000);  // Needs extra time to delete.
        handleList = leaseTasksByTag60Secs(groupTag, count, true);
        assertEquals(0, handleList.size());

        // delete task by name and taskHandle
        count = 2;
        taskBaseName += "_delete_by_name";
        addTasks(count, taskBaseName, groupTag, payload);
        handleList = leaseTasksByTag60Secs(groupTag, count, false);
        assertEquals(count, handleList.size());

        queue.deleteTask(handleList.get(0).getName());
        queue.deleteTask(handleList.get(1));
        sync(7000);  // Needs extra time to delete.
        handleList = leaseTasksByTag60Secs(groupTag, count, true);
        assertEquals(0, handleList.size());
    }

    @Test
    public void testModifyTaskLease() {
        String groupTag = "testModifyTaskLease";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();
        String uniqueGroupTag = taskBaseName;

        int count = 1;
        addTasks(count, taskBaseName, uniqueGroupTag, "nada");

        // lease all tasks for a second lease period
        long leaseDuration = 10000;
        LeaseOptions options = LeaseOptions.Builder
            .withTag(uniqueGroupTag)
            .countLimit(count)
            .leasePeriod(leaseDuration, TimeUnit.MILLISECONDS);

        List<TaskHandle> tasks = leaseTasksByOptions(uniqueGroupTag, count, false, options);
        assertEquals(1, tasks.size());

        // reset lease to 60 seconds.
        queue.modifyTaskLease(tasks.get(0), 30, TimeUnit.SECONDS);

        sync(leaseDuration + 10000);  // wait for lease to expire...
        List<TaskHandle> tasksAfterExpire = queue.leaseTasks(options);

        // ...but it shouldn't expire since lease time was extended.
        assertEquals(0, tasksAfterExpire.size());

        deleteMultipleTasks(tasks);
    }

    @Test(expected = IllegalStateException.class)
    public void testModifyNonLeasedTask() {
        String groupTag = "testModifyNonLeasedTask";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();
        deleteOnTearDownTags.add(taskBaseName);

        int count = 1;
        List<TaskHandle> taskList = addTasks(count, taskBaseName, taskBaseName, payload);
        queue.modifyTaskLease(taskList.get(0), 2, TimeUnit.SECONDS);
    }

    @Test
    public void testLeaseExpiration() {
        String groupTag = "testLeaseExpiration";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();
        deleteOnTearDownTags.add(taskBaseName);

        int count = 1;
        List<TaskHandle> taskList = addTasks(count, taskBaseName, groupTag, "nada");
        long leaseDuration = 1000;
        LeaseOptions options = LeaseOptions.Builder
            .withTag(groupTag)
            .countLimit(count)
            .leasePeriod(leaseDuration, TimeUnit.MILLISECONDS);

        List<TaskHandle> tasks = queue.leaseTasks(options);
        assertEquals(count, tasks.size());

        sync(leaseDuration + 1000); // wait for lease to expire
        List<TaskHandle> tasksAfterExpire = queue.leaseTasks(options);

        // expired, so it should be available for lease.
        assertEquals(count, tasksAfterExpire.size());

        deleteMultipleTasks(tasksAfterExpire);
    }

    @Test
    public void testPullWithTag() throws Exception {
        String groupTag = "testPullWithTag";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();

        TaskHandle th = queue.add(withMethod(PULL).tag(taskBaseName));
        try {
            List<TaskHandle> handles = queue.leaseTasksByTag(30, TimeUnit.MINUTES, 100, taskBaseName);
            assertFalse(handles.isEmpty());
            TaskHandle lh = handles.get(0);
            assertEquals(th.getName(), lh.getName());
        } finally {
            queue.deleteTask(th);
        }
    }

    @Test
    public void testPullWithGroupByTag() throws Exception {
        String groupTag = "testPullWithGroupTag";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();
        TaskHandle th1 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foobar", "UTF-8"));
        TaskHandle th2 = queue.add(withMethod(PULL).tag(taskBaseName + "other").payload("foofoo", "UTF-8"));
        TaskHandle th3 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foofoo".getBytes(), "UTF-8"));

        sync(5000); // Give some time for the tasks to be available for lease.

        try {
            // If options specifies no tag, but does specify groupByTag,
            // only tasks having the same tag as the task with earliest eta will be returned.
            LeaseOptions options = LeaseOptions.Builder.withLeasePeriod(30, TimeUnit.SECONDS).countLimit(100).groupByTag();
            List<TaskHandle> handles = queue.leaseTasks(options);

            // The first and third tasks with the same tag should be returned.
            assertEquals(2, handles.size());

            Set<String> createdHandles = new HashSet<>();
            createdHandles.add(th1.getName());
            createdHandles.add(th3.getName());

            Set<String> returnedHandles = new HashSet<>();
            returnedHandles.add(handles.get(0).getName());
            returnedHandles.add(handles.get(1).getName());

            assertEquals(createdHandles, returnedHandles);

            assertEquals(taskBaseName, th1.getTag());
            Assert.assertArrayEquals(taskBaseName.getBytes(), th1.getTagAsBytes());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
            queue.deleteTask(th3);
        }
    }

    @Test
    public void testExtractParams() throws Exception {
        String groupTag = "testExtractParams";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();
        queue.add(withMethod(PULL).tag(taskBaseName).payload("foobar=baz", "UTF-8"));
        List<TaskHandle> tasks = queue.leaseTasks(LeaseOptions.Builder.withCountLimit(1).leasePeriod(1, TimeUnit.MINUTES));
        Assert.assertEquals(1, tasks.size());
        TaskHandle th = tasks.get(0);
        List<Map.Entry<String, String>> entries = th.extractParams();
        Assert.assertEquals(1, entries.size());
        Assert.assertEquals("foobar", entries.get(0).getKey());
        Assert.assertEquals("baz", entries.get(0).getValue());
    }

    @Test
    public void testLeaseTasksByTagBytes() {
        String groupTag = "testLeaseTasksByTagBytes";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();
        taskTags.add(taskBaseName);
        byte[] tagBytes = taskBaseName.getBytes();

        TaskOptions options =
            withMethod(TaskOptions.Method.PULL)
                .taskName(taskBaseName + "_0")
                .tag(tagBytes)
                .payload("");
        queue.add(options);
        sync(5000);  // Give tasks a chance to become available.

        List<TaskHandle> tasks = queue.leaseTasksByTagBytes(1, TimeUnit.SECONDS, 10, tagBytes);
        assertEquals(1, tasks.size());
        queue.deleteTask(tasks);
    }

    @Test
    public void testPullMultipleWithSameTag() throws Exception {
        String groupTag = "testLeaseTasksByTagBytes";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();
        TaskHandle th1 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foobar", "UTF-8"));
        TaskHandle th2 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foofoo".getBytes(), "UTF-8"));
        sync();
        try {
            int numTasksToLease = 100;
            List<TaskHandle> handles = queue.leaseTasksByTag(30, TimeUnit.MINUTES, numTasksToLease, taskBaseName);
            assertEquals(2, handles.size());

            Set<String> createdHandles = new HashSet<>();
            createdHandles.add(th1.getName());
            createdHandles.add(th2.getName());

            Set<String> returnedHandles = new HashSet<>();
            returnedHandles.add(handles.get(0).getName());
            returnedHandles.add(handles.get(1).getName());

            assertEquals(createdHandles, returnedHandles);
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
        }
    }

    @Test
    public void testPullMultipleWithDiffTag() throws Exception {
        String groupTag = "testPullMultipleWithDiffTag";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();

        TaskHandle th1 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foobar"));
        TaskHandle th2 = queue.add(withMethod(PULL).tag(taskBaseName + "other").payload("foofoo".getBytes()));
        TaskHandle th3 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foofoo".getBytes()));
        sync();
        try {
            List<TaskHandle> handles = queue.leaseTasksByTag(30, TimeUnit.MINUTES, 100, taskBaseName);
            assertEquals(2, handles.size());

            handles = queue.leaseTasksByTag(30, TimeUnit.MINUTES, 100, taskBaseName + "other");
            assertEquals(1, handles.size());
            assertEquals(th2.getName(), handles.get(0).getName());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
            queue.deleteTask(th3);
        }
    }

    @Test
    public void testPullMultipleWithSameTagWithOptions1() throws Exception {
        String groupTag = "testPullMultipleWithSameTagWithOptions1";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();

        TaskHandle th1 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foobar".getBytes()));
        TaskHandle th2 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foofoo"));
        sync();
        try {
            LeaseOptions lo = LeaseOptions.Builder
                .withLeasePeriod(30, TimeUnit.MINUTES).countLimit(100).tag(taskBaseName);
            List<TaskHandle> handles = queue.leaseTasks(lo);
            assertEquals(2, handles.size());

            Set<String> createdHandles = new HashSet<>();
            createdHandles.add(th1.getName());
            createdHandles.add(th2.getName());

            Set<String> returnedHandles = new HashSet<>();
            returnedHandles.add(handles.get(0).getName());
            returnedHandles.add(handles.get(1).getName());

            assertEquals(createdHandles, returnedHandles);
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
        }
    }

    @Test
    public void testPullMultipleWithSameTagWithOptions2() throws Exception {
        String groupTag = "testPullMultipleWithSameTagWithOptions2";
        String taskBaseName = groupTag + "_" + getTimeStampRandom();

        TaskHandle th1 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foobar"));
        TaskHandle th2 = queue.add(withMethod(PULL).tag(taskBaseName).payload("foofoo".getBytes()));
        sync();
        try {
            LeaseOptions lo = LeaseOptions.Builder
                .withLeasePeriod(30, TimeUnit.MINUTES).countLimit(100).tag(taskBaseName.getBytes());
            List<TaskHandle> handles = queue.leaseTasks(lo);
            assertEquals(2, handles.size());

            Set<String> createdHandles = new HashSet<>();
            createdHandles.add(th1.getName());
            createdHandles.add(th2.getName());

            Set<String> returnedHandles = new HashSet<>();
            returnedHandles.add(handles.get(0).getName());
            returnedHandles.add(handles.get(1).getName());

            assertEquals(createdHandles, returnedHandles);
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
        }
    }

    @Test
    public void testEtaMillis() {
        String tag = "testEtaMillis_" + getTimeStampRandom();
        queue.add(withMethod(PULL).etaMillis(System.currentTimeMillis() + 10000).tag(tag));
        sync(5000);  // Give tasks a chance to become available.

        List<TaskHandle> tasks = queue.leaseTasks(LeaseOptions.Builder.withTag(tag).leasePeriod(1, TimeUnit.SECONDS).countLimit(1));
        assertEquals(0, tasks.size());

        sync(10000);

        tasks = queue.leaseTasks(LeaseOptions.Builder.withTag(tag.getBytes()).leasePeriod(1, TimeUnit.SECONDS).countLimit(1));
        assertEquals(1, tasks.size());

        queue.deleteTask(tasks);
    }

    @Test
    public void testCountdownMillis() {
        String tag = "testCountdownMillis_" + getTimeStampRandom();
        queue.add(withMethod(PULL).countdownMillis(10000).tag(tag));
        sync(5000);  // Give tasks a chance to become available.

        List<TaskHandle> tasks = queue.leaseTasks(LeaseOptions.Builder.withTag(tag.getBytes()).leasePeriod(1, TimeUnit.SECONDS).countLimit(1));
        assertEquals(0, tasks.size());

        sync(10000);

        tasks = queue.leaseTasks(LeaseOptions.Builder.withTag(tag).leasePeriod(1, TimeUnit.SECONDS).countLimit(1));
        assertEquals(1, tasks.size());

        queue.deleteTask(tasks);
    }

    @Test(expected = TaskAlreadyExistsException.class)
    public void testAddingTwoTasksWithSameNameThrowsException() {
        String taskName = "sameName_" + getTimeStampRandom();
        queue.add(withMethod(PULL).taskName(taskName));
        queue.add(withMethod(PULL).taskName(taskName));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddingTwoTasksWithSameNameInSingleRequestThrowsException() {
        String taskName = "sameName_" + getTimeStampRandom();
        queue.add(
            Arrays.asList(
                withMethod(PULL).taskName(taskName),
                withMethod(PULL).taskName(taskName)));
    }

    @Test
    public void testDeadlineInSeconds() {
        queue.add(withMethod(PULL));
        // TODO - what does this deadline actually do?
        List<TaskHandle> tasks = queue.leaseTasks(LeaseOptions.Builder.withCountLimit(1).leasePeriod(2, TimeUnit.MINUTES).deadlineInSeconds(5.0));
        queue.deleteTask(tasks);
    }

    /**
     * Get random string to make taskname unique.  Using timestamp alone seems like it is not enough
     * so add some randomness at the end.
     *
     * @return timestamp plus random number
     */
    private String getTimeStampRandom() {
        int num = (int) (Math.random() * 1000000);
        String rand = Integer.toString(num);
        return timeStamp + "_" + rand;
    }

    private List<TaskHandle> leaseTasksByOptions(String groupTag, int count, boolean zeroSizeAcceptable, LeaseOptions options) {
        int retryCount = 10;
        int retryInterval = 2000;
        return leaseTasksByOptions(groupTag, count, zeroSizeAcceptable, options, retryCount, retryInterval);
    }

    /**
     * @return List<TaskHandle> the accumulated list of tasks up to count.
     */
    private List<TaskHandle> leaseTasksByOptions(String groupTag, int count,
                                                 boolean zeroSizeAcceptable, LeaseOptions options,
                                                 int retry, int interval) {
        List<TaskHandle> handleList = null;
        List<TaskHandle> masterHandleList = new ArrayList<>();

        int retryCount = retry;
        int retryCounter = 0;
        int retryInterval = interval;
        while (masterHandleList.size() < count) {
            sync(retryInterval);  // first iteration gives time for tasks to activate.
            try {
                handleList = queue.leaseTasks(options);
            } catch (TransientFailureException tfe) {  // This is common.
                sync(retryInterval);
                log.warning(tfe.toString());
                handleList = null;
                continue;
            }

            if (handleList.size() > 0) {
                masterHandleList.addAll(handleList);
            }

            if (handleList.size() >= 0 && zeroSizeAcceptable) {
                return masterHandleList;  // even zero tasks okay, return what we got.
            }

            if (masterHandleList.size() >= count) {
                return masterHandleList;  // Success, got all tasks requested.
            }

            if (retryCounter++ > retryCount) {
                break;
            }
        }

        String errMsg = "Couldn't lease " + Integer.toString(count) + " tag:" +
            groupTag + " after " + retryCount + " attempts.";
        log.warning(errMsg);
        if (handleList == null) {  // Couldn't communicate with Task service.
            throw new TransientFailureException(errMsg);
        }

        return masterHandleList;  // Return what we've got, could be partial.
    }

    /**
     * Try to make leasing tasks less flaky by doing retries.
     *
     * @param groupTag           group tag to filter by
     * @param count              number of tasks to lease.
     * @param zeroSizeAcceptable when an empty list is okay or expected.
     * @return List<TaskHandle>
     */
    private List<TaskHandle> leaseTasksByTag60Secs(String groupTag, int count,
                                                   boolean zeroSizeAcceptable) {
        LeaseOptions options = LeaseOptions.Builder
            .withTag(groupTag)
            .countLimit(count)  // Max lease count is 1000.
            .leasePeriod(60, TimeUnit.SECONDS);
        return leaseTasksByOptions(groupTag, count, zeroSizeAcceptable, options);

    }

    /**
     * @param count        Number of tasks to create.
     * @param taskBaseName add count to this name to create task name.
     * @param groupTag     will be used during tearDown to delete tasks.
     * @param payload      to be used by task.
     * @return List of TaskHandles from queue.add()
     */
    private List<TaskHandle> addTasks(int count, String taskBaseName,
                                      String groupTag, String payload) {
        ArrayList<TaskOptions> optionList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TaskOptions options =
                withMethod(TaskOptions.Method.PULL)
                    .taskName(taskBaseName + "_" + i)
                    .tag(groupTag)
                    .payload(payload);
            optionList.add(options);
        }
        taskTags.add(groupTag);
        List<TaskHandle> taskHandles = queue.add(optionList);
        sync(5000);  // Give tasks a chance to become available.

        return taskHandles;
    }

    /**
     * @param taskName name of the task to delete.
     */
    private void deleteTaskByName(String taskName) {
        queue.deleteTask(taskName);
    }

    /**
     * @param taskHandles list of handles of tasks to delete.
     */
    private void deleteMultipleTasks(List<TaskHandle> taskHandles) {
        queue.deleteTask(taskHandles);
    }
}

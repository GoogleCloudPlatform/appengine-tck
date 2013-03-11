package com.google.appengine.tck.taskqueue;

import static com.google.appengine.api.taskqueue.TaskOptions.Method.PULL;
import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING_PULL;
import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING_REMOTE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.InvalidQueueModeException;
import com.google.appengine.api.taskqueue.LeaseOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueConstants;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TransientFailureException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Pull Queue Test.
 *
 * @author terryok@google.com (Terry Okamoto)
 * @author mluksa@redhat.com (Marko Luksa)
 */
@RunWith(Arquillian.class)
public class PullQueueTest extends TaskqueueTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final int MAX_LEASE_COUNT = 1000;
  private Queue queue;
  private String timeStamp;  // make task names unique for test run.
  private String payload;

  // Create tasks with addTasks() tracking.  They will be deleted
  // during tearDown().
  private final List<String> taskTags = new ArrayList<String>();
  private final List<String> deleteOnTearDownTags = new ArrayList<String>();

  private static final Logger log =
      Logger.getLogger(PullQueueTest.class.getName());

  @Before
  public void setUp() {
    queue = QueueFactory.getQueue(E2E_TESTING_PULL);
    queue.purge();
    sleep(2000);  // Give tasks a chance to become available.

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
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPullTaskWithUrlIsNotAllowed() {
    queue.add(TaskOptions.Builder.withMethod(PULL).payload("payload").url("someUrl"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPullTaskWithHeaderIsNotAllowed() {
    queue.add(TaskOptions.Builder.withMethod(PULL).payload("payload").header("someHeader", "foo"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPullTaskWithRetryOptionsIsNotAllowed() {
    queue.add(TaskOptions.Builder.withMethod(PULL).payload("payload").retryOptions(RetryOptions.Builder.withTaskRetryLimit(1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPullTaskCannotHaveBothPayloadAndParams() {
    queue.add(TaskOptions.Builder.withMethod(PULL).payload("payload").param("someParam", "foo"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTransactionalTasksMustBeNameless() {
    Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
    try {
      QueueFactory.getDefaultQueue().add(tx, TaskOptions.Builder.withTaskName("foo"));
    } finally {
      tx.rollback();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeEtaMillis() {
    queue.add(TaskOptions.Builder.withMethod(PULL).etaMillis(-1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEtaMillisTooFarInFuture() {
    queue.add(TaskOptions.Builder.withMethod(PULL)
        .etaMillis(System.currentTimeMillis() + QueueConstants.getMaxEtaDeltaMillis() + 1000));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeCountdownMillis() {
    queue.add(TaskOptions.Builder.withMethod(PULL).countdownMillis(-1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCountdownMillisTooLarge() {
    queue.add(TaskOptions.Builder.withMethod(PULL)
        .countdownMillis(QueueConstants.getMaxEtaDeltaMillis() + 1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEtaMillisAndCountdownMillisAreExclusive() {
    queue.add(TaskOptions.Builder.withMethod(PULL)
        .etaMillis(System.currentTimeMillis() + 1000)
        .countdownMillis(1000));
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

  @Test
  public void testLeaseFromNonPullQueue() {
    Queue remoteQueue = QueueFactory.getQueue(E2E_TESTING_REMOTE);
    thrown.expect(InvalidQueueModeException.class);
    remoteQueue.leaseTasks(1, TimeUnit.MILLISECONDS, 1);
  }

  @Test
  public void testLeaseNonExistQueue() {
    Queue nonExistQueue = QueueFactory.getQueue("nonExistQueue");
    thrown.expect(IllegalStateException.class);
    nonExistQueue.leaseTasks(1, TimeUnit.MILLISECONDS, 1);
  }

  @Test
  public void testNegativeLeasePeriod() {
    thrown.expect(IllegalArgumentException.class);
    queue.leaseTasks(-1, TimeUnit.MILLISECONDS, 1);

    thrown.expect(IllegalArgumentException.class);
    queue.leaseTasks(-1, TimeUnit.MICROSECONDS, 1);

    thrown.expect(IllegalArgumentException.class);
    queue.leaseTasks(-1, TimeUnit.NANOSECONDS, 1);
  }

  @Test
  public void testNegativeCountLimit() {
    thrown.expect(IllegalArgumentException.class);
    queue.leaseTasks(1, TimeUnit.MILLISECONDS, -1);

    thrown.expect(IllegalArgumentException.class);
    queue.leaseTasks(1, TimeUnit.MICROSECONDS, -1);

    thrown.expect(IllegalArgumentException.class);
    queue.leaseTasks(1, TimeUnit.NANOSECONDS, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLeaseWithoutLeasePeriod() {
    queue.leaseTasks(LeaseOptions.Builder.withCountLimit(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLeaseWithoutCountLimit() {
    queue.leaseTasks(LeaseOptions.Builder.withLeasePeriod(1, TimeUnit.SECONDS));
  }

  @Test
  public void testDeleteNonExist() {
    Queue nonExistQueue = QueueFactory.getQueue("nonExistQueue");
    thrown.expect(IllegalStateException.class);
    nonExistQueue.deleteTask("nonexist");
  }

  @Test
  public void testDelete() {
    String groupTag = "testDelete";
    String taskBaseName = groupTag + "_" + getTimeStampRandom();

    // delete non exist task
    assertEquals("Deleting non-existing task.", false,
        queue.deleteTask("nonexisttask"));

    // delete tasks by list of taskHandles
    int count = 2;
    addTasks(count, taskBaseName, groupTag, payload);
    List<TaskHandle> handleList = leaseTasksByTag60Secs(groupTag, count, false);
    assertEquals(count, handleList.size());
    queue.deleteTask(handleList);
    sleep(7000);  // Needs extra time to delete.
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
    sleep(7000);  // Needs extra time to delete.
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

    List<TaskHandle> tasks = leaseTasksByOptions(uniqueGroupTag, count, false,
        options);
    assertEquals(1, tasks.size());

    // reset lease to 60 seconds.
    queue.modifyTaskLease(tasks.get(0), 30, TimeUnit.SECONDS);

    sleep(leaseDuration + 10000);  // wait for lease to expire...
    List<TaskHandle> tasksAfterExpire = queue.leaseTasks(options);

    // ...but it shouldn't expire since lease time was extended.
    assertEquals(0, tasksAfterExpire.size());

    deleteMultipleTasks(tasks);
  }

  @Test
  public void testModifyNonLeasedTask() {
    String groupTag = "testModifyNonLeasedTask";
    String taskBaseName = groupTag + "_" + getTimeStampRandom();
    deleteOnTearDownTags.add(taskBaseName);

    int count = 1;
    List<TaskHandle> taskList = addTasks(count, taskBaseName, taskBaseName,
        payload);
    thrown.expect(IllegalStateException.class);
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

    sleep(leaseDuration + 1000); // wait for lease to expire
    List<TaskHandle> tasksAfterExpire = queue.leaseTasks(options);

    // expired, so it should be available for lease.
    assertEquals(count, tasksAfterExpire.size());

    deleteMultipleTasks(tasksAfterExpire);
  }

  @Test
  public void testBadLeaseOptions() {
    thrown.expect(IllegalArgumentException.class);
    LeaseOptions option = LeaseOptions.Builder.withCountLimit(-1);

    thrown.expect(IllegalArgumentException.class);
    LeaseOptions option2 = LeaseOptions.Builder.withDeadlineInSeconds((double) -1);
  }

  @Test
  public void testLeaseTasksByTagBytes() {
    String groupTag = "testLeaseTasksByTagBytes";
    String taskBaseName = groupTag + "_" + getTimeStampRandom();
    taskTags.add(taskBaseName);
    byte[] tagBytes = taskBaseName.getBytes();

    TaskOptions options = TaskOptions.Builder
        .withMethod(TaskOptions.Method.PULL)
        .taskName(taskBaseName + "_0")
        .tag(tagBytes)
        .payload("");
    queue.add(options);
    sleep(5000);  // Give tasks a chance to become available.
    List<TaskHandle> tasks = queue.leaseTasksByTagBytes(1, TimeUnit.SECONDS, 10, tagBytes);
    assertEquals(1, tasks.size());
    do {
      queue.purge();
      sleep(2000);
      tasks = queue.leaseTasksByTagBytes(1, TimeUnit.SECONDS, 10, tagBytes);
    } while (!tasks.isEmpty());
  }

  @Test
  public void testEtaMillis() {
    String tag = "testEtaMillis_" + getTimeStampRandom();
    queue.add(TaskOptions.Builder.withMethod(PULL).etaMillis(System.currentTimeMillis() + 15000).tag(tag));
    sleep(5000);  // Give tasks a chance to become available.

    List<TaskHandle> tasks = queue.leaseTasks(LeaseOptions.Builder.withTag(tag).leasePeriod(1, TimeUnit.SECONDS).countLimit(1));
    assertEquals(0, tasks.size());

    sleep(10000);

    tasks = queue.leaseTasks(LeaseOptions.Builder.withTag(tag).leasePeriod(1, TimeUnit.SECONDS).countLimit(1));
    assertEquals(1, tasks.size());

    queue.deleteTask(tasks);
  }

  @Test
  public void testCountdownMillis() {
    String tag = "testCountdownMillis_" + getTimeStampRandom();
    queue.add(TaskOptions.Builder.withMethod(PULL).countdownMillis(15000).tag(tag));
    sleep(5000);  // Give tasks a chance to become available.

    List<TaskHandle> tasks = queue.leaseTasks(LeaseOptions.Builder.withTag(tag).leasePeriod(1, TimeUnit.SECONDS).countLimit(1));
    assertEquals(0, tasks.size());

    sleep(15000);

    tasks = queue.leaseTasks(LeaseOptions.Builder.withTag(tag).leasePeriod(1, TimeUnit.SECONDS).countLimit(1));
    assertEquals(1, tasks.size());

    queue.deleteTask(tasks);
  }

  private void sleep(long milliSecs) {
    try {
      Thread.sleep(milliSecs);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get random string LeaseExpto make taskname unique.  Using timestamp alone seems like it is not enough
   * so add some randomness at the end.
   *
   * @return timestamp plus random number
   */
  private String getTimeStampRandom() {
    int num = (int) (Math.random() * 1000000);
    String rand = Integer.toString(num);
    return timeStamp + "_" + rand;
  }

  private List<TaskHandle> leaseTasksByOptions(String groupTag, int count,
                                               boolean zeroSizeAcceptable, LeaseOptions options) {
    int retryCount = 10;
    int retryInterval = 2000;
    List<TaskHandle> handleList = leaseTasksByOptions(groupTag, count, zeroSizeAcceptable,
        options, retryCount, retryInterval);
    return handleList;
  }
  
  /**
   * @return List<TaskHandle> the accumulated list of tasks up to count.
   */
  private List<TaskHandle> leaseTasksByOptions(String groupTag, int count,
                                               boolean zeroSizeAcceptable, LeaseOptions options,
                                               int retry, int interval) {
    List<TaskHandle> handleList = null;
    List<TaskHandle> masterHandleList = new ArrayList<TaskHandle>();

    int retryCount = retry;
    int retryCounter = 0;
    int retryInterval = interval;
    while (masterHandleList.size() < count) {
      sleep(retryInterval);  // first iteration gives time for tasks to activate.
      try {
        handleList = queue.leaseTasks(options);
      } catch (TransientFailureException tfe) {  // This is common.
        sleep(retryInterval);
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
    ArrayList<TaskOptions> optionList = new ArrayList<TaskOptions>();
    for (int i = 0; i < count; i++) {
      TaskOptions options = TaskOptions.Builder
          .withMethod(TaskOptions.Method.PULL)
          .taskName(taskBaseName + "_" + i)
          .tag(groupTag)
          .payload(payload);
      optionList.add(options);
    }
    taskTags.add(groupTag);
    List<TaskHandle> taskHandles = queue.add(optionList);
    sleep(5000);  // Give tasks a chance to become available.

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

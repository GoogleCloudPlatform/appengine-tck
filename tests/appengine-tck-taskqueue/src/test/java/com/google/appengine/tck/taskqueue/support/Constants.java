package com.google.appengine.tck.taskqueue.support;

/**
 * Common properties used in task queues tests.
 */
public class Constants {
  private Constants() { }

  // Queue names
  public static final String E2E_TESTING_EXEC = "exec";
  public static final String E2E_TESTING = "E2E-Testing";
  public static final String E2E_TESTING_DEFERRED = "E2E-Testing-Deferred";
  public static final String E2E_TESTING_RETRY = "E2E-Testing-Retry";
  public static final String E2E_TESTING_PULL = "E2E-Testing-Pull";
  public static final String E2E_TESTING_REMOTE = "E2E-Testing-Remote";

  // Entities created for verification
  public static final String ENTITY_TASK_QUEUE_TEST = "TaskQueueTest";
  public static final String ENTITY_DEFERRED_TEST = "DeferredTest";

  // Entity properties
  public static final String TEST_RUN_ID = "test_run_id";
  public static final String TEST_METHOD_TAG = "test_method_tag";
}

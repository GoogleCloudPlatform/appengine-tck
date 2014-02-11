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

/**
 * Common properties used in task queues tests.
 */
public class Constants {
    private Constants() {
    }

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
    public static final String EXECUTED_AT = "executed_at";

    // Markers
    public static final String MARKER = "_marker_";
}

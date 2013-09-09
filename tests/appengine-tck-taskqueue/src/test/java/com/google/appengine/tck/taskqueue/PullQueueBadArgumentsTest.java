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

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.LeaseOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueConstants;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withMethod;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.PULL;
import static com.google.appengine.tck.taskqueue.support.Constants.E2E_TESTING_PULL;

/**
 * Test if Pull Queue throws IllegalArgumentException when necessary.
 *
 * @author terryok@google.com (Terry Okamoto)
 * @author mluksa@redhat.com (Marko Luksa)
 */
@RunWith(Arquillian.class)
public class PullQueueBadArgumentsTest extends QueueTestBase {

    private Queue queue;

    @Before
    public void setUp() {
        queue = QueueFactory.getQueue(E2E_TESTING_PULL);
        purgeAndPause(queue);
    }

    @After
    public void tearDown() {
        purgeAndPause(queue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPullTaskWithUrlIsNotAllowed() {
        queue.add(withMethod(PULL).payload("payload").url("someUrl"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPullTaskWithHeaderIsNotAllowed() {
        queue.add(withMethod(PULL).payload("payload").header("someHeader", "foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPullTaskWithHeadersIsNotAllowed() {
        queue.add(withMethod(PULL).payload("payload").headers(Collections.singletonMap("someHeader", "foo")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPullTaskWithRetryOptionsIsNotAllowed() {
        queue.add(withMethod(PULL).payload("payload").retryOptions(RetryOptions.Builder.withTaskRetryLimit(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPullTaskCannotHaveBothPayloadAndParams() {
        queue.add(withMethod(PULL).payload("payload").param("someParam", "foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransactionalTasksMustBeNameless() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        try {
            queue.add(tx, withMethod(PULL).taskName("foo"));
        } finally {
            tx.rollback();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeEtaMillis() {
        queue.add(withMethod(PULL).etaMillis(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEtaMillisTooFarInFuture() {
        queue.add(withMethod(PULL)
            .etaMillis(System.currentTimeMillis() + QueueConstants.getMaxEtaDeltaMillis() + 1000));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCountdownMillis() {
        queue.add(withMethod(PULL).countdownMillis(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCountdownMillisTooLarge() {
        queue.add(withMethod(PULL)
            .countdownMillis(QueueConstants.getMaxEtaDeltaMillis() + 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEtaMillisAndCountdownMillisAreExclusive() {
        queue.add(withMethod(PULL)
            .etaMillis(System.currentTimeMillis() + 1000)
            .countdownMillis(1000));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeLeasePeriod() {
        queue.leaseTasks(-1, TimeUnit.MILLISECONDS, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCountLimit() {
        queue.leaseTasks(1, TimeUnit.MILLISECONDS, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeaseWithoutLeasePeriod() {
        queue.leaseTasks(LeaseOptions.Builder.withCountLimit(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeaseWithoutCountLimit() {
        queue.leaseTasks(LeaseOptions.Builder.withLeasePeriod(1, TimeUnit.SECONDS));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testLeaseOptionsWithNegativeCountLimit() {
        LeaseOptions.Builder.withCountLimit(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeaseOptionsWithNegativeDeadlineInSeconds() {
        LeaseOptions.Builder.withDeadlineInSeconds((double) -1);
    }

}

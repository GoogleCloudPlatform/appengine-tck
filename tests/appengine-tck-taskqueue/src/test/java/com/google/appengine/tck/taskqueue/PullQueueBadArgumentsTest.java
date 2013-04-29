package com.google.appengine.tck.taskqueue;

import java.util.concurrent.TimeUnit;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.LeaseOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueConstants;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import org.jboss.arquillian.junit.Arquillian;
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

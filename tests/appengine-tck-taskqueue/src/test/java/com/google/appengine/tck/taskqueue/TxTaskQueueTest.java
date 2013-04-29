package com.google.appengine.tck.taskqueue;

import java.util.Collections;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tx queue tests.
 *
 * @author ales.justin@jboss.org (Ales Justin)
 */
@RunWith(Arquillian.class)
public class TxTaskQueueTest extends QueueTestBase {
    private Queue getDefaultQueue() {
        return QueueFactory.getDefaultQueue();
    }

    @Test
    public void testNoTaskSingle() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        try {
            getDefaultQueue().add(tx, TaskOptions.Builder.withDefaults());
        } finally {
            tx.rollback();
        }
        Assert.assertEquals(0, getDefaultQueue().fetchStatistics().getNumTasks());
    }

    @Test
    public void testNoTaskIterable() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        try {
            getDefaultQueue().add(tx, Collections.singleton(TaskOptions.Builder.withDefaults()));
        } finally {
            tx.rollback();
        }
        Assert.assertEquals(0, getDefaultQueue().fetchStatistics().getNumTasks());
    }

    @Test
    public void testNoTaskSingleAsync() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        try {
            waitOnFuture(getDefaultQueue().addAsync(tx, TaskOptions.Builder.withDefaults()));
        } finally {
            tx.rollback();
        }
        Assert.assertEquals(0, waitOnFuture(getDefaultQueue().fetchStatisticsAsync(2013.0)).getNumTasks());
    }

    @Test
    public void testNoTaskIterableAsync() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        try {
            waitOnFuture(getDefaultQueue().addAsync(tx, Collections.singleton(TaskOptions.Builder.withDefaults())));
        } finally {
            tx.rollback();
        }
        Assert.assertEquals(0, waitOnFuture(getDefaultQueue().fetchStatisticsAsync(2013.0)).getNumTasks());
    }
}

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

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tx queue tests.
 *
 * @author ales.justin@jboss.org (Ales Justin)
 * @author terryok@google.com (Terry Okamoto)
 *
 * Taskqueue statistics are random for the dev_appserver/SDK so we don't bother asserting for that case.
 */
@RunWith(Arquillian.class)
public class TxTaskQueueTest extends QueueTestBase {
    private Queue getDefaultQueue() {
        return QueueFactory.getDefaultQueue();
    }

    @Before
    public void setUp() {
        purgeAndPause(QueueFactory.getDefaultQueue());
    }

    @After
    public void tearDown() {
        purgeAndPause(QueueFactory.getDefaultQueue());
    }

    @Test
    public void testNoTaskSingle() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        final int beforeNumTasks = getDefaultQueue().fetchStatistics().getNumTasks();
        try {
            getDefaultQueue().add(tx, TaskOptions.Builder.withDefaults());
        } finally {
            tx.rollback();
        }

        if (doIgnore("testNoTaskSingle")) {
            return;
        }

        sync(10000);  // Wait for statistics servers to refresh.
        Assert.assertEquals(beforeNumTasks, getDefaultQueue().fetchStatistics().getNumTasks());
    }

    @Test
    public void testNoTaskIterable() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        final int beforeNumTasks = getDefaultQueue().fetchStatistics().getNumTasks();
        try {
            getDefaultQueue().add(tx, Collections.singleton(TaskOptions.Builder.withDefaults()));
        } finally {
            tx.rollback();
        }

        if (doIgnore("testNoTaskIterable")) {
            return;
        }

        sync(10000);  // Wait for statistics servers to refresh.
        Assert.assertEquals(beforeNumTasks, getDefaultQueue().fetchStatistics().getNumTasks());
    }

    @Test
    public void testNoTaskSingleAsync() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        final int beforeNumTasks = getDefaultQueue().fetchStatistics().getNumTasks();
        try {
            waitOnFuture(getDefaultQueue().addAsync(tx, TaskOptions.Builder.withDefaults()));
        } finally {
            tx.rollback();
        }

        if (doIgnore("testNoTaskSingleAsync")) {
            return;
        }

        sync(10000);
        Assert.assertEquals(beforeNumTasks, waitOnFuture(getDefaultQueue().fetchStatisticsAsync(2013.0)).getNumTasks());
    }

    @Test
    public void testNoTaskIterableAsync() {
        Transaction tx = DatastoreServiceFactory.getDatastoreService().beginTransaction();
        final int beforeNumTasks = getDefaultQueue().fetchStatistics().getNumTasks();
        try {
            waitOnFuture(getDefaultQueue().addAsync(tx, Collections.singleton(TaskOptions.Builder.withDefaults())));
        } finally {
            tx.rollback();
        }

        if (doIgnore("testNoTaskIterableAsync")) {
            return;
        }

        sync(10000);
        Assert.assertEquals(beforeNumTasks, waitOnFuture(getDefaultQueue().fetchStatisticsAsync(2013.0)).getNumTasks());
    }
}

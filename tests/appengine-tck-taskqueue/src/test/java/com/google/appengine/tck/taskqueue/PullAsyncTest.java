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

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.taskqueue.LeaseOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withMethod;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.PULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class PullAsyncTest extends QueueTestBase {
    @Test
    public void testPullParams() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th = queue.add(withMethod(PULL).param("foo", "bar").etaMillis(15000));
        try {
            List<TaskHandle> handles = waitOnFuture(queue.leaseTasksAsync(30, TimeUnit.MINUTES, 100));
            assertFalse(handles.isEmpty());
            TaskHandle lh = handles.get(0);
            assertEquals(th.getName(), lh.getName());
        } finally {
            queue.deleteTask(th);
        }
    }

    @Test
    public void testPullPayload() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th = queue.add(withMethod(PULL).payload("foobar").etaMillis(15000));
        try {
            List<TaskHandle> handles = waitOnFuture(queue.leaseTasksAsync(30, TimeUnit.MINUTES, 100));
            assertFalse(handles.isEmpty());
            TaskHandle lh = handles.get(0);
            assertEquals(th.getName(), lh.getName());
        } finally {
            queue.deleteTask(th);
        }
    }

    @Test
    public void testPullWithTag() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th = queue.add(withMethod(PULL).tag("barfoo1").etaMillis(15000));
        try {
            List<TaskHandle> handles = waitOnFuture(queue.leaseTasksByTagAsync(30, TimeUnit.MINUTES, 100, "barfoo1"));
            assertFalse(handles.isEmpty());
            TaskHandle lh = handles.get(0);
            assertEquals(th.getName(), lh.getName());
        } finally {
            queue.deleteTask(th);
        }
    }

    @Test
    public void testPullMultipleWithSameTag() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th1 = queue.add(withMethod(PULL).tag("barfoo2").payload("foobar").etaMillis(15000));
        TaskHandle th2 = queue.add(withMethod(PULL).tag("barfoo2").payload("foofoo").etaMillis(10000));
        try {
            List<TaskHandle> handles = waitOnFuture(queue.leaseTasksByTagAsync(30, TimeUnit.MINUTES, 100, "barfoo2"));
            assertEquals(2, handles.size());
            // order is reversed, due to eta-millis
            assertEquals(th2.getName(), handles.get(0).getName());
            assertEquals(th1.getName(), handles.get(1).getName());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
        }
    }

    @Test
    public void testPullMultipleWithDiffTag() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th1 = queue.add(withMethod(PULL).tag("barfoo3").payload("foobar").etaMillis(15000));
        TaskHandle th2 = queue.add(withMethod(PULL).tag("qwerty").payload("foofoo").etaMillis(10000));
        TaskHandle th3 = queue.add(withMethod(PULL).tag("barfoo3").payload("foofoo").etaMillis(10000));
        try {
            List<TaskHandle> handles = waitOnFuture(queue.leaseTasksByTagAsync(30, TimeUnit.MINUTES, 100, "barfoo3"));
            assertEquals(2, handles.size());
            assertEquals(th3.getName(), handles.get(0).getName());
            assertEquals(th1.getName(), handles.get(1).getName());

            handles = queue.leaseTasksByTag(30, TimeUnit.MINUTES, 100, "qwerty");
            assertEquals(1, handles.size());
            assertEquals(th2.getName(), handles.get(0).getName());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
            queue.deleteTask(th3);
        }
    }

    @Test
    public void testPullWithGroupTag() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th1 = queue.add(withMethod(PULL).tag("barfoo3").payload("foobar").etaMillis(15000));
        TaskHandle th2 = queue.add(withMethod(PULL).tag("qwerty").payload("foofoo").etaMillis(11000));
        TaskHandle th3 = queue.add(withMethod(PULL).tag("barfoo3").payload("foofoo").etaMillis(10000));
        try {
            LeaseOptions options = new LeaseOptions(LeaseOptions.Builder.withLeasePeriod(1000L, TimeUnit.SECONDS)).countLimit(100).groupByTag();
            List<TaskHandle> handles = waitOnFuture(queue.leaseTasksAsync(options));
            assertEquals(2, handles.size());
            assertEquals(th3.getName(), handles.get(0).getName());
            assertEquals(th1.getName(), handles.get(1).getName());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
            queue.deleteTask(th3);
        }
    }

    @Test
    public void testLeaseTasksOnlyReturnsSpecifiedNumberOfTasks() {
        Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th1 = queue.add(withMethod(PULL));
        TaskHandle th2 = queue.add(withMethod(PULL));
        try {
            int countLimit = 1;
            List<TaskHandle> handles = waitOnFuture(queue.leaseTasksAsync(10, TimeUnit.SECONDS, countLimit));
            assertEquals(countLimit, handles.size());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
        }
    }

}

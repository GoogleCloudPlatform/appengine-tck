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

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
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
public class PullTest extends QueueTestBase {

    @Before
    public void setUp() {
        purgeAndPauseByName("pull-queue");
    }

    @After
    public void tearDown() {
        purgeAndPauseByName("pull-queue");
    }

    @Test
    public void testPullParams() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th = queue.add(withMethod(PULL).param("foo", "bar").etaMillis(15000));
        sync();
        try {
            List<TaskHandle> handles = queue.leaseTasks(30, TimeUnit.MINUTES, 100);
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
        TaskHandle th = queue.add(withMethod(PULL).payload("foobar".getBytes()).etaMillis(15000));
        sync();
        try {
            List<TaskHandle> handles = queue.leaseTasks(30, TimeUnit.MINUTES, 100);
            assertFalse(handles.isEmpty());
            TaskHandle lh = handles.get(0);
            assertEquals(th.getName(), lh.getName());
        } finally {
            queue.deleteTask(th);
        }
    }

    @Test
    public void testLeaseTasksOnlyReturnsSpecifiedNumberOfTasks() {
        Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th1 = queue.add(withMethod(PULL));
        TaskHandle th2 = queue.add(withMethod(PULL));
        sync();
        try {
            int countLimit = 1;
            List<TaskHandle> handles = queue.leaseTasks(10, TimeUnit.SECONDS, countLimit);
            assertEquals(countLimit, handles.size());
        } finally {
            queue.deleteTask(th1);
            queue.deleteTask(th2);
        }
    }

}

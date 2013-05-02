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

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.QueueStatistics;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
//@Category(JBoss.class) // should be @All, once GAE local supports stats
public class StatsTest extends QueueTestBase {
    @Test
    public void testStatsAPI() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        QueueStatistics stats = queue.fetchStatistics();
        Assert.assertNotNull(stats);
        Assert.assertEquals("pull-queue", stats.getQueueName());
        // TODO -- more stats checks
    }

//    @Test
//    public void testBasics() throws Exception {
//        final Queue queue = QueueFactory.getQueue("pull-queue");
//        queue.purge();
//        QueueStatistics preStats = queue.fetchStatistics();
//        Assert.assertNotNull(preStats);
//        int currentTaskCount = preStats.getNumTasks();
//        long taskExecuteTime = System.currentTimeMillis() + (15 * 1000);
//        TaskHandle th = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).param("foo", "bar").etaMillis(taskExecuteTime));
//        sync(2000L);
//        try {
//            queue.purge();
//            QueueStatistics postStats = queue.fetchStatistics();
//            Assert.assertNotNull(postStats);
//            Assert.assertEquals(currentTaskCount + 1, postStats.getNumTasks());
//
//            List<TaskHandle> handles = queue.leaseTasks(30, TimeUnit.MINUTES, 100);
//            Assert.assertFalse(handles.isEmpty());
//            TaskHandle lh = handles.get(0);
//            Assert.assertEquals(th.getName(), lh.getName());
//            sync(5000L);
//        } finally {
//            queue.deleteTask(th);
//        }
//    }
}

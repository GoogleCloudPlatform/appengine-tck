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
import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class SmokeTest extends QueueTestBase {
    @Test
    public void testBasics() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        sync(2000L);
        TaskHandle th = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).param("foo", "bar".getBytes()));
        try {
            List<TaskHandle> handles = queue.leaseTasks(30, TimeUnit.MINUTES, 100);
            Assert.assertFalse(handles.isEmpty());
            Assert.assertEquals(1, handles.size());
            TaskHandle lh = handles.get(0);
            Assert.assertEquals(th.getName(), lh.getName());
            sync(5000L);
        } finally {
            queue.deleteTask(th);
        }
    }
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
public class SmokeTest extends TaskqueueTestBase {
    @Test
    public void testBasics() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        TaskHandle th = queue.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).param("foo", "bar").etaMillis(15000));
        try {
            List<TaskHandle> handles = queue.leaseTasks(30, TimeUnit.MINUTES, 100);
            Assert.assertFalse(handles.isEmpty());
            TaskHandle lh = handles.get(0);
            Assert.assertEquals(th.getName(), lh.getName());
            sync(5000L);
        } finally {
            queue.deleteTask(th);
        }
    }
}

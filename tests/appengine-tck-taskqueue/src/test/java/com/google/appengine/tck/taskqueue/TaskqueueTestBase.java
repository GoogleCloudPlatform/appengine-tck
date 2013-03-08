/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.taskqueue.support.DatastoreUtil;
import com.google.appengine.tck.taskqueue.support.DefaultQueueServlet;
import com.google.appengine.tck.taskqueue.support.ExecDeferred;
import com.google.appengine.tck.taskqueue.support.ExecTask;
import com.google.appengine.tck.taskqueue.support.PrintServlet;
import com.google.appengine.tck.taskqueue.support.RequestData;
import com.google.appengine.tck.taskqueue.support.RetryTestServlet;
import com.google.appengine.tck.taskqueue.support.TestQueueServlet;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;


/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public abstract class TaskqueueTestBase extends TestBase {

    @Deployment
    public static Archive getDeployment() {
      TestContext context = new TestContext();
      context.setWebXmlFile("web-taskqueue.xml");
      WebArchive war = getTckDeployment(context);
      war.addClasses(TaskqueueTestBase.class, DatastoreUtil.class);
      war.addClasses(ExecTask.class, ExecDeferred.class);
      war.addClasses(DeferredTest.class, PullQueueTest.class, PullTest.class, SmokeTest.class, StatsTest.class, TaskQueueTest.class, TasksTest.class);
      war.addClass(RequestData.class);
      war.addClass(DefaultQueueServlet.class);
      war.addClass(TestQueueServlet.class);
      war.addClass(PrintServlet.class);
      war.addClass(RetryTestServlet.class);
      war.addAsWebInfResource("queue.xml");
      return war;
    }

}

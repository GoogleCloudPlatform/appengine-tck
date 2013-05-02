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
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class QueueTestBase extends TestBase {
    protected static final String URL = "/_ah/test";
    public static final String TASK_RETRY_COUNT = "X-AppEngine-TaskRetryCount";
    public static final String TASK_EXECUTION_COUNT = "X-AppEngine-TaskExecutionCount";
    public static final String QUEUE_NAME = "X-AppEngine-QueueName";
    public static final String TASK_NAME = "X-AppEngine-TaskName";

    @Deployment
    public static Archive getDeployment() {
        TestContext context = new TestContext();
        context.setWebXmlFile("web-taskqueue.xml");
        WebArchive war = getTckDeployment(context);
        war.addClasses(QueueTestBase.class, DatastoreUtil.class);
        war.addClasses(ExecTask.class, ExecDeferred.class);
        war.addClass(RequestData.class);
        war.addClass(DefaultQueueServlet.class);
        war.addClass(TestQueueServlet.class);
        war.addClass(PrintServlet.class);
        war.addClass(RetryTestServlet.class);
        war.addAsWebInfResource("queue.xml");
        return war;
    }

}

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
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
import org.junit.Before;


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

    private MemcacheService cache;

    @Before
    public void init() {
        cache = MemcacheServiceFactory.getMemcacheService();
    }

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

    protected Set<String> taskHandlesToNameSet(TaskHandle... handles) {
        return taskHandleListToNameSet(Arrays.asList(handles));
    }

    /**
     * Create a Set with task names to compare with a queried set of task handles.
     * Used with taskHandlesToNameSet()
     */
    protected Set<String> taskHandleListToNameSet(List<TaskHandle> handles) {
        Set<String> taskNames = new HashSet<String>();

        for (TaskHandle handle : handles) {
            taskNames.add(handle.getName());
        }
        return taskNames;
    }

    /**
     * @param key         memcache key
     * @param targetValue object value to wait for.
     * @param <T>         memcache object type.
     * @return the current value from memcache (targetValue, not targetValue, or null)
     */
    protected <T> T waitForTestData(String key, T targetValue) {
        int milliSeconds = 0;
        int maxMilliSeconds = 30 * 1000;

        T currentValue = (T) cache.get(key);

        while (currentValue == null && milliSeconds <= maxMilliSeconds) {
            sync(1000);
            milliSeconds += 1000;
            currentValue = (T) cache.get(key);
        }

        if (!cache.contains(key)) {
            log.warning("cache item:" + key + " does not exist.");
            return null;
        }

        if (currentValue == null) {
            log.warning("cache item: " + key + " exists, but is null.");
            return null;
        }

        while (!currentValue.equals(targetValue) && milliSeconds <= maxMilliSeconds) {
            sync(1000);
            milliSeconds += 1000;
            currentValue = (T) cache.get(key);
        }

        return currentValue;
    }

    protected <T> T waitForTestDataToExist(String key) {
        int milliSeconds = 0;
        int maxMilliSeconds = 20 * 1000;

        T currentValue = (T) cache.get(key);

        while (!cache.contains(key) && milliSeconds <= maxMilliSeconds) {
            log.info(key + ": does not exist.");
            sync(1000);
            milliSeconds += 1000;
            currentValue = (T) cache.get(key);
        }

        return currentValue;
    }
}

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.taskqueue.InvalidQueueModeException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;

import org.jboss.arquillian.junit.Arquillian;
import com.google.appengine.tck.taskqueue.support.DefaultQueueServlet;
import com.google.appengine.tck.taskqueue.support.PrintServlet;
import com.google.appengine.tck.taskqueue.support.RequestData;
import com.google.appengine.tck.taskqueue.support.RetryTestServlet;
import com.google.appengine.tck.taskqueue.support.TestQueueServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withHeader;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withMethod;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withPayload;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withTag;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withTaskName;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.DELETE;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.GET;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.HEAD;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.POST;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.PULL;
import static com.google.appengine.api.taskqueue.TaskOptions.Method.PUT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class TasksTest extends TaskqueueTestBase {
    private static final String URL = "/_ah/test";
    public static final String TASK_RETRY_COUNT = "X-AppEngine-TaskRetryCount";
    public static final String TASK_EXECUTION_COUNT = "X-AppEngine-TaskExecutionCount";
    public static final String QUEUE_NAME = "X-AppEngine-QueueName";
    public static final String TASK_NAME = "X-AppEngine-TaskName";

    @Before
    public void setUp() throws Exception {
        DefaultQueueServlet.reset();
        TestQueueServlet.reset();
        RetryTestServlet.reset();
    }

    @After
    public void tearDown() throws Exception {
        PrintServlet.reset();
    }

    @Test
    public void testSmoke() throws Exception {
        final Queue queue = QueueFactory.getQueue("tasks-queue");
        queue.add(withUrl(URL));
        sync();
        assertNotNull(PrintServlet.getLastRequest());
    }

    @Test
    public void testTaskWithoutUrlIsSubmittedToDefaultUrl() throws Exception {
        Queue defaultQueue = QueueFactory.getDefaultQueue();
        defaultQueue.add(withMethod(POST));
        sync();
        assertTrue("DefaultQueueServlet was not invoked", DefaultQueueServlet.wasInvoked());

        Queue testQueue = QueueFactory.getQueue("test");
        testQueue.add(withMethod(POST));
        sync();
        assertTrue("TestQueueServlet was not invoked", TestQueueServlet.wasInvoked());
    }

    @Test
    public void testTaskHandleContainsAllNecessaryProperties() throws Exception {
        Queue queue = QueueFactory.getDefaultQueue();
        TaskHandle handle = queue.add(withTaskName("foo").payload("payload"));

        assertEquals("default", handle.getQueueName());
        assertEquals("foo", handle.getName());
        assertEquals("payload", new String(handle.getPayload(), "UTF-8"));
//        assertEquals(, handle.getEtaMillis());    // TODO
//        assertEquals(, handle.getRetryCount());
    }

    @Test
    public void testTaskHandleContainsAutoGeneratedTaskNameWhenTaskNameNotDefinedInTaskOptions() throws Exception {
        Queue queue = QueueFactory.getDefaultQueue();
        TaskHandle handle = queue.add();
        assertNotNull(handle.getName());
    }

    @Test
    public void testRequestHeaders() throws Exception {
        Queue defaultQueue = QueueFactory.getDefaultQueue();
        defaultQueue.add(withTaskName("task1"));
        sync();

        RequestData request = DefaultQueueServlet.getLastRequest();
        assertEquals("default", request.getHeader(QUEUE_NAME));
        assertEquals("task1", request.getHeader(TASK_NAME));
        assertNotNull(request.getHeader(TASK_RETRY_COUNT));
        assertNotNull(request.getHeader(TASK_EXECUTION_COUNT));
//        assertNotNull(request.getHeader("X-AppEngine-TaskETA"));    // TODO

        Queue testQueue = QueueFactory.getQueue("test");
        testQueue.add(withTaskName("task2"));
        sync();

        request = TestQueueServlet.getLastRequest();
        assertEquals("test", request.getHeader(QUEUE_NAME));
        assertEquals("task2", request.getHeader(TASK_NAME));
    }

    @Test
    public void testAllPushMethodsAreSupported() throws Exception {
        assertServletReceivesCorrectMethod(GET);
        assertServletReceivesCorrectMethod(PUT);
        assertServletReceivesCorrectMethod(HEAD);
        assertServletReceivesCorrectMethod(POST);
        assertServletReceivesCorrectMethod(DELETE);
    }

    private void assertServletReceivesCorrectMethod(TaskOptions.Method method) {
        MethodRequestHandler handler = new MethodRequestHandler();
        PrintServlet.setRequestHandler(handler);

        Queue queue = QueueFactory.getQueue("tasks-queue");
        queue.add(withUrl(URL).method(method));
        sync();

        assertEquals("Servlet received invalid HTTP method.", method.name(), handler.method);
    }

    @Test
    public void testPayload() throws Exception {
        String sentPayload = "payload";

        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(withPayload(sentPayload));
        sync();

        String receivedPayload = new String(DefaultQueueServlet.getLastRequest().getBody(), "UTF-8");
        assertEquals(sentPayload, receivedPayload);
    }

    @Test
    public void testHeaders() throws Exception {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(withHeader("header_key", "header_value"));
        sync();

        RequestData lastRequest = DefaultQueueServlet.getLastRequest();
        assertEquals("header_value", lastRequest.getHeader("header_key"));
    }

    @Test
    public void testParams() throws Exception {
        class ParamHandler implements PrintServlet.RequestHandler {
            private String paramValue;

            public void handleRequest(ServletRequest req) {
                paramValue = req.getParameter("single_value");
            }
        }

        ParamHandler handler = new ParamHandler();
        PrintServlet.setRequestHandler(handler);

        final Queue queue = QueueFactory.getQueue("tasks-queue");
        queue.add(withUrl(URL).param("single_value", "param_value"));
        sync();

        assertEquals("param_value", handler.paramValue);
    }

    @Test
    public void testMultiValueParams() throws Exception {
        class ParamHandler implements PrintServlet.RequestHandler {
            private String[] paramValues;

            public void handleRequest(ServletRequest req) {
                paramValues = req.getParameterValues("multi_value");
            }
        }

        ParamHandler handler = new ParamHandler();
        PrintServlet.setRequestHandler(handler);

        final Queue queue = QueueFactory.getQueue("tasks-queue");
        queue.add(
            withUrl(URL)
                .param("multi_value", "param_value1")
                .param("multi_value", "param_value2"));
        sync();

        assertNotNull(handler.paramValues);
        assertEquals(
            new HashSet<String>(Arrays.asList("param_value1", "param_value2")),
            new HashSet<String>(Arrays.asList(handler.paramValues)));
    }

    @Test
    public void testRetry() throws Exception {
        RetryTestServlet.setNumberOfTimesToFail(1);

        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(withUrl("/_ah/retryTest").retryOptions(RetryOptions.Builder.withTaskRetryLimit(5)));
        sync();

        assertEquals(2, RetryTestServlet.getInvocationCount());

        RequestData request1 = RetryTestServlet.getRequest(0);
        assertEquals("0", request1.getHeader(TASK_RETRY_COUNT));
        assertEquals("0", request1.getHeader(TASK_EXECUTION_COUNT));

        RequestData request2 = RetryTestServlet.getRequest(1);
        assertEquals("1", request2.getHeader(TASK_RETRY_COUNT));
        assertEquals("1", request2.getHeader(TASK_EXECUTION_COUNT));
    }

    @Test
    public void testRetryLimitIsHonored() throws Exception {
        RetryTestServlet.setNumberOfTimesToFail(10);

        Queue queue = QueueFactory.getDefaultQueue();
        TaskHandle handle = queue.add(withUrl("/_ah/retryTest").retryOptions(RetryOptions.Builder.withTaskRetryLimit(2)));
        sync();

        assertEquals(2, RetryTestServlet.getInvocationCount());
    }

    @Test(expected = InvalidQueueModeException.class)
    public void testLeaseTaskFromPushQueueThrowsException() {
        Queue pushQueue = QueueFactory.getDefaultQueue();
        pushQueue.leaseTasks(1000, TimeUnit.SECONDS, 1);
    }

    @Test
    public void testOnlyPullTasksCanBeAddedToPullQueue() {
        Queue pullQueue = QueueFactory.getQueue("pull-queue");
        pullQueue.add(withMethod(PULL));
        assertAddThrowsExceptionForMethod(DELETE, pullQueue);
        assertAddThrowsExceptionForMethod(GET, pullQueue);
        assertAddThrowsExceptionForMethod(HEAD, pullQueue);
        assertAddThrowsExceptionForMethod(PUT, pullQueue);
        assertAddThrowsExceptionForMethod(POST, pullQueue);
    }

    @Test
    public void testPullTasksCannotBeAddedToPushQueue() {
        Queue pushQueue = QueueFactory.getDefaultQueue();
        pushQueue.add(withMethod(DELETE));
        pushQueue.add(withMethod(GET));
        pushQueue.add(withMethod(HEAD));
        pushQueue.add(withMethod(PUT));
        pushQueue.add(withMethod(POST));
        assertAddThrowsExceptionForMethod(PULL, pushQueue);
    }

    @Test
    public void testOnlyPullTasksCanHaveTag() {
        Queue pullQueue = QueueFactory.getQueue("pull-queue");
        pullQueue.add(withMethod(PULL).tag("foo"));

        Queue pushQueue = QueueFactory.getDefaultQueue();
        try {
            pushQueue.add(withTag("foo"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    private void assertAddThrowsExceptionForMethod(TaskOptions.Method method, Queue queue) {
        try {
            queue.add(withMethod(method));
            fail("Expected InvalidQueueModeException");
        } catch (InvalidQueueModeException e) {
            // pass
        }
    }

    private class MethodRequestHandler implements PrintServlet.RequestHandler {
        private String method;

        public void handleRequest(ServletRequest req) {
            method = ((HttpServletRequest) req).getMethod();
        }
    }

}

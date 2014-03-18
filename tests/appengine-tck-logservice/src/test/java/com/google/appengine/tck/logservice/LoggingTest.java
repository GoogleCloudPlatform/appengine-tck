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

package com.google.appengine.tck.logservice;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Marko Luksa
 */
@RunWith(Arquillian.class)
public class LoggingTest extends LoggingTestBase {

    private Logger log;

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment(newTestContext());
    }

    @Before
    public void setUp() throws Exception {
        log = Logger.getLogger(LoggingTest.class.getName());
    }

    @Test
    public void testLogging() {
        String text = "hello_testLogging_" + getTimeStampRandom();
        assertLogDoesntContain(text);

        log.info(text);
        flush(log);

        assertLogContains(text);
    }

    @Test
    public void testLogLinesAlwaysStoredInEmptyNamespace() {
        String text = "something logged while namespace not set to empty. " + getTimeStampRandom();
        assertLogDoesntContain(text);

        NamespaceManager.set("some-namespace");
        try {
            log.info(text);
            flush(log);
        } finally {
            NamespaceManager.set("");
        }

        assertLogContains(text);
    }

    @Test
    public void testLogMessageStartsWithClassAndMethodAndEndsWithNewLine() {
        String methodName = "testLogMessageStartsWithClassAndMethodAndEndsWithNewLine";
        String logMark = getTimeStampRandom();
        String msg = "logged message " + logMark;

        Logger fooLogger = Logger.getLogger("fooLogger");   // logger name is ignored completely (!?)
        fooLogger.info(msg);
        flush(fooLogger);
        sync(7000);

        int retryMax = 1;
        AppLogLine appLogLine = findLogLineContaining(msg, retryMax);

        String expectedMessage = getClass().getName() + " " + methodName + ": " + msg + "\n";
        assertEquals(expectedMessage, appLogLine.getLogMessage());
    }

    @Test
    public void testLogMessageIsFormatted() {
        // GAE dev server doesn't handle this properly (see http://code.google.com/p/googleappengine/issues/detail?id=8666)

        String logMark = getTimeStampRandom();
        String logMsg = "Parameterized message with params {0} and {1}";
        String logExpect = "Parameterized message with params param1 and " + logMark;
        log.log(Level.INFO, logMsg, new Object[] {"param1", logMark});
        flush(log);
        sync(7000);

        int retryMax = 1;
        AppLogLine logLine = findLogLineContaining(logMark, retryMax);
        assertNotNull("log with " + logMark + " should exist.", logLine);
        assertTrue("logLine should contain: " + logExpect + "but is: " + logLine,
            logLine.toString().contains(logExpect));
    }

    @Test
    public void testLogLineTimeUsec() {
        String logMark = getTimeStampRandom();

        long beforeTimeUsec = System.currentTimeMillis() * 1000;
        log.log(Level.INFO, "testLogLineTimeUsec " + logMark);
        long afterTimeUsec = (System.currentTimeMillis() + 1) * 1000;
        flush(log);
        sync(7000);

        int retryMax = 1;
        AppLogLine logLine = findLogLineContaining(logMark, retryMax);
        assertNotNull("log with " + logMark + " should exist.", logLine);

        assertTrue("Expected logLine.getTimeUsec (" + logLine.getTimeUsec() + ") >= beforeTimeUsec (" + beforeTimeUsec + "), but it was not", logLine.getTimeUsec() >= beforeTimeUsec);
        assertTrue("Expected logLine.getTimeUsec (" + logLine.getTimeUsec() + ") <= afterTimeUsec (" + afterTimeUsec + "), but it was not", logLine.getTimeUsec() <= beforeTimeUsec);
    }

    @Test
    public void testLogLinesAreReturnedInSameOrderAsTheyAreLogged() {
        String logMark = getTimeStampRandom();
        String msg1 = "msg 1 " + logMark;
        String msg2 = "msg 2 " + logMark;
        log.log(Level.INFO, msg1);
        log.log(Level.INFO, msg2);
        flush(log);
        sync(15000);

        RequestLogs requestLogs = getCurrentRequestLogs();
        Integer msg1Index = null;
        Integer msg2Index = null;
        int i = 0;
        for (AppLogLine appLogLine : requestLogs.getAppLogLines()) {
            if (appLogLine.getLogMessage().contains(msg1)) {
                msg1Index = i;
            } else if (appLogLine.getLogMessage().contains(msg2)) {
                msg2Index = i;
            }
            i++;
        }
        assertNotNull("1st log message not found in appLogLines", msg1Index);
        assertNotNull("2nd log message not found in appLogLines", msg2Index);
        assertTrue("Expected first logged message to come before second logged message", msg1Index < msg2Index);
    }

    public RequestLogs getCurrentRequestLogs() {
        LogQuery logQuery = new LogQuery()
            .includeAppLogs(true)
            .includeIncomplete(true)
            .startTimeMillis(System.currentTimeMillis() - 20000);
        for (RequestLogs requestLogs : LogServiceFactory.getLogService().fetch(logQuery)) {
            if (requestLogs.getRequestId().equals(getCurrentRequestId())) {
                return requestLogs;
            }
        }
        fail("Could not find RequestLogs for current request");
        return null;

//        not sure, why the following code throws LogServiceException: An error occurred retrieving logs from storage.
//        LogQuery logQuery = new LogQuery()
//            .includeAppLogs(true)
//            .requestIds(Collections.singletonList(getCurrentRequestId()));
//        Iterable<RequestLogs> iterable = LogServiceFactory.getLogService().fetch(logQuery);
//        assertTrue("Could not find RequestLogs for current request", iterable.iterator().hasNext());
//        return iterable.iterator().next();
    }
}

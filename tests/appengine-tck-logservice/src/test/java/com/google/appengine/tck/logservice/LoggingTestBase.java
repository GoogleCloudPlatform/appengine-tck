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

import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Logger;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.event.TestLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycles;
import com.google.apphosting.api.ApiProxy;
import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.Decoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BaseNCodec;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Ales Justin
 * @author Marko Luksa
 */
public abstract class LoggingTestBase extends TestBase {

    private boolean clearLogAfterEachTestMethod;

    public LoggingTestBase() {
        this(true);
    }

    public LoggingTestBase(boolean clearLogAfterEachTestMethod) {
        this.clearLogAfterEachTestMethod = clearLogAfterEachTestMethod;
    }

    protected static TestContext newTestContext() {
        return new TestContext();
    }

    protected static WebArchive getDefaultDeployment(TestContext context) {
        context.setAppEngineWebXmlFile("appengine-web-with-logging-properties.xml");
        WebArchive war = getTckDeployment(context);
        war.addClasses(LoggingTestBase.class, TestBase.class)
            // classes for Base64.isBase64()
            .addClasses(Base64.class, BaseNCodec.class)
            .addClasses(BinaryEncoder.class, Encoder.class)
            .addClasses(BinaryDecoder.class, Decoder.class)
            .addClasses(EncoderException.class, DecoderException.class)
            .addAsWebInfResource("currentTimeUsec.jsp")
            .addAsWebInfResource("doNothing.jsp")
            .addAsWebInfResource("storeTestData.jsp")
            .addAsWebInfResource("throwException.jsp")
            .addAsWebInfResource("log4j-test.properties")
            .addAsWebInfResource("logging-all.properties");
        return war;
    }

    @Before
    public void before() {
        if (clearLogAfterEachTestMethod && isInContainer()) {
            clear();
        }
    }

    @After
    public void after() {
        if (clearLogAfterEachTestMethod && isInContainer()) {
            clear();
        }
    }

    protected static void clear() {
        LogService service = LogServiceFactory.getLogService();
        TestLifecycleEvent event = TestLifecycles.createServiceLifecycleEvent(LoggingTestBase.class, service);
        TestLifecycles.after(event);
    }

    protected void flush(Logger log) {
        ApiProxy.flushLogs();
        for (Handler handler : log.getHandlers()) {
            handler.flush();
        }
    }

    protected void pause(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    protected boolean logContains(String text, int retryMax) {
        for (int i = 0; i <= retryMax; i++) {
            if (findLogLineContaining(text, retryMax) != null) {
                return true;
            }
            pause(1500);
        }
        return false;
    }

    protected AppLogLine findLogLineContaining(String text, int retryMax) {
        LogQuery logQuery = new LogQuery()
            .includeAppLogs(true)
            .includeIncomplete(true)
                // Not specifying start time causes test to time out since it searches
                // all the logs.
            .startTimeMillis(System.currentTimeMillis() - (20 * 1000));
        return findLogLine(text, logQuery, retryMax);
    }

    protected Iterator<RequestLogs> findLogLine(LogQuery query, int retryMax) {
        LogService service = LogServiceFactory.getLogService();
        Iterator<RequestLogs> iterator = null;
        for (int i = 0; i <= retryMax; i++) {
            iterator = service.fetch(query).iterator();
            if (iterator.hasNext()) {
                return iterator;
            }
            pause(1500);
        }
        return iterator;
    }

    protected AppLogLine findLogLine(String text, LogQuery logQuery, int retryMax) {
        for (int i = 0; i <= retryMax; i++) {
            AppLogLine line = findLogLine(text, logQuery);
            if (line != null) {
                return line;
            }
            pause(1500);
        }
        return null;
    }

    protected AppLogLine findLogLine(String text, LogQuery logQuery) {
        Iterable<RequestLogs> iterable = LogServiceFactory.getLogService().fetch(logQuery);
        for (RequestLogs logs : iterable) {
            for (AppLogLine logLine : logs.getAppLogLines()) {
                if (logLine.getLogMessage().contains(text)) {
                    return logLine;
                }
            }
        }
        return null;
    }

    protected void assertLogDoesntContain(String text) {
        int retryMax = 1;
        assertFalse("log should not contain '" + text + "', but it does", logContains(text, retryMax));
    }

    protected void assertLogContains(String text) {
        assertLogContains(text, null);
    }

    protected void assertLogContains(String text, LogService.LogLevel logLevel) {
        int retryMax = 4;
        AppLogLine logLine = findLogLineContaining(text, retryMax);
        assertNotNull("log should contain '" + text + "', but it does not", logLine);
        if (logLevel != null) {
            assertEquals("incorrect logLevel for text '" + text + "'", logLevel, logLine.getLogLevel());
        }
    }

    protected void assertLogQueryReturns(String text, LogQuery logQuery) {
        AppLogLine logLine = findLogLine(text, logQuery);
        assertNotNull("logQuery should return '" + text + "', but it does not", logLine);
    }

    protected void assertLogQueryDoesNotReturn(String text, LogQuery logQuery) {
        AppLogLine logLine = findLogLine(text, logQuery);
        assertNull("logQuery should not return '" + text + "', but it does", logLine);
    }

    /**
     * Create unique marker for a log line.
     *
     * @return timestamp plus random number
     */
    protected String getTimeStampRandom() {
        int num = (int) (Math.random() * 1000000);
        String rand = Integer.toString(num);

        return System.currentTimeMillis() + "_" + rand;
    }

}

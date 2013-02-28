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

package com.google.appengine.tck.logservice;

import java.util.logging.Handler;
import java.util.logging.Logger;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import com.google.appengine.tck.base.ServicesLifecycles;
import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
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
        ServicesLifecycles.after(service);
    }

    protected void flush(Logger log) {
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
        for (int i = 0; i < retryMax; i++) {
          if (findLogLineContaining(text, retryMax) != null) {
            return true;
          }
          pause(1000);
        }
        return false;
    }

    protected AppLogLine findLogLineContaining(String text, int retryMax) {
        LogQuery logQuery = new LogQuery().includeAppLogs(true).includeIncomplete(true);
        return findLogLine(text, logQuery, retryMax);
    }

  protected AppLogLine findLogLine(String text, LogQuery logQuery, int retryMax) {
    for (int i = 0; i < retryMax; i++) {
      AppLogLine line = findLogLine(text, logQuery);
      if (line != null) {
        return line;
      }
      pause(1000);
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
      int retryMax = 2;
        assertFalse("log should not contain '" + text + "', but it does", logContains(text, retryMax));
    }

    protected void assertLogContains(String text) {
        assertLogContains(text, null);
    }

    protected void assertLogContains(String text, LogService.LogLevel logLevel) {
        int retryMax = 5;
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
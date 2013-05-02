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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

}

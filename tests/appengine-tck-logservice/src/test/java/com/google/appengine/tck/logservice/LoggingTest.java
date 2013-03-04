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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        String logMsg = "Parameterized message " + logMark + " with params {0} and {1}";
        String logExpect = "Parameterized message " + logMark + " with params param1 and 222";
        log.log(Level.INFO, logMsg, new Object[] {"param1", 222});
        flush(log);

        int retryMax = 1;
        AppLogLine logLine = findLogLineContaining(logMark, retryMax);
        assertNotNull("log should contain " + logMark + " but it does not", logLine);
        assertEquals(logExpect, logLine.getLogMessage());
    }

}
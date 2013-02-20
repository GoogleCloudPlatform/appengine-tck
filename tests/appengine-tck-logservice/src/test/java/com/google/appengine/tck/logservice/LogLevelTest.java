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

import java.util.logging.Logger;

import com.google.appengine.api.log.LogService;
import com.google.appengine.tck.base.TestContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class LogLevelTest extends LoggingTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = newTestContext().setAppEngineWebXmlFile("appengine-web-with-logging-properties.xml");
        return getTckDeployment(context)
                .addAsWebInfResource("logging-all.properties", "logging.properties");
    }

    @Test
    public void testLogLevelInAppLogLineMatchesActualLogLevelUsedWhenLogging() {
        Logger log = Logger.getLogger(LogLevelTest.class.getName());
        log.finest("finest_testLogLevelMatches");
        log.finer("finer_testLogLevelMatches");
        log.fine("fine_testLogLevelMatches");
        log.config("config_testLogLevelMatches");
        log.info("info_testLogLevelMatches");
        log.warning("warning_testLogLevelMatches");
        log.severe("severe_testLogLevelMatches");
        flush(log);

        assertLogContains("finest_testLogLevelMatches", LogService.LogLevel.DEBUG);
        assertLogContains("finer_testLogLevelMatches", LogService.LogLevel.DEBUG);
        assertLogContains("fine_testLogLevelMatches", LogService.LogLevel.DEBUG);
        assertLogContains("config_testLogLevelMatches", LogService.LogLevel.DEBUG);

        // we can't test the following on dev appserver, because it returns incorrect logLevels
        // more info at http://code.google.com/p/googleappengine/issues/detail?id=8651
        //TODO:Renable after project compiles and runs.
//        assertLogContains("info_testLogLevelMatches", LogService.LogLevel.INFO);
//        assertLogContains("warning_testLogLevelMatches", LogService.LogLevel.WARN);
//        assertLogContains("severe_testLogLevelMatches", LogService.LogLevel.ERROR);
    }

}
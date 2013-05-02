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

import java.util.logging.Logger;

import com.google.appengine.api.log.LogService;
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
        return getDefaultDeployment(newTestContext()).addAsWebInfResource("logging-all.properties", "logging.properties");
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
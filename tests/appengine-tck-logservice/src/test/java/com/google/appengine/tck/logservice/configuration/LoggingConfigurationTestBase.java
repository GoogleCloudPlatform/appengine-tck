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

package com.google.appengine.tck.logservice.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.logservice.LoggingTestBase;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author Marko Luksa
 */
public abstract class LoggingConfigurationTestBase extends LoggingTestBase {

    public static final List<Level> LEVELS = Arrays.asList(Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST);

    protected void assertLogOnlyLogsMessagesAboveOrAtLevel(Level minLevel) {
        long start = System.currentTimeMillis();

        Logger log = Logger.getLogger(getClass().getName());
        for (Level level : LEVELS) {
            log.log(level, createMessage(level, start));
        }
        flush(log);

        for (Level level : LEVELS) {
            if (level.intValue() >= minLevel.intValue()) {
                assertLogContains(createMessage(level, start));
            } else {
                assertLogDoesntContain(createMessage(level, start));
            }
        }
    }

    private String createMessage(Level lev, long start) {
        return "Log message at level " + lev.getName() + " (" + start + ")";
    }

    protected static Archive getDeploymentWithLoggingLevelSetTo(Level level) {
        TestContext context = newTestContext().setAppEngineWebXmlFile("appengine-web-with-logging-properties.xml");
        WebArchive war = getDefaultDeployment(context);
        war.addClass(LoggingConfigurationTestBase.class);
        war.addAsWebInfResource(new StringAsset(".level=" + level.getName()), "logging.properties");
        return war;
    }

}

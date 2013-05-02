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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class LogServiceTest extends LoggingTestBase {

    private LogService service;

    private void assertLogQueryExecutes(LogQuery logQuery, String testName, List<String> exceptionList) {
        try {
            Iterable<RequestLogs> iterable = service.fetch(logQuery);
            iterable.iterator().hasNext();
        } catch (Exception e) {
            exceptionList.add(testName + ": " + e.toString());
        }
    }

    @Before
    public void setUp() throws Exception {
        service = LogServiceFactory.getLogService();
    }

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment(newTestContext());
    }

    @Test
    public void testAllKindsOfLogQueries() {
        List<String> exceptions = new ArrayList<String>();
        assertLogQueryExecutes(new LogQuery(), "testDefaultQuery", exceptions);
        assertLogQueryExecutes(new LogQuery().minLogLevel(LogService.LogLevel.WARN), "testMinLogLevel", exceptions);
        assertLogQueryExecutes(new LogQuery().includeIncomplete(true), "testIncludeIncompleteTrue", exceptions);
        assertLogQueryExecutes(new LogQuery().includeIncomplete(false), "testIncludeIncompleteFalse", exceptions);
        assertLogQueryExecutes(new LogQuery().includeAppLogs(true), "testIncludeAppLogsTrue", exceptions);
        assertLogQueryExecutes(new LogQuery().includeAppLogs(false), "testIncludeAppLogsFalse", exceptions);
        assertLogQueryExecutes(new LogQuery().batchSize(20), "testBatchSize", exceptions);
        assertLogQueryExecutes(new LogQuery().offset(null), "testOffset", exceptions);
        assertLogQueryExecutes(new LogQuery().majorVersionIds(Arrays.asList("1", "2", "3")), "testMajorVersionIds", exceptions);
        // TODO assertLogQueryExecutes(new LogQuery().serverVersions(Collections.singletonList(Pair.of((String) null, (String) null))), "testServerVersions", exceptions);
        assertLogQueryExecutes(new LogQuery().startTimeMillis(System.currentTimeMillis()), "testStartTimeMillis", exceptions);
        assertLogQueryExecutes(new LogQuery().startTimeUsec(1000L * System.currentTimeMillis()), "testStartTimeUsec", exceptions);
        assertLogQueryExecutes(new LogQuery().endTimeMillis(System.currentTimeMillis()), "testEndTimeMillis", exceptions);
        assertLogQueryExecutes(new LogQuery().endTimeUsec(1000L * System.currentTimeMillis()), "testEndTimeUsec", exceptions);
        assertLogQueryExecutes(
            new LogQuery()
                .minLogLevel(LogService.LogLevel.WARN)
                .includeIncomplete(true)
                .includeAppLogs(true)
                .batchSize(20)
                .offset(null)
                .majorVersionIds(Arrays.asList("1", "2", "3"))
                .startTimeMillis(System.currentTimeMillis() - 3000L)
                .endTimeMillis(System.currentTimeMillis() - 2000L),
            "testCombo", exceptions);
        assertEquals(exceptions.toString(), 0, exceptions.size());
    }

    @Test
    public void testAllKindsOfLogQueriesWithBuilder() {
        List<String> exceptions = new ArrayList<String>();
        assertLogQueryExecutes(LogQuery.Builder.withDefaults(), "testDefaultQuery", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withMinLogLevel(LogService.LogLevel.WARN), "testMinLogLevel", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withIncludeIncomplete(true), "testIncludeIncompleteTrue", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withIncludeIncomplete(false), "testIncludeIncompleteFalse", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withIncludeAppLogs(true), "testIncludeAppLogsTrue", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withIncludeAppLogs(false), "testIncludeAppLogsFalse", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withBatchSize(20), "testBatchSize", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withOffset(null), "testOffset", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withMajorVersionIds(Arrays.asList("1", "2", "3")), "testMajorVersionIds", exceptions);
        // TODO assertLogQueryExecutes(LogQuery.Builder.withServerVersions(Collections.singletonList(Pair.of((String) null, (String) null))), "testServerVersions", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withStartTimeMillis(System.currentTimeMillis()), "testStartTimeMillis", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withStartTimeUsec(1000L * System.currentTimeMillis()), "testStartTimeUsec", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withEndTimeMillis(System.currentTimeMillis()), "testEndTimeMillis", exceptions);
        assertLogQueryExecutes(LogQuery.Builder.withEndTimeUsec(1000L * System.currentTimeMillis()), "testEndTimeUsec", exceptions);
        assertLogQueryExecutes(
            LogQuery.Builder
                .withMinLogLevel(LogService.LogLevel.WARN)
                .includeIncomplete(true)
                .includeAppLogs(true)
                .batchSize(20)
                .offset(null)
                .majorVersionIds(Arrays.asList("1", "2", "3"))
                .startTimeMillis(System.currentTimeMillis() - 3000L)
                .endTimeMillis(System.currentTimeMillis() - 2000L),
            "testCombo", exceptions);
        assertEquals(exceptions.toString(), 0, exceptions.size());
    }

    @Test
    public void testLogLinesAreReturnedOnlyWhenRequested() {
        Logger log = Logger.getLogger(LogServiceTest.class.getName());
        log.info("hello_testLogLinesAreReturnedOnlyWhenRequested");
        flush(log);

        for (RequestLogs logs : service.fetch(new LogQuery().includeIncomplete(true).includeAppLogs(false))) {
            assertTrue("AppLogLines should be empty", logs.getAppLogLines().isEmpty());
        }

        for (RequestLogs logs : service.fetch(new LogQuery().includeIncomplete(true).includeAppLogs(true))) {
            if (!logs.getAppLogLines().isEmpty()) {
                // if we've found at least one appLogLine, the test passed
                return;
            }
        }
        fail("Should have found at least one appLogLine, but didn't find any");
    }

}
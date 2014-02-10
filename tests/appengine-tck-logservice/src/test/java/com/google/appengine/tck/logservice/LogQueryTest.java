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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.log.LogService.LogLevel.DEBUG;
import static com.google.appengine.api.log.LogService.LogLevel.ERROR;
import static com.google.appengine.api.log.LogService.LogLevel.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class LogQueryTest extends LoggingTestBase {

    private Logger log;

    private static String request1Id;
    private static String request2Id;
    private static String request3Id;

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment(newTestContext());
    }

    public LogQueryTest() {
        super(false);
    }

    @Before
    public void setUp() throws Exception {
        log = Logger.getLogger(LogQueryTest.class.getName());
    }

    @Test
    @InSequence(1)
    public void createCompleteRequest1() throws Exception {
        request1Id = getCurrentRequestId();
        log.info("info_createCompleteRequest1");
        log.warning("warning_createCompleteRequest1");
        flush(log);
    }

    @Test
    @InSequence(2)
    public void createCompleteRequest2() throws Exception {
        request2Id = getCurrentRequestId();
        log.severe("severe_createCompleteRequest2");
        flush(log);
    }

    @Test
    @InSequence(3)
    public void createCompleteRequest3() throws Exception {
        request3Id = getCurrentRequestId();
        log.info("info_createCompleteRequest3");
        flush(log);
    }

    @Test
    @InSequence(20)
    public void testMinLogLevel() throws Exception {
        sync(5000);  // Wait for previous requests.
        String infoLogMsg = "info_incompleteRequest-" + System.currentTimeMillis();
        log.info(infoLogMsg);
        flush(log);
        sync(5000);  // Yes, even after a flush we need to wait for the logs to be available.

        LogQuery debugLogQuery = new LogQuery().includeAppLogs(true).includeIncomplete(true).minLogLevel(DEBUG);
        assertLogQueryReturns("info_createCompleteRequest1", debugLogQuery);
        assertLogQueryReturns("warning_createCompleteRequest1", debugLogQuery);
        assertLogQueryReturns("severe_createCompleteRequest2", debugLogQuery);
        assertLogQueryReturns("info_createCompleteRequest3", debugLogQuery);
        assertLogQueryReturns(infoLogMsg, debugLogQuery);

        LogQuery warnLogQuery = new LogQuery().includeAppLogs(true).includeIncomplete(true).minLogLevel(WARN);
        assertLogQueryReturns("info_createCompleteRequest1", warnLogQuery);
        assertLogQueryReturns("warning_createCompleteRequest1", warnLogQuery);
        assertLogQueryReturns("severe_createCompleteRequest2", warnLogQuery);
        assertLogQueryDoesNotReturn("info_createCompleteRequest3", warnLogQuery);
        assertLogQueryDoesNotReturn(infoLogMsg, warnLogQuery);

        LogQuery errorLogQuery = new LogQuery().includeAppLogs(true).includeIncomplete(true).minLogLevel(ERROR);
        assertLogQueryReturns("severe_createCompleteRequest2", errorLogQuery);
        assertLogQueryDoesNotReturn("info_createCompleteRequest1", errorLogQuery);
        assertLogQueryDoesNotReturn("warning_createCompleteRequest1", errorLogQuery);
        assertLogQueryDoesNotReturn("info_createCompleteRequest3", errorLogQuery);
        assertLogQueryDoesNotReturn(infoLogMsg, errorLogQuery);
    }

    @Test
    @InSequence(20)
    public void testIncomplete() throws Exception {
        // GAE dev server doesn't handle this
        if (execute("testIncomplete") == false) {
            return;
        }
        String infoLogMsg = "info_incompleteRequest-" + System.currentTimeMillis();
        log.info(infoLogMsg);
        flush(log);
        sync(5000);  // Yes, even after a flush we need to wait for the logs to be available.

        assertLogQueryReturns(infoLogMsg, new LogQuery().includeAppLogs(true).includeIncomplete(true));

        assertLogQueryReturns("info_createCompleteRequest1", new LogQuery().includeAppLogs(true).includeIncomplete(true));
        assertLogQueryDoesNotReturn(infoLogMsg, new LogQuery().includeAppLogs(true)
            .includeIncomplete(false).startTimeMillis(System.currentTimeMillis() - 60000));
    }

    @Test
    @InSequence(20)
    public void testRequestIds() throws Exception {
        LogService service = LogServiceFactory.getLogService();

        LogQuery logQuery = new LogQuery().requestIds(Arrays.asList(request1Id, request2Id));
        Iterator<RequestLogs> iterator = service.fetch(logQuery).iterator();
        assertEquals(request1Id, iterator.next().getRequestId());
        assertEquals(request2Id, iterator.next().getRequestId());
        assertFalse(iterator.hasNext());

        logQuery = new LogQuery().requestIds(Arrays.asList(request2Id));
        iterator = service.fetch(logQuery).iterator();
        assertEquals(request2Id, iterator.next().getRequestId());
        assertFalse(iterator.hasNext());
    }

    @Test
    @InSequence(20)
    public void testGetBatchSize() throws Exception {
        long size = 1;
        LogService service = LogServiceFactory.getLogService();
        LogQuery logQuery = new LogQuery().requestIds(Arrays.asList(request1Id, request2Id)).batchSize((int)size);

        Iterator<RequestLogs> iterator = service.fetch(logQuery).iterator();
        assertNotNull(iterator.next());
        // TODO: renable when expected behavior is confirmed.
//        assertFalse("Batch size 1 so there should not be another log", iterator.hasNext());
        long batchSize = logQuery.getBatchSize();
        assertEquals(size, batchSize);
    }

    @Test
    @InSequence(20)
    public void testGetters() throws Exception {
        long batchSize = 20;
        long startMilliTime = System.currentTimeMillis() - 3000L;
        long endMilliTime = System.currentTimeMillis() - 2000L;
        List<String> majorVersions = Arrays.asList("1", "2", "3");
        LogQuery logQuery =  new LogQuery()
            .batchSize((int) batchSize)
            .startTimeMillis(startMilliTime)
            .endTimeMillis(endMilliTime)
            .minLogLevel(LogService.LogLevel.WARN)
            .includeIncomplete(true)
            .includeAppLogs(true)
            .offset(null)
            .majorVersionIds(majorVersions);

        executeQuery(logQuery);

        // The LogQuery should be unmodified, so you can re-use.
        assertEquals(batchSize, (long)logQuery.getBatchSize());
        assertEquals(startMilliTime, (long)logQuery.getStartTimeMillis());
        assertEquals(startMilliTime * 1000, (long)logQuery.getStartTimeUsec());
        assertEquals(endMilliTime, (long)logQuery.getEndTimeMillis());
        assertEquals(endMilliTime * 1000, (long)logQuery.getEndTimeUsec());
        assertEquals(LogService.LogLevel.WARN, logQuery.getMinLogLevel());
        assertEquals(true, logQuery.getIncludeIncomplete());
        assertEquals(true, logQuery.getIncludeAppLogs());
        assertEquals(null, logQuery.getOffset());
        assertEquals(majorVersions, logQuery.getMajorVersionIds());
        assertEquals(new ArrayList<String>(), logQuery.getRequestIds());

        List<LogQuery.Version> versions = Arrays.asList(new LogQuery.Version("module1", "1"), new LogQuery.Version("module2", "3"));
        logQuery =  new LogQuery()
            .versions(versions)
            .startTimeMillis(startMilliTime);

        executeQuery(logQuery);

        assertEquals(versions, logQuery.getVersions());
    }

    private void executeQuery(LogQuery logQuery) {
        LogService service = LogServiceFactory.getLogService();
        Iterator<RequestLogs> iterator = service.fetch(logQuery).iterator();
    }

}

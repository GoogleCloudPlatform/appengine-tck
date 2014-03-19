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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.RequestLogs;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tck.event.Property;
import org.apache.commons.codec.binary.Base64;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests to check if RequestLogs returned by LogService contain all the necessary data.
 *
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class RequestLogsTest extends LoggingTestBase {

    public static final String USER_AGENT = "GAE TCK Test";
    public static final String REFERRER = "http://www.referrer.com/foo.html";
    public static final String REQUEST_1_ENTITY_NAME = "1";
    public static final String REQUEST_1_RESOURCE = "/index.jsp?entityName=" + REQUEST_1_ENTITY_NAME;
    public static final String REQUEST_2_ENTITY_NAME = "2";
    public static final String REQUEST_2_RESOURCE = "/index2.jsp?entityName=" + REQUEST_2_ENTITY_NAME;
    public static final String REQUEST_3_ENTITY_NAME = "3";
    public static final String REQUEST_3_RESOURCE = "/index3.jsp?entityName=" + REQUEST_3_ENTITY_NAME;
    public static final String REGEX_IP4 = "[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+";
    public static final String REGEX_TIMESTAMP = "[0-9]{1,2}/[A-Za-z]{3}/[0-9]{4}:[0-9]{2}:[0-9]{2}:[0-9]{2} [+\\-][0-9]{4}";
    public static final String REGEX_REQUEST_LOG_ID = "([0-9]|[a-f])+";

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getDefaultDeployment(newTestContext());
        war.addAsWebResource("doNothing.jsp", "index.jsp");
        war.addAsWebResource("doNothing.jsp", "index2.jsp");
        war.addAsWebResource("throwException.jsp", "index3.jsp");
        war.addAsWebResource("storeTestData.jsp");
        war.addAsWebResource("currentTimeUsec.jsp");
        return war;
    }

    public RequestLogsTest() {
        super(false);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (isInContainer()) {
            clear();
        }
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void createRequests(@ArquillianResource URL url) throws Exception {
        long time1 = getServerTimeUsec(url);
        performGetRequest(new URL(url, REQUEST_1_RESOURCE));
        long time2 = getServerTimeUsec(url);

        performPostRequest(new URL(url, REQUEST_2_RESOURCE));

        try {
            performGetRequest(new URL(url, REQUEST_3_RESOURCE));
        } catch (IOException ignored) {
        }

        // since we're running as a client, we need to pass data to the server, so we can use them later in testStartAndEndTimeUsec
        storeTestData(url, time1, time2);
    }

    private long getServerTimeUsec(URL url) throws IOException {
        String response = performGetRequest(new URL(url, "currentTimeUsec.jsp"));
        return Long.parseLong(response);
    }

    private void storeTestData(URL url, long time1, long time2) throws IOException {
        performGetRequest(new URL(url, "storeTestData.jsp?time1=" + time1 + "&time2=" + time2));
    }

    private String performGetRequest(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Referer", REFERRER);
        try {
            return readFullyAndClose(connection.getInputStream()).trim();
        } finally {
            connection.disconnect();
        }
    }

    private String readFullyAndClose(InputStream in) throws IOException {
        try {
            StringBuilder sbuf = new StringBuilder();
            int ch;
            while ((ch = in.read()) != -1) {
                sbuf.append((char) ch);
            }
            return sbuf.toString();
        } finally {
            in.close();
        }
    }

    private String performPostRequest(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (PrintWriter out = new PrintWriter(connection.getOutputStream())) {
                out.println("foo=bar");
            }

            return readFullyAndClose(connection.getInputStream()).trim();
        } finally {
            connection.disconnect();
        }
    }

    @Test
    @InSequence(20)
    public void testStartAndEndTimeUsec() throws Exception {
        RequestLogs requestLogs1 = getRequestLogs1();

        long time1 = getTime(1);
        long time2 = getTime(2);
        long startTimeUsec = requestLogs1.getStartTimeUsec();
        assertTrue("expected startTimeUsec to be >= " + time1 + ", but was " + startTimeUsec, startTimeUsec >= time1);
        assertTrue("expected startTimeUsec to be <= " + time2 + ", but was " + startTimeUsec, startTimeUsec <= time2);

        long endTimeUsec = requestLogs1.getEndTimeUsec();
        assertTrue("expected endTimeUsec to be >= " + time1 + ", but was " + endTimeUsec, endTimeUsec >= time1);
        assertTrue("expected endTimeUsec to be <= " + time2 + ", but was " + endTimeUsec, endTimeUsec <= time2);

        assertTrue("expected endTimeUsec to be more than startTimeUsec, but it wasn't (startTime was " + startTimeUsec + "; endTime was " + endTimeUsec, startTimeUsec < endTimeUsec);
    }

    @Test
    @InSequence(20)
    public void testMethod() throws Exception {
        assertEquals("GET", getRequestLogs1().getMethod());
        assertEquals("POST", getRequestLogs2().getMethod());
    }

    @Test
    @InSequence(20)
    public void testHttpVersion() throws Exception {
        assertEquals("HTTP/1.1", getRequestLogs1().getHttpVersion());
    }

    @Test
    @InSequence(20)
    public void testClientIp() throws Exception {
        RequestLogs requestLogs1 = getRequestLogs1();

        Property ip = property("testClientIp");
        if (ip.exists()) {
            assertEquals(ip.getPropertyValue(), requestLogs1.getIp());
        } else {
            assertRegexpMatches(REGEX_IP4, requestLogs1.getIp());
        }
    }

    @Test
    @InSequence(20)
    public void testHost() throws Exception {
        RequestLogs requestLogs1 = getRequestLogs1();
        assertEquals(getServerHostAndPort(), requestLogs1.getHost());
    }

    @Test
    @InSequence(20)
    public void testResource() throws Exception {
        assertEquals(REQUEST_1_RESOURCE, getRequestLogs1().getResource());
        assertEquals(REQUEST_2_RESOURCE, getRequestLogs2().getResource());
        assertEquals(REQUEST_3_RESOURCE, getRequestLogs3().getResource());
    }

    @Test
    @InSequence(20)
    public void testUserAgent() throws Exception {
        assertEquals(USER_AGENT, getRequestLogs1().getUserAgent());
    }

    @Test
    @InSequence(20)
    public void testReferrer() throws Exception {
        assertEquals(REFERRER, getRequestLogs1().getReferrer());
    }

    @Test
    @InSequence(20)
    public void testCombined() throws Exception {
        String regexp = REGEX_IP4 + " - - \\[" + REGEX_TIMESTAMP + "\\] \"" +
            Pattern.quote("GET " + REQUEST_1_RESOURCE + " HTTP/1.1") +
            "\" [0-9]+ [0-9]+ .*";
        assertRegexpMatches(regexp, getRequestLogs1().getCombined());
    }

    @Ignore("not implemented yet")
    @Test
    @InSequence(20)
    public void testInstanceKey() throws Exception {
        assertEquals("", getRequestLogs1().getInstanceKey());
    }

    @Test
    @InSequence(20)
    public void testNickname() throws Exception {
        assertEquals("", getRequestLogs1().getNickname());
        // TODO check if nickname returns correct user when user is logged in
    }

    @Ignore("Not implemented yet")
    @Test
    @InSequence(20)
    public void testTaskInfo() throws Exception {
        assertEquals("", getRequestLogs1().getTaskName());
        assertEquals("", getRequestLogs1().getTaskQueueName());
    }

    @Test
    @InSequence(20)
    public void testBackendInfo() throws Exception {
        assertEquals(-1, getRequestLogs1().getReplicaIndex());
        // TODO check backend request also
    }

    @Test
    @InSequence(20)
    public void testApplicationInfo() throws Exception {
        RequestLogs requestLogs1 = getRequestLogs1();
        String versionId = requestLogs1.getVersionId();
        assertTrue("1".equals(versionId) || versionId.startsWith("1."));
    }

    @Test
    @InSequence(20)
    public void testModuleId() throws Exception {
        RequestLogs requestLogs1 = getRequestLogs1();
        String moduleId = requestLogs1.getModuleId();
        assertEquals("default", moduleId);
    }

    @Test
    @InSequence(20)
    public void testRequestId() throws Exception {
        assertEquals(getRequest1Id(), getRequestLogs1().getRequestId());
        assertEquals(getRequest2Id(), getRequestLogs2().getRequestId());
    }

    @Test
    @InSequence(20)
    public void testRequestIdCurrentRequest() throws Exception {
        RequestLogs logs = getCurrentRequestLogs();
        assertNotNull(logs);
        String currentId = logs.getRequestId();
        assertEquals(getCurrentRequestId(), currentId);
    }

    @Test
    @InSequence(20)
    public void testStatus() throws Exception {
        assertEquals(200, getRequestLogs1().getStatus());
        assertEquals(200, getRequestLogs2().getStatus());
        assertEquals(500, getRequestLogs3().getStatus());
    }

    @Ignore("not implemented yet")
    @Test
    @InSequence(20)
    public void testResponseSize() throws Exception {
        long responseSize = getRequestLogs1().getResponseSize();
        assertTrue("expected responseSize to be more than 0, but was " + responseSize, responseSize > 0);
    }

    @Test
    @InSequence(20)
    public void testIsFinished() throws Exception {
        assertTrue(getRequestLogs1().isFinished());
        assertTrue(getRequestLogs2().isFinished());
    }

    @Test
    @InSequence(20)
    public void testIsFinishedCurrentRequest() throws Exception {
        RequestLogs logs = getCurrentRequestLogs();
        assertNotNull(logs);
        assertFalse(logs.isFinished());
    }

    @Test
    @InSequence(20)
    public void testCurrentRequestLogId() throws Exception {
        String id = getCurrentRequestId();
        assertTrue("request_log_id:" + id, id.matches(REGEX_REQUEST_LOG_ID));
    }

    /**
     * These could return different values from the implementations so just assert the basics.
     */
    @Test
    @InSequence(20)
    public void testMiscProperties() throws Exception {
        RequestLogs logs = getRequestLogs1();

        assertNotNull("App Engine Release, e.g. 1.8.0, or empty string.", logs.getAppEngineRelease());
        assertTrue("Estimated cost of this request, in dollars.", logs.getCost() >= 0.0);
        assertTrue("Time required to process this request in microseconds.", logs.getLatencyUsec() >= 0);
        assertTrue("Microseconds request spent in pending request queue, if was pending at all.", logs.getPendingTimeUsec() >= 0);
        assertFalse("This should never be a loading request: " + logs.toString(), logs.isLoadingRequest());

        String appId = SystemProperty.applicationId.get();  // appIds have a prefix according to datacenter.
        assertTrue("The application ID that handled this request.", logs.getAppId().endsWith(appId));

        long cycles = logs.getMcycles();
        assertTrue("Number of machine cycles used to process this request: " + cycles, cycles >= 0);

        String getOffsetMsg = "Base64-encoded offset used with subsequent LogQuery to continue reading logs at the point in time immediately following this request.";
        assertNotNull(getOffsetMsg, logs.getOffset());
        assertTrue("Should be Base64: " + logs.getOffset(), Base64.isBase64(logs.getOffset().getBytes()));

        String mapEntryMsg = "File or class within the URL mapping used for this request: " + logs.getUrlMapEntry();
        assertNotNull(mapEntryMsg, logs.getUrlMapEntry());
    }

    @Test
    @InSequence(20)
    public void testRequestLogsAreSortedNewestFirst() throws EntityNotFoundException {
        LogQuery query = new LogQuery().startTimeMillis(System.currentTimeMillis() - 60000);
        Iterator<RequestLogs> iterator = findLogLine(query, 3);

        Long previousEndTimeUsec = null;
        while (iterator.hasNext()) {
            RequestLogs requestLogs = iterator.next();
            long endTimeUsec = requestLogs.getEndTimeUsec();
            if (previousEndTimeUsec != null) {
                assertTrue(
                    "RequestLogs with endTimeUsec " + endTimeUsec + " was returned after RequestLogs with endTimeUsec " + previousEndTimeUsec,
                    previousEndTimeUsec >= endTimeUsec);
            }
            previousEndTimeUsec = endTimeUsec;
        }
    }


    private RequestLogs getCurrentRequestLogs() {
        return getRequestLogs(getCurrentRequestId());
    }

    private RequestLogs getRequestLogs2() throws EntityNotFoundException {
        return getRequestLogs(getRequest2Id());
    }

    private RequestLogs getRequestLogs3() throws EntityNotFoundException {
        return getRequestLogs(getRequest3Id());
    }

    private RequestLogs getRequestLogs1() throws EntityNotFoundException {
        return getRequestLogs(getRequest1Id());
    }

    private RequestLogs getRequestLogs(String request1Id) {
        LogQuery logQuery = new LogQuery().requestIds(Collections.singletonList(request1Id));
        Iterator<RequestLogs> iterator = findLogLine(logQuery, 2);
        if (iterator == null || !iterator.hasNext()) {
            return null;
        }
        return iterator.next();
    }

    private String getRequest1Id() throws EntityNotFoundException {
        return getRequestId(REQUEST_1_ENTITY_NAME);
    }

    private String getRequest3Id() throws EntityNotFoundException {
        return getRequestId(REQUEST_3_ENTITY_NAME);
    }

    private String getRequest2Id() throws EntityNotFoundException {
        return getRequestId(REQUEST_2_ENTITY_NAME);
    }

    private String getRequestId(String entityName) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        return (String) datastore.get(KeyFactory.createKey(ENTITY_KIND, entityName)).getProperty(REQUEST_ID_PROPERTY);
    }

    private long getTime(int i) throws EntityNotFoundException {
        Entity testDataEntity = getTestDataEntity();
        return (Long) testDataEntity.getProperty("time" + i);
    }

    private String getServerHostAndPort() throws EntityNotFoundException {
        String serverName = (String) getTestDataEntity().getProperty("serverName");
        long serverPort = (Long) getTestDataEntity().getProperty("serverPort");
        return serverName + (serverPort == 80 ? "" : (":" + serverPort));
    }

    private Entity getTestDataEntity() throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        return datastore.get(KeyFactory.createKey(ENTITY_KIND, ENTITY_NAME));
    }
}

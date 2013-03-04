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
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import com.google.apphosting.api.ApiProxy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Tests to check if RequestLogs returned by LogService contain all the necessary data.
 *
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class RequestLogsTest extends LoggingTestBase {

  public static final String USER_AGENT = "GAE TCK Test";
  public static final String REFERRER = "http://www.referrer.com/foo.html";
  public static final String ENTITY_KIND = "RequestLogs";
  public static final String ENTITY_NAME = "TimeData";
  public static final String REQUEST_ID_PROPERTY = "requestId";
  public static final String REQUEST_1_ENTITY_NAME = "1";
  public static final String REQUEST_2_ENTITY_NAME = "2";
  public static final String REQUEST_3_ENTITY_NAME = "3";
  public static final String REGEX_IP4 = "[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+";
  public static final String REGEX_TIMESTAMP = "[0-9]{1,2}/[A-Za-z]{3}/[0-9]{4}:[0-9]{2}:[0-9]{2}:[0-9]{2} [+\\-][0-9]{4}";
  public static final String REGEX_REQUEST_LOG_ID = "([0-9]|[a-f])+";

  private LogService service;

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

  @Before
  public void setUp() throws Exception {
    if (isInContainer()) {
      service = LogServiceFactory.getLogService();
    }
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
    performGetRequest(new URL(url, "index.jsp?entityName=" + REQUEST_1_ENTITY_NAME));
    long time2 = getServerTimeUsec(url);

    performPostRequest(new URL(url, "index2.jsp?entityName=" + REQUEST_2_ENTITY_NAME));

    try {
      performGetRequest(new URL(url, "index3.jsp?entityName=" + REQUEST_3_ENTITY_NAME));
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
      PrintWriter out = new PrintWriter(connection.getOutputStream());
      try {
        out.println("foo=bar");
      } finally {
        out.close();
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

    if (isRuntimeDev()) {
      assertEquals("127.0.0.1", requestLogs1.getIp());
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
    String contextPath = "";
    assertEquals(contextPath + "/index.jsp", getRequestLogs1().getResource());
    assertEquals(contextPath + "/index2.jsp", getRequestLogs2().getResource());
    assertEquals(contextPath + "/index3.jsp", getRequestLogs3().getResource());
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
    String resource = "/index.jsp?entityName=1";
    String regexp = REGEX_IP4 + " - - \\[" + REGEX_TIMESTAMP + "\\] \"" +
        Pattern.quote("GET " + resource + " HTTP/1.1") +
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
    assertEquals("1", requestLogs1.getVersionId());
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

  private String getCurrentRequestId() {
    return (String) ApiProxy.getCurrentEnvironment().getAttributes().get("com.google.appengine.runtime.request_log_id");
  }

}
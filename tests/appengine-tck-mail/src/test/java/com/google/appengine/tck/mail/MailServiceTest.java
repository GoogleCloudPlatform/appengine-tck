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
package com.google.appengine.tck.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests sending via {@link MailService#send} and {@link javax.mail.Transport#send}, and, for a
 * deployed application, receiving via a POST.
 * <p/>
 * Set the gateway via the commandline with -Dtck.mail.gateway=your-gateway Emails will look like
 * this: testuser@your-gateway.theappid.appspot.com
 *
 * @author terryok@google.com
 * @author ales.justin@jboss.org
 */
@RunWith(Arquillian.class)
public class MailServiceTest extends MailTestBase {

    private static final String BODY = "from GAE TCK Mail Test.\n";

    private static final int TIMEOUT_MAX = 30;

    private MailService mailService;
    private MemcacheService memcache;

    @Before
    public void setUp() {
        mailService = MailServiceFactory.getMailService();
        memcache = MemcacheServiceFactory.getMemcacheService();
    }

    protected boolean doExecute(String context) {
        return execute(context);
    }

    @Test
    public void testSendAndReceiveBasicMessage() throws Exception {
        MailService.Message msg = new MailService.Message();
        msg.setSender(getFrom());
        msg.setTo(getTo());

        // Send email to self for debugging.
        // msg.setCc("you@yourdomain.com");
        String subjectTag = "Basic-Message-Test-" + System.currentTimeMillis();
        msg.setSubject(subjectTag);
        msg.setTextBody(BODY);

        mailService.send(msg);

        if (doExecute("testSendAndReceiveBasicMessage") == false) {
            log.info("Not running on production, skipping assert.");
        } else {
            assertMessageReceived(subjectTag);
        }
    }

    @Test
    public void testAllowedHeaders() throws Exception {
        MailService.Message msg = new MailService.Message();
        msg.setSender(getFrom());
        msg.setTo(getTo());

        // Send email to self for debugging.
        // msg.setCc("you@yourdomain.com");
        String subjectTag = "Allowed-Headers-Test-" + System.currentTimeMillis();
        msg.setSubject(subjectTag);
        msg.setTextBody(BODY);

        // https://developers.google.com/appengine/docs/java/mail/#Sending_Mail_with_Headers
        Set<MailService.Header> headers = new HashSet<>();
        Map<String, String> headersMap = createExpectedHeaders();

        for (Map.Entry entry : headersMap.entrySet()) {
            headers.add(new MailService.Header(entry.getKey().toString(), entry.getValue().toString()));
        }
        msg.setHeaders(headers);
        mailService.send(msg);

        if (doExecute("testAllowedHeaders") == false) {
            log.info("Not running on production, skipping assert.");
        } else {
            assertMessageReceived(subjectTag);
            assertHeadersExist(headersMap, getMessageHeaders(subjectTag));
        }
    }

    @Test
    public void testJavaxTransportSendAndReceiveBasicMessage() throws Exception {
        Session session = instance(Session.class);
        if (session == null) {
            session = Session.getDefaultInstance(new Properties(), null);
        }
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(getFrom()));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(getTo()));

        // Send email to self for debugging.
        // msg.setRecipient(Message.RecipientType.CC, new InternetAddress("you@yourdomain.com"));
        String subjectTag = "Javax-Transport-Test-" + System.currentTimeMillis();
        msg.setSubject(subjectTag);
        msg.setText(BODY);
        Transport.send(msg);

        if (doExecute("testJavaxTransportSendAndReceiveBasicMessage") == false) {
            log.info("Not running on production, skipping assert.");
        } else {
            assertMessageReceived(subjectTag);
        }
    }

    @Test
    public void testSendToAdmin() throws Exception {
        MailService.Message msg = new MailService.Message();
        msg.setSender(getFrom());
        String subjectTag = "Send-to-admin-" + System.currentTimeMillis();
        msg.setSubject(subjectTag);
        msg.setTextBody(BODY);
        mailService.sendToAdmins(msg);

        // Assuming success if no exception was thrown without calling sendToAdmins();
    }

    private void assertMessageReceived(String subjectTag) {
        Map<String, String> mimeProps = pollForMail(subjectTag);
        if (mimeProps == null) {
            fail(subjectTag + " not found after " + TIMEOUT_MAX + " seconds.");
        }
        Map<String, String> expectedMimeProps = createExpectedMimePropertiesMap(subjectTag);

        assertEquals(expectedMimeProps, mimeProps);
    }

    private Map<String, String> getMessageHeaders(String subjectTag) {
        return pollForMail(subjectTag + "-HEADERS");
    }

    private void assertHeadersExist(Map<String, String> expected, Map<String, String> actual) {
        List<String> errors = new ArrayList<>();
        for (Map.Entry entry : expected.entrySet()) {
            String expectedHeader = (String) entry.getKey();
            String expectedValue = (String) entry.getValue();

            if (!actual.containsKey(expectedHeader)) {
                errors.add(expectedHeader + ": was not found.");
                continue;
            }

            String actualValue = actual.get(expectedHeader);
            if (!expectedValue.equals(actualValue)) {
                errors.add(expectedHeader + ": " + expectedHeader + " != " + actual.get(expectedHeader));
            }
        }
        assertTrue(errors.toString(), errors.isEmpty());
    }

    private Map<String, String> createExpectedMimePropertiesMap(String subjectKey) {
        Map<String, String> mimeProps = new HashMap<>();

        mimeProps.put("subject", subjectKey);
        mimeProps.put("from", getFrom());

        return mimeProps;
    }

    /**
     * Allowed headers.
     *
     * @return map of headers to be set and verified.
     */
    private Map<String, String> createExpectedHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("In-Reply-To", "123abc");
        headers.put("List-Id", "123abc");
        headers.put("List-Unsubscribe", "123abc");
        headers.put("On-Behalf-Of", "123abc");
        headers.put("References", "123abc");
        headers.put("Resent-Date", "123abc");
        headers.put("Resent-From", "123abc");
        headers.put("Resent-To", "123abc");

        return headers;
    }


    private String getTo() {
        return "to@" + appId() + "." + mailGateway();
    }

    private String getFrom() {
        return "from@" + appId() + "." + mailGateway();
    }

    private String appId() {
        return SystemProperty.applicationId.get();
    }

    private String mailGateway() {
        String gateway;
        try {
            gateway = readProperties(TCK_PROPERTIES).getProperty("tck.mail.gateway");
            if (gateway == null) {
                gateway = "appspotmail.com";
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        log.info("tck.mail.gateway = " + gateway);
        return gateway;
    }

    private Map<String, String> pollForMail(String subjectTag) {
        int secondsElapsed = 0;

        Map<String, String> testData = null;
        while (secondsElapsed++ <= TIMEOUT_MAX) {
            //noinspection unchecked
            testData = (Map<String, String>) memcache.get(subjectTag);
            if (testData != null) {
                return testData;
            }
            sync();
        }
        return testData;
    }
}

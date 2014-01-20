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
import java.util.Arrays;
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
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tck.mail.support.MimeProperties;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
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

    private static final String BODY = "Simple message.";

    private static final int TIMEOUT_MAX = 45;

    private MailService mailService;

    @Before
    public void setUp() {
        mailService = MailServiceFactory.getMailService();
        clear();
    }

    @After
    public void tearDown() {
        clear();
    }

    protected boolean doExecute(String context) {
        return execute(context);
    }

    @Test
    public void testSendAndReceiveBasicMessage() throws Exception {
        MimeProperties mp = new MimeProperties();
        mp.subject = "Basic-Message-Test-" + System.currentTimeMillis();
        mp.from = getEmail("from-basic-test");
        mp.to = getEmail("to-basic-test");
        mp.body = BODY;

        MailService.Message msg = new MailService.Message();
        msg.setSubject(mp.subject);
        msg.setSender(mp.from);
        msg.setTo(mp.to);
        msg.setTextBody(BODY);

        // Send email to self for debugging.
        // msg.setCc("you@yourdomain.com");

        mailService.send(msg);

        if (doExecute("testSendAndReceiveBasicMessage") == false) {
            log.info("Not running on production, skipping assert.");
        } else {
            assertMessageReceived(mp);
        }
    }

    @Test
    public void testSendAndReceiveFullMessage() throws Exception {
        final String htmlBody = "<html><body><b>I am bold.</b></body></html>";

        MimeProperties mp = new MimeProperties();
        mp.subject = "Full-Message-Test-" + System.currentTimeMillis();
        mp.from = getEmail("from-test-full");
        mp.to = getEmail("to-test-full");
        mp.cc = getEmail("cc-test-full");
        mp.bcc = getEmail("bcc-test-full");
        mp.replyTo = getEmail("replyto-test-full");

        mp.multiPartsList.add("I am bold.");
        mp.multiPartsList.add(htmlBody);

        MailService.Message msg = new MailService.Message();
        msg.setSubject(mp.subject);
        msg.setSender(mp.from);
        msg.setTo(mp.to);
        msg.setCc(mp.cc);
        msg.setBcc(mp.bcc);
        msg.setReplyTo(mp.replyTo);
        msg.setHtmlBody(htmlBody);

        mailService.send(msg);

        // Verify that send() did not modify msg.
        assertEquals(mp.subject, msg.getSubject());
        assertEquals(mp.to, msg.getTo().iterator().next());
        assertEquals(mp.from, msg.getSender());
        assertEquals(mp.cc, msg.getCc().iterator().next());
        assertEquals(mp.bcc, msg.getBcc().iterator().next());
        assertEquals(mp.replyTo, msg.getReplyTo());
        assertEquals(htmlBody, msg.getHtmlBody());

        if (doExecute("testSendAndReceiveFullMessage") == false) {
            log.info("Not running on production, skipping assert.");
        } else {
            assertMessageReceived(mp);
        }
    }

    @Test
    public void testValidAttachment() throws Exception {
        MimeProperties mp = new MimeProperties();
        mp.subject = "Valid-Attachment-Test-" + System.currentTimeMillis();
        mp.from = getEmail("from-test-valid-attachment");
        mp.to = getEmail("to-test-valid-attachment");

        MailService.Attachment attachment = createValidAttachment();
        mp.multiPartsList.add(BODY);
        mp.multiPartsList.add(new String(attachment.getData()));

        MailService.Message msg = new MailService.Message();
        msg.setSubject(mp.subject);
        msg.setSender(mp.from);
        msg.setTo(mp.to);
        msg.setTextBody(BODY);
        msg.setAttachments(attachment);

        mailService.send(msg);

        if (doExecute("testValidAttachment") == false) {
            log.info("Not running on production, skipping assert.");
        } else {
            assertMessageReceived(mp);
        }
    }

    @Test
    public void testInvalidAttachment() throws Exception {
        for (String extension : getInvalidAttachmentFileTypes()) {
            MimeProperties mp = new MimeProperties();
            mp.subject = "Invalid-Attachment-Test-" + extension + System.currentTimeMillis();
            mp.from = getEmail("from-test-invalid-attachment");
            mp.to = getEmail("to-test-invalid-attachment");

            MailService.Attachment attachment = createInvalidAttachment(extension);
            mp.multiPartsList.add(BODY);
            mp.multiPartsList.add(new String(attachment.getData()));

            MailService.Message msg = new MailService.Message();
            msg.setSubject(mp.subject);
            msg.setSender(mp.from);
            msg.setTo(mp.to);
            msg.setTextBody(BODY);
            msg.setAttachments(attachment);

            try {
                mailService.send(msg);
                throw new IllegalStateException("IllegalArgumentException not thrown for invalid attachment type. " + extension);
            } catch (IllegalArgumentException iae) {
                // as expected
            }
        }
    }

    private List<String> getInvalidAttachmentFileTypes() {
        String[] extensions = {"ade", "adp", "bat", "chm", "cmd", "com", "cpl", "exe",
            "hta", "ins", "isp", "jse", "lib", "mde", "msc", "msp", "mst", "pif", "scr",
            "sct", "shb", "sys", "vb", "vbe", "vbs", "vxd", "wsc", "wsf", "wsh"};
        return Arrays.asList(extensions);
    }

    private MailService.Attachment createValidAttachment() {
        byte[] bytes = "I'm attached to these valid bytes.".getBytes();
        return new MailService.Attachment("test-attach.txt", bytes);
    }

    private MailService.Attachment createInvalidAttachment(String extension) {
        byte[] bytes = "I've got an invalid file type.".getBytes();
        return new MailService.Attachment("test-attach." + extension, bytes);
    }

    @Test
    public void testBounceNotification() throws Exception {
        MimeProperties mp = new MimeProperties();
        mp.subject = "Bounce-Notification-Test-" + System.currentTimeMillis();
        mp.from = getEmail("from-test-bounce");
        mp.to = getEmail("to-test-bounce") + "bogus";
        mp.body = BODY;

        MailService.Message msg = new MailService.Message();
        msg.setSubject(mp.subject);
        msg.setSender(mp.from);
        msg.setTo(mp.to);
        msg.setTextBody(BODY);

        mailService.send(msg);

        mp.subject = "BOUNCED:" + mp.subject;  // BounceHandlerServelt also prepends this

        // Assert is not called for this test, see logs to verify BounceHandlerServlet was called.
        // assertMessageReceived(mp);
    }

    @Test
    public void testAllowedHeaders() throws Exception {
        MimeProperties mp = new MimeProperties();
        mp.subject = "Allowed-Headers-Test-" + System.currentTimeMillis();
        mp.from = getEmail("from-test-header");
        mp.to = getEmail("to-test-header");
        mp.body = BODY;

        MailService.Message msg = new MailService.Message();
        msg.setSubject(mp.subject);
        msg.setSender(mp.from);
        msg.setTo(mp.to);
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
            assertMessageReceived(mp);
            assertHeadersExist(createExpectedHeadersVerifyList(headersMap));
        }
    }

    @Test
    public void testJavaxTransportSendAndReceiveBasicMessage() throws Exception {
        Session session = instance(Session.class);
        if (session == null) {
            session = Session.getDefaultInstance(new Properties(), null);
        }
        MimeProperties mp = new MimeProperties();
        mp.subject = "Javax-Transport-Test-" + System.currentTimeMillis();
        mp.from = getEmail("from-test-x");
        mp.to = getEmail("to-test-x");
        mp.body = BODY;

        MimeMessage msg = new MimeMessage(session);
        msg.setSubject(mp.subject);
        msg.setFrom(new InternetAddress(mp.from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mp.to));
        msg.setText(BODY);
        // Send email to self for debugging.
        // msg.setRecipient(Message.RecipientType.CC, new InternetAddress("you@yourdomain.com"));

        Transport.send(msg);

        if (doExecute("testJavaxTransportSendAndReceiveBasicMessage") == false) {
            log.info("Not running on production, skipping assert.");
        } else {
            assertMessageReceived(mp);
        }
    }

    @Test
    public void testSendToAdmin() throws Exception {
        MailService.Message msg = new MailService.Message();
        msg.setSender(getEmail("from-admin-test"));
        String subjectTag = "Send-to-admin-" + System.currentTimeMillis();
        msg.setSubject(subjectTag);
        msg.setTextBody(BODY);
        mailService.sendToAdmins(msg);

        // Assuming success if no exception was thrown without calling sendToAdmins();
    }

    private void assertMessageReceived(MimeProperties expectedMimeProps) {
        MimeProperties mp = pollForMail();
        if (mp == null) {
            fail("No MimeProperties found in temp data after " + TIMEOUT_MAX + " seconds.");
        }

        assertEquals("subject:", expectedMimeProps.subject, mp.subject);
        assertEquals("from:", expectedMimeProps.from, mp.from);
        assertEquals("to:", expectedMimeProps.to, mp.to);
        assertEquals("cc:", expectedMimeProps.cc, mp.cc);

        // also, the to: and cc: would fail if bcc: was added to them.
        assertEquals("bcc:", MimeProperties.BLANK, mp.bcc);

        if (expectedMimeProps.replyTo.equals(MimeProperties.BLANK)) {
            assertEquals("replyTo:", expectedMimeProps.from, mp.replyTo);
        } else {
            assertEquals("replyTo:", expectedMimeProps.replyTo, mp.replyTo);
        }

        if (expectedMimeProps.body.equals(MimeProperties.BLANK)) {
            for (int i = 0; i < expectedMimeProps.multiPartsList.size(); i++) {
                String expectedPart = expectedMimeProps.multiPartsList.get(i).trim();
                String actualPart = mp.multiPartsList.get(i).trim();
                assertEquals(expectedPart, actualPart);
            }
        } else {
            assertEquals("body:", expectedMimeProps.body, mp.body);
        }
    }

    private void assertHeadersExist(List<String> expectedHeaderLines) {
        MimeProperties mp = pollForMail();
        if (mp == null) {
            fail("No MimeProperties found in temp data after " + TIMEOUT_MAX + " seconds.");
        }

        List<String> errors = new ArrayList<>();

        for (String headerLine : expectedHeaderLines) {
            if (!mp.headers.contains(headerLine)) {
                errors.add(headerLine + ": was not found.");
            }
        }

        if (!errors.isEmpty()) {
            errors.add("Actual: " + mp.headers);
        }
        assertTrue(errors.toString(), errors.isEmpty());
    }

    /**
     * Allowed headers.
     *
     * @return map of headers to be set and verified.
     */
    private List<String> createExpectedHeadersVerifyList(Map<String, String> map) {
        List<String> headers = new ArrayList<>();

        for (Map.Entry entry : map.entrySet()) {
            headers.add(entry.getKey() + ": " + entry.getValue());
        }
        return headers;
    }

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

    private String getEmail(String user) {
        return String.format("%s@%s.%s", user, appId(), mailGateway());
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

    private MimeProperties pollForMail() {
        int secondsElapsed = 0;

        while (secondsElapsed <= TIMEOUT_MAX) {
            MimeProperties mp = getLastMimeProperties();
            if (mp != null) {
                return mp;
            }
            sync(2000);
            secondsElapsed += 2;
        }
        return null;
    }
}

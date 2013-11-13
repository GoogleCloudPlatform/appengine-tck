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

package com.google.appengine.tck.xmpp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.MessageType;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * XMPP Tests.
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class SimpleXmppTest extends XmppTestBase {
    private static final int TIMEOUT_IN_SECONDS = 30;
    private static final String TEST_BODY = "Hello from SimpleXmppTest!";

    private String appId;
    private String xmppServer;
    private XMPPService xmppService;
    private DatastoreService datastoreService;

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment();
    }

    @Before
    public void setUp() {
        xmppService = XMPPServiceFactory.getXMPPService();
        datastoreService = DatastoreServiceFactory.getDatastoreService();
        clearTestData();
        initConfig();
    }

    @Test
    public void testXmppSendMessageAndReceiveDefaultJid() {
        if (execute("testXmppSendMessageAndReceiveDefaultJid") == false) {
            return;
        }

        JID fromJID = new JID(appId + "@" + xmppServer);
        // We're sending messages to ourselves, so toJID and fromJID are the same.
        @SuppressWarnings("UnnecessaryLocalVariable")
        JID toJID = fromJID;

        MessageBuilder builder = new MessageBuilder();
        builder.withMessageType(MessageType.valueOf("CHAT"));
        builder.withFromJid(fromJID);
        builder.withRecipientJids(toJID);
        String testBody = TEST_BODY + System.currentTimeMillis();
        builder.withBody(testBody);
        builder.asXml(false);
        Message msg = builder.build();

        SendResponse response = xmppService.sendMessage(msg);
        assertNotNull("expected a response", response);
        assertEquals(1, response.getStatusMap().size());
        assertEquals(SendResponse.Status.SUCCESS, response.getStatusMap().get(toJID));

        verifyChatReceivedWithBody(testBody);
    }

    private void verifyChatReceivedWithBody(String body) {
        Entity chat = pollForChatWithTimeout(TIMEOUT_IN_SECONDS);
        if (chat == null) {
            fail("gave up after " + TIMEOUT_IN_SECONDS + " seconds");
        }
        assertEquals(body, chat.getProperty("body"));
    }

    private void initConfig() {
        try {
            appId = readProperties(TCK_PROPERTIES).getProperty("appengine.appId");
            if (appId == null) {
                appId = "tckapp";
            }
            xmppServer = readProperties(TCK_PROPERTIES).getProperty("tck.xmpp.server");
            if (xmppServer == null) {
                xmppServer = "appspot.com";
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void clearTestData() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("XmppMsg");
        List<Key> msgs = new ArrayList<>();
        for (Entity e : ds.prepare(query).asIterable()) {
            msgs.add(e.getKey());
        }
        ds.delete(msgs);
    }

    private Entity pollForChatWithTimeout(int timeoutInSeconds) {
        Key testChatKey = KeyFactory.createKey("XmppMsg", "test");
        Entity chatMsg = null;
        while (timeoutInSeconds-- > 0) {
            try {
                chatMsg = datastoreService.get(testChatKey);
                break;
            } catch (EntityNotFoundException enfe) {
                sync();
            }
        }
        return chatMsg;
    }
}

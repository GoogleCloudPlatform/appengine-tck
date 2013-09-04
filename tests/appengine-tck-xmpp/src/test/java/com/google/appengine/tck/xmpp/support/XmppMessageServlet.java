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

package com.google.appengine.tck.xmpp.support;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handle incoming Xmpp requests.
 */
public class XmppMessageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(XmppMessageServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        XMPPService xmppService = XMPPServiceFactory.getXMPPService();
        Message message = xmppService.parseMessage(req);
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

        log.info("Chat received: " + message.getStanza());

        Entity entity = new Entity("XmppMsg", "test");
        entity.setProperty("type", message.getMessageType().toString());
        entity.setProperty("from", message.getFromJid().toString());
        entity.setProperty("to", message.getRecipientJids()[0].toString());
        entity.setProperty("body", message.getBody());
        datastoreService.put(entity);
    }
}


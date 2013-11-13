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

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

/**
 * Handle incoming Xmpp requests.
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public class XmppMessageServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(XmppMessageServlet.class.getName());
    private DatastoreService datastoreService;

    @Override
    public void init() throws ServletException {
        super.init();
        datastoreService = DatastoreServiceFactory.getDatastoreService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        XMPPService xmppService = XMPPServiceFactory.getXMPPService();
        Message message = xmppService.parseMessage(req);

        log.info("Chat received: " + message.getStanza());

        Entity entity = new Entity("XmppMsg", "test");
        entity.setProperty("type", message.getMessageType().toString());
        entity.setProperty("from", message.getFromJid().toString());
        entity.setProperty("to", message.getRecipientJids()[0].toString());
        entity.setProperty("body", message.getBody());
        datastoreService.put(entity);
    }
}


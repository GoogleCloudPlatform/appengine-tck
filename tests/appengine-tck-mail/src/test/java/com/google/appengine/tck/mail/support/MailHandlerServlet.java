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
package com.google.appengine.tck.mail.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.tck.base.TestBase;

/**
 * Handle incoming mail.
 *
 * @author terryok@google.com
 */
public class MailHandlerServlet extends HttpServlet {

    Logger log = Logger.getLogger(MailHandlerServlet.class.getName());

    /**
     * Stores subject and headers into memcache for test to confirm mail delivery.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {

        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage message;
        MimeProperties mp;
        try {
            message = new MimeMessage(session, req.getInputStream());
            mp = new MimeProperties(message);

            List<String> headers = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            Enumeration e = message.getAllHeaderLines();
            while (e.hasMoreElements()) {
                String headerLine = (String) e.nextElement();
                headers.add(headerLine);
                sb.append("\n").append(headerLine);
            }
            log.info("HEADERS: " + sb.toString());

            mp.headers = headers.toString();
            TestBase.putTempData(mp);

        } catch (MessagingException me) {
            log.severe("Error while processing email: " + me.toString());
        }
    }
}

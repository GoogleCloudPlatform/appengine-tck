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

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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
        MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
        Map<String, String> mimeProps = new HashMap<String, String>();
        Map<String, String> headers = new HashMap<String, String>();

        MimeMessage message = null;
        try {
            message = new MimeMessage(session, req.getInputStream());
            mimeProps.put("subject", message.getSubject());
            mimeProps.put("from", message.getFrom()[0].toString());

            StringBuilder sb = new StringBuilder();
            Enumeration e = message.getAllHeaderLines();
            while (e.hasMoreElements()) {
                String line = (String) e.nextElement();
                int delimiterIdx = line.indexOf(":");
                String header = line.substring(0, delimiterIdx);
                String value = line.substring(delimiterIdx + 2); // 2 = colon + space
                headers.put(header, value);

                sb.append("\n" + line);
            }
            log.info("HEADERS: " + sb.toString());

        } catch (MessagingException me) {
            mimeProps.put("error", me.toString());
            log.severe("Error while processing email: " + me.toString());
        }

        String testDataKey = mimeProps.get("subject");
        memcache.put(testDataKey, mimeProps);
        memcache.put(testDataKey + "-HEADERS", headers);
    }
}

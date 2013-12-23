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

import com.google.appengine.api.mail.BounceNotification;
import com.google.appengine.api.mail.BounceNotificationParser;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Because bounce notifications can take an undetermined amount of time, it does not log temp data.
 *
 * @author terryok@google.com
 */
public class BounceHandlerServlet extends HttpServlet {

    Logger log = Logger.getLogger(BounceHandlerServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            BounceNotification bounce = BounceNotificationParser.parse(req);

            // Bounced Raw MimeMessage
            MimeProperties mp = new MimeProperties(bounce.getRawMessage());
            mp.subject = "BOUNCED:" + mp.subject;
            log.info("Bounce Notification - Raw Message: " + mp);

            // Original Details
            BounceNotification.Details details;
            details = bounce.getOriginal();
            MimeProperties mpOriginal = new MimeProperties();
            mpOriginal.from = details.getFrom();
            mpOriginal.to = details.getTo();
            mpOriginal.subject = details.getSubject();
            mpOriginal.body = details.getText();
            log.info("Bounce Notification - Original Info: " + mpOriginal);

            // Notification Details
            details = bounce.getNotification();
            MimeProperties mpNotification = new MimeProperties();
            mpNotification.from = details.getFrom();
            mpNotification.to = details.getTo();
            mpNotification.subject = details.getSubject();
            mpNotification.body = details.getText();
            log.info("Bounce Notification - Notification Info: " + mpNotification);

        } catch (MessagingException me) {
            throw new IllegalStateException(me);
        }
    }
}

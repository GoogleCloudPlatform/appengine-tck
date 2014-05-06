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

package com.google.appengine.tck.login;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserServiceFactory;

/**
 * @author Julien Deray
 * @author Ales Justin
 */
public class GetLoginUrlServlet extends HttpServlet {
    private static final String HTML = "<html><body>%s</body></html>";

    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final PrintWriter writer = resp.getWriter();

        final String type = req.getParameter("type");
        if ("poke".equals(type)) {
            writer.write(String.format(HTML, "Poke!"));
        } else {
            String loginURL = UserServiceFactory.getUserService().createLoginURL(req.getParameter("location"));
            writer.write(String.format(HTML, String.format("<div id=\"login-url\">%s</div>", loginURL)));
        }
    }
}

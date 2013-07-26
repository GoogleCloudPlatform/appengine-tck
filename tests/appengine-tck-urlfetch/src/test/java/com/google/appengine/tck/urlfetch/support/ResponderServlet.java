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

package com.google.appengine.tck.urlfetch.support;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet used to generate responses for tests.
 */
public class ResponderServlet extends HttpServlet {

    public static final String DEFAULT_CONTENT = "Hello, world";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        if (action == null) {
            PrintWriter writer = response.getWriter();
            writer.print(DEFAULT_CONTENT);
        } else if (action.equals("send404")) {
            response.sendError(404, "Pretend I'm not here");
        } else if (action.equals("sleep10")) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                // ignored
            }
            response.getWriter().println("Slept 10 seconds\n");
        } else {
            response.sendError(500, "unknown action: " + action);
        }
    }
}

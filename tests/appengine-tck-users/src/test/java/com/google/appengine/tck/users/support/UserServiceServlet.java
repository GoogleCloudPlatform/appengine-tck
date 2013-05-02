/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.appengine.tck.users.support;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

/**
 * Used by tests marked with @RunAsClient, since it can log into the servlet authenticated and
 * exercise the UserService.
 */
public class UserServiceServlet extends HttpServlet {

    private UserService userService;
    private User user;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        userService = UserServiceFactory.getUserService();
        user = userService.getCurrentUser();
        String env = SystemProperty.environment.value().toString();
        String responseMsg = env + ",";

        String method = req.getParameter("method");

        if (method == null) {
            resp.getWriter().print(responseMsg + "Error: Must set method parameter.");
            return;
        }

        if (method.equals("env")) {
            responseMsg += env;
            resp.getWriter().print(responseMsg);
            return;
        }

        responseMsg += callMethod(method);
        resp.getWriter().print(responseMsg);
    }

    private String callMethod(String method) {
        try {
            if (method.equals("getEmail")) {
                if (user == null) {
                    return "user is null";
                } else {
                    return user.getEmail();
                }
            } else if (method.equals("isUserLoggedIn")) {
                return "" + userService.isUserLoggedIn();
            } else if (method.equals("isUserAdmin")) {
                return "" + userService.isUserAdmin();
            } else {
                return "UNRECOGNIZED METHOD:" + method;
            }
        } catch (Exception e) {
            return method + ":" + e.toString();
        }
    }
}

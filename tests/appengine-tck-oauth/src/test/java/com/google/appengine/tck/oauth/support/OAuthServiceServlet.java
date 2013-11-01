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

package com.google.appengine.tck.oauth.support;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.utils.SystemProperty;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * Used by tests marked with @RunAsClient, since it can log into the servlet authenticated and
 * exercise the UserService.
 */
public class OAuthServiceServlet extends HttpServlet {

    private final Logger log = Logger.getLogger(getClass().getName());
    private OAuthService oAuthService;
    private User user;
    private String errorOnScope;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logHeaders(req);

        String currentScope = null;
        String lastValidScope = null;
        user = null;
        errorOnScope = null;

        oAuthService = OAuthServiceFactory.getOAuthService();
        Enumeration<String> scopes = req.getHeaders("oauth-test-scope");
        try {
            // If any scopes are invalid, exception is thrown, then error message returned via errorOnScope.
            while (scopes.hasMoreElements()) {
                currentScope = scopes.nextElement();
                user = oAuthService.getCurrentUser(currentScope);
                lastValidScope = currentScope;
                log.info("Valid scope, user: " + user.getEmail());
            }

        } catch (OAuthRequestException e) {
            errorOnScope = e.toString() + " Invalid scope: " + currentScope;
            log.info(errorOnScope);
        }

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

        responseMsg += callMethod(method, lastValidScope);
        resp.getWriter().print(responseMsg);
    }

    private String callMethod(String method, String scope) {
        try {
            if (method.equals("getEmail")) {
                if (errorOnScope != null) {
                    return errorOnScope;
                }
                if (user == null) {
                    return "user is null";
                } else {
                    return user.getEmail();
                }

            } else if (method.equals("isUserAdmin")) {
                if (scope == null) {
                    return "" + oAuthService.isUserAdmin();
                } else {
                    return "" + oAuthService.isUserAdmin(scope);
                }

            } else if (method.equals("getClientId")) {
                return oAuthService.getClientId(scope);

            } else if (method.equals("getOAuthConsumerKey")) {
                return oAuthService.getOAuthConsumerKey();

            } else if (method.equals("isUserAdmin")) {
                return "" + oAuthService.isUserAdmin();

            } else {
                return "UNRECOGNIZED METHOD:" + method;
            }
        } catch (Exception e) {
            return method + ":" + e.toString();
        }
    }

    private void logHeaders(HttpServletRequest req) {
        Enumeration<String> em = req.getHeaderNames();
        while (em.hasMoreElements()) {
            String h = em.nextElement();
            log.info(h + ":" + req.getHeader(h));
        }
    }
}

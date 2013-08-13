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

package com.google.appengine.tck.site;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class UpdateServlet extends HttpServlet {
    private String updateToken;
    private DatastoreService ds;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        updateToken = config.getInitParameter("update-token");
        ds = DatastoreServiceFactory.getDatastoreService();
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = req.getParameter("updateToken");
        if (token == null || token.equals(updateToken) == false) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String buildType = req.getParameter("buildType");
        long buildId = Long.parseLong(req.getParameter("buildId"));

        int failedTests = Integer.parseInt(req.getParameter("failedTests"));
        int passedTests = Integer.parseInt(req.getParameter("passedTests"));
        int ignoredTests = Integer.parseInt(req.getParameter("ignoredTests"));

        String[] tests = readFailedTests(req.getInputStream());

        Entity data = new Entity(DatastoreReport.class.getSimpleName());
        data.setProperty("buildType", buildType);
        data.setProperty("buildId", buildId);
        data.setProperty("failedTests", failedTests);
        data.setProperty("passedTests", passedTests);
        data.setProperty("ignoredTests", ignoredTests);
        data.setUnindexedProperty("failedTestsList", Arrays.asList(tests));

        log("Adding new data: " + data);

        ds.put(data);
    }

    private static String[] readFailedTests(InputStream is) throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = is.read()) != -1) {
                sb.append((char) ch);
            }
            return sb.toString().split("\n");
        } finally {
            is.close();
        }
    }
}

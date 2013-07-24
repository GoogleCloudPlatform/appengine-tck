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
import java.io.PrintWriter;
import java.util.Arrays;

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
public class SiteServlet extends HttpServlet {
    private DatastoreService ds;

    public void init() throws ServletException {
        super.init();
        ds = DatastoreServiceFactory.getDatastoreService();
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            PrintWriter writer = resp.getWriter();

            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n");

            drawChart(writer, "GaeJavaSdk_182", "SDK");
            drawChart(writer, "AppEngineTck_Capedwarf_Beta", "CapeDwarf");

            writer.write("</head>\n");
            writer.write("<body>\n");

            addDiv(writer, "SDK");
            addDiv(writer, "CapeDwarf");

            writer.write("</body>\n");
            writer.write("</html>\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void drawChart(PrintWriter writer, String buildType, String label) throws Exception {
        final Report report = new DatastoreReport(buildType);

        if (report.hasData(ds) == false) {
            writer.write(String.format("No data available for build type: %s\n", buildType));
            return;
        }

        int failedTests = report.getFailedTests();
        int passedTests = report.getPassedTests();
        int ignoredTests = report.getIgnoredTests();

        writer.write(String.format("<script type=\"text/javascript\">\n" +
            "      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});\n" +
            "      google.setOnLoadCallback(drawChart);\n" +
            "      function drawChart() {\n" +
            "        var data = google.visualization.arrayToDataTable([\n" +
            "          ['Type', '#'],\n" +
            "          ['Ignored', %s],\n" +
            "          ['Failed', %s],\n" +
            "          ['Passed', %s],\n" +
            "        ]);\n" +
            "\n" +
            "        var options = {\n" +
            "          title: ['%s']\n" +
            "        };\n" +
            "\n" +
            "        var chart = new google.visualization.PieChart(document.getElementById('%s_div'));\n" +
            "        chart.draw(data, options);\n" +
            "      }\n" +
            "    </script>", ignoredTests, failedTests, passedTests, label, label));
    }

    private static void addDiv(PrintWriter writer, String label) {
        writer.write(String.format("<div id=\"%s_div\" style=\"width: 900px; height: 500px;\"></div>", label));
    }
}

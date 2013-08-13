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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ReportServlet extends HttpServlet {
    private DatastoreService ds;

    public void init() throws ServletException {
        super.init();
        ds = DatastoreServiceFactory.getDatastoreService();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            PrintWriter writer = resp.getWriter();

            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n");

            drawChart(writer, "GaeJavaSdk", "SDK");
            drawChart(writer, "AppEngineTck_Capedwarf", "CapeDwarf");

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

    private void drawChart(PrintWriter writer, String buildTypeId, String label) throws Exception {
        final Report report = new DatastoreReport(buildTypeId);

        if (report.hasData(ds) == false) {
            writer.write(String.format("No data available for build type id: %s</p>\n", buildTypeId));
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

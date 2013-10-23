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

package com.google.appengine.tck.prospectivesearch.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.prospectivesearch.ProspectiveSearchServiceFactory;
import com.google.appengine.tck.base.TestBase;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class MatchResponseServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InvocationData invocationData = new InvocationData();
        invocationData.key = request.getParameter("key");
        invocationData.topic = request.getParameter("topic");
        invocationData.resultsOffset = Integer.parseInt(request.getParameter("results_offset"));
        invocationData.resultsCount = Integer.parseInt(request.getParameter("results_count"));
        String[] ids = request.getParameterValues("id");
        invocationData.subIds = (ids != null) ? Arrays.asList(ids) : new ArrayList<String>();
        if (request.getParameter("document") != null) {
            invocationData.lastReceivedDocument = ProspectiveSearchServiceFactory.getProspectiveSearchService().getDocument(request);
        }
        TestBase.putTempData(invocationData);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    public static int getInvocationCount() {
        return getInvocations().size();
    }

    public static boolean isInvoked() {
        return getInvocationCount() > 0;
    }

    public static List<InvocationData> getInvocations() {
        return TestBase.getAllTempData(InvocationData.class);
    }

    public static InvocationData getLastInvocationData() {
        return TestBase.getLastTempData(InvocationData.class);
    }

    public static void clear() {
        TestBase.deleteTempData(InvocationData.class);
    }

    public static List<String> getAllSubIds() {
        List<String> receivedSubIds = new ArrayList<>();
        for (InvocationData invocationData : getInvocations()) {
            receivedSubIds.addAll(invocationData.getSubIds());
        }
        return receivedSubIds;
    }
}

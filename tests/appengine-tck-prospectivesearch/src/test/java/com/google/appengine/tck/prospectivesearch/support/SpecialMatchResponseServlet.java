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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchServiceFactory;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class SpecialMatchResponseServlet extends HttpServlet {

    private static boolean invoked;
    private static Entity lastReceivedDocument;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        invoked = true;
        lastReceivedDocument = ProspectiveSearchServiceFactory.getProspectiveSearchService().getDocument(request);
    }

    public static boolean isInvoked() {
        return invoked;
    }

    public static Entity getLastReceivedDocument() {
        return lastReceivedDocument;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    public static void clear() {
        invoked = false;
        lastReceivedDocument = null;
    }
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.google.appengine.tck.taskqueue.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class RetryTestServlet extends HttpServlet {

    private static List<RequestData> requests = new ArrayList<RequestData>();

    private static int numberOfTimesToFail;
    private static int invocationCount;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        requests.add(new RequestData(req));
        invocationCount++;
        if (invocationCount <= numberOfTimesToFail) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);   // can be anything outside of 2xx
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

    public static void reset() {
        invocationCount = 0;
        requests.clear();
    }

    public static void setNumberOfTimesToFail(int failCount) {
        RetryTestServlet.numberOfTimesToFail = failCount;
    }

    public static int getInvocationCount() {
        return invocationCount;
    }

    public static RequestData getRequest(int index) {
        return requests.get(index);
    }
}

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FetchServlet extends HttpServlet {
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String content = new String(toBytes(req.getInputStream()));
        if ("Tralala".equals(content)) {
            resp.getWriter().write("Hopsasa");
        } else if ("Juhuhu".equals(content)) {
            resp.getWriter().write("Bruhuhu");
        } else if ("Headers!".equals(content)) {
            log("Setting header - <ABC : 123>!");
            // uncombined
            resp.addHeader("ABC", "123");
            // combined
            resp.addHeader("XYZ", "1");
            resp.addHeader("XYZ", "2");
            resp.addHeader("XYZ", "3");
        }
    }

    public static byte[] toBytes(InputStream is) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1) {
                baos.write(b);
            }
            return baos.toByteArray();
        } finally {
            is.close();
        }
    }
}

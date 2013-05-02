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

package com.google.appengine.tck.taskqueue.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;


/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class RequestData {

    private byte[] body;
    private Map<String, String> headers = new HashMap<String, String>();

    public RequestData(HttpServletRequest req) throws IOException {
        storeHeaders(req);
        storeBody(req);
    }

    private void storeHeaders(HttpServletRequest req) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, req.getHeader(header));
        }
    }

    private void storeBody(HttpServletRequest req) throws IOException {
        ServletInputStream in = req.getInputStream();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyStream(in, baos);
            body = baos.toByteArray();
        } finally {
            in.close();
        }
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public byte[] getBody() {
        return body;
    }

    /**
     * Copy stream.
     *
     * @param in  the input stream
     * @param out the output stream
     * @throws IOException for any IO error
     */
    private void copyStream(final InputStream in, final OutputStream out) throws IOException {
        final byte[] bytes = new byte[8192];
        int cnt;
        while ((cnt = in.read(bytes)) != -1) {
            out.write(bytes, 0, cnt);
        }
    }
}

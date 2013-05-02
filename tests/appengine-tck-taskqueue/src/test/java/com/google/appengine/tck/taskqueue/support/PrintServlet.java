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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class PrintServlet extends HttpServlet {
    private Logger log = Logger.getLogger(PrintServlet.class.getName());

    private static RequestHandler requestHandler;
    private static ServletRequest lastRequest;

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        log.info("Ping - " + req);

        if (requestHandler != null) {
            requestHandler.handleRequest(req);
        }

        lastRequest = req;

        final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            Entity entity = new Entity("Qwert");
            entity.setProperty("xyz", 123);
            Key key = ds.put(entity);

            entity = ds.get(key);
            log.info(entity.toString());

            FileService fs = FileServiceFactory.getFileService();
            AppEngineFile file = fs.createNewBlobFile("qwertfile");
            FileWriteChannel fwc = fs.openWriteChannel(file, false);
            try {
                log.info("b_l = " + fwc.write(ByteBuffer.wrap("qwert".getBytes())));
            } finally {
                fwc.close();
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static ServletRequest getLastRequest(final boolean reset) {
        try {
            return lastRequest;
        } finally {
            if (reset)
                reset();
        }
    }

    public static ServletRequest getLastRequest() {
        return getLastRequest(true);
    }

    public static void reset() {
        lastRequest = null;
        requestHandler = null;
    }

    public static void setRequestHandler(RequestHandler requestHandler) {
        PrintServlet.requestHandler = requestHandler;
    }

    public static interface RequestHandler {
        void handleRequest(ServletRequest req);
    }
}

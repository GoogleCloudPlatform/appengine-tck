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

/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.appengine.tck.byteman.support;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ConcurrentTxServlet extends HttpServlet {
    private final Random RANDOM = new Random();

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String entityGroup = req.getParameter("eg");
        String counter = req.getParameter("c");

        Entity entity = new Entity(entityGroup);
        entity.setProperty("foo", RANDOM.nextInt());

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        final Transaction tx = ds.beginTransaction();
        try {
            putEntity(ds, entity);
            tx.commit();
            resp.getWriter().write("OK" + counter);
        } catch (Exception e) {
            tx.rollback();
            resp.getWriter().write("ERROR" + counter + ":" + e.getClass());
        } finally {
            cleanup(counter);
        }
    }

    private void putEntity(DatastoreService ds, Entity entity) throws IOException {
        ds.put(entity);
    }

    private void cleanup(String counter) {
        log("Counter = " + counter);
    }
}

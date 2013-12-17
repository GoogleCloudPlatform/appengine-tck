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

package com.google.appengine.tck.blobstore.support;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.ByteRange;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BlobserviceServeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BlobstoreService service = BlobstoreServiceFactory.getBlobstoreService();

        String blobKey = request.getParameter("blobKey");
        ByteRange range = service.getByteRange(request);
        String blobRange = request.getParameter("blobRange");
        String blobRangeString = request.getParameter("blobRangeString");

        BlobKey key = new BlobKey(blobKey);
        if (range != null) {
            service.serve(key, range, response);
        } else if (blobRange != null) {
            service.serve(key, new ByteRange(Long.parseLong(blobRange)), response);
        } else if (blobRangeString != null) {
            service.serve(key, blobRangeString, response);
        } else {
            service.serve(key, response);
        }
    }
}

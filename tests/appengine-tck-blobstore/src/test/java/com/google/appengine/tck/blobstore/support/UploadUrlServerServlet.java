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

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class UploadUrlServerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        response.getWriter().println(blobstore.createUploadUrl(parseSuccessPath(request)));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        response.getWriter().println(blobstore.createUploadUrl(parseSuccessPath(request), parseOptions(request)));
    }

    protected String parseSuccessPath(HttpServletRequest request) {
        String successPath = request.getParameter("successPath");
        return (successPath != null) ? "/" + successPath : "/uploadHandler";
    }

    protected UploadOptions parseOptions(HttpServletRequest request) {
        UploadOptions options = UploadOptions.Builder.withDefaults();

        String maxPerBlob = request.getParameter("max_per_blob");
        if (maxPerBlob != null) {
            options.maxUploadSizeBytesPerBlob(Long.parseLong(maxPerBlob));
        }

        String maxAll = request.getParameter("max_all");
        if (maxAll != null) {
            options.maxUploadSizeBytes(Long.parseLong(maxAll));
        }

        String bucketName = request.getParameter("bucket_name");
        if (bucketName != null) {
            options.googleStorageBucketName(bucketName);
        }

        return options;
    }
}

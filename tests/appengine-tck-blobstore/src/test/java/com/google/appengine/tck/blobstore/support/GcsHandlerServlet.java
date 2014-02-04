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
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GcsHandlerServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        FileInfo info = getFirst(blobstore.getFileInfos(request));
        String gsObjectName = info.getGsObjectName();

        GcsService service = GcsServiceFactory.createGcsService();
        int p = gsObjectName.lastIndexOf("/");
        GcsFilename filename = new GcsFilename("GcsBucket", gsObjectName.substring(p + 1));
        GcsFileMetadata metadata = service.getMetadata(filename);
        if (metadata == null) {
            throw new IllegalStateException("Null GCS metadata: " + filename);
        }
        try (InputStream inputStream = Channels.newInputStream(service.openReadChannel(metadata.getFilename(), 0))) {
            IOUtils.copyStream(inputStream, response.getOutputStream());
        }
        response.getWriter().write("_123");
    }

    private <E> E getFirst(Map<String, List<E>> map) {
        for (Map.Entry<String, List<E>> entry : map.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                return entry.getValue().get(0);
            }
        }
        throw new IllegalStateException("Empty map!");
    }
}

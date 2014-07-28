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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GcsHandlerServlet extends HttpServlet {
    protected final Logger log = Logger.getLogger(getClass().getName());

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
        FileInfo info = UploadHandlerServlet.getFirst(blobstore.getFileInfos(request), false);
        String gsObjectName = info.getGsObjectName();
        String uploadedFilename = info.getFilename();

        if (!uploadedFilename.equals(request.getParameter("uploadedFilename"))) {
            throw new IllegalStateException("Uploaded filename is incorrect: expecting " + request.getParameter("uploadedFilename") + " but got " + uploadedFilename);
        }

        GcsService service = GcsServiceFactory.createGcsService();

        String mimeType;
        ServletOutputStream outputStream = response.getOutputStream();

        if (gsObjectName.contains("fake")) { //GCS Local File Upload Implementation is broken
            log.warning("Using hacked blobstore-gcs hybrid test because the local gcs implementation is broken");
            String blobKeyString = gsObjectName.substring(gsObjectName.indexOf("-") + 1, gsObjectName.lastIndexOf("-"));
            DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
            BlobInfoFactory blobInfoFactory = new BlobInfoFactory(datastoreService);
            mimeType = blobInfoFactory.loadBlobInfo(new BlobKey(blobKeyString)).getContentType();

            ByteArrayInputStream bais = new ByteArrayInputStream(BlobstoreServiceFactory.getBlobstoreService().fetchData(new BlobKey(blobKeyString), 0, BlobstoreService.MAX_BLOB_FETCH_SIZE - 1));
            IOUtils.copyStream(bais, outputStream);
            outputStream.write("_123".getBytes());
        } else {
            GcsFilename filename = new GcsFilename("GcsBucket", gsObjectName);
            GcsFileMetadata metadata = service.getMetadata(filename);
            if (metadata == null) {
                throw new IllegalStateException("Null GCS metadata: " + filename);
            }
            mimeType = metadata.getOptions().getMimeType();

            try (InputStream inputStream = Channels.newInputStream(service.openReadChannel(metadata.getFilename(), 0))) {
                IOUtils.copyStream(inputStream, outputStream);
            }
            outputStream.write("_123".getBytes());
        }

        if (mimeType == null || !mimeType.equals(request.getParameter("uploadedContentType"))) {
            throw new IllegalStateException("Uploaded mimeType is incorrect: expecting " + request.getParameter("uploadedContentType") + " but got " + mimeType);
        }
    }
}

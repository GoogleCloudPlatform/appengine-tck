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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.FileInfo;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class UploadHandlerServlet extends HttpServlet {

    private static BlobKey lastUploadedBlobKey;
    private static BlobInfo lastUploadedBlobInfo;
    private static FileInfo lastUploadedFileInfo;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();

        lastUploadedBlobKey = getFirst(blobstore.getUploads(request));
        lastUploadedBlobInfo = getFirst(blobstore.getBlobInfos(request));
        lastUploadedFileInfo = getFirst(blobstore.getFileInfos(request));
    }

    private <E> E getFirst(Map<String, List<E>> map) {
        for (Map.Entry<String, List<E>> entry : map.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                return entry.getValue().get(0);
            }
        }
        return null;
    }

    public static BlobKey getLastUploadedBlobKey() {
        return lastUploadedBlobKey;
    }

    public static BlobInfo getLastUploadedBlobInfo() {
        return lastUploadedBlobInfo;
    }

    public static FileInfo getLastUploadedFileInfo() {
        return lastUploadedFileInfo;
    }

    public static void reset() {
        lastUploadedBlobKey = null;
        lastUploadedBlobInfo = null;
        lastUploadedFileInfo = null;
    }
}

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
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class ServeBlobServlet extends HttpServlet {

    private FileService service = FileServiceFactory.getFileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mimeType = request.getParameter("mimeType");
        String contents = request.getParameter("contents");
        String blobRange = request.getParameter("blobRange");
        String name = request.getParameter("name");

        AppEngineFile file = service.createNewBlobFile(mimeType, name);
        writeToFile(file, contents);
        BlobKey blobKey = service.getBlobKey(file);

        response.addHeader("X-AppEngine-BlobKey", blobKey.getKeyString());
        if (blobRange != null) {
            response.addHeader("X-AppEngine-BlobRange", blobRange);
        }
    }

    private void writeToFile(AppEngineFile file, String content) throws IOException {
        FileWriteChannel channel = service.openWriteChannel(file, true);
        try {
            channel.write(ByteBuffer.wrap(content.getBytes()));
        } finally {
            channel.closeFinally();
        }
    }

}

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

package com.google.appengine.tck.blobstore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.blobstore.support.BlobserviceServeServlet;
import com.google.appengine.tck.blobstore.support.FileUploader;
import com.google.appengine.tck.blobstore.support.IOUtils;
import com.google.appengine.tck.blobstore.support.ServeBlobServlet;
import com.google.appengine.tck.blobstore.support.UploadHandlerServlet;
import com.google.appengine.tck.blobstore.support.UploadUrlServerServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class BlobstoreTestBase extends TestBase {
    protected static final char[] Hexadecimal = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static final String FILENAME = "uploadedFile.txt";
    public static final String CONTENT_TYPE = "text/plain";
    public static final byte[] UPLOADED_CONTENT = "uploaded content".getBytes();
    public static final String MD5_HASH;

    static String toHexString(byte[] bytes) {
        final char[] chars = new char[bytes.length * 2];
        for (int b = 0, c = 0; b < bytes.length; b++) {
            int v = (int) bytes[b] & 0xFF;
            chars[c++] = Hexadecimal[v / 16];
            chars[c++] = Hexadecimal[v % 16];
        }
        return new String(chars);
    }

    static {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            MD5_HASH = toHexString(md.digest(UPLOADED_CONTENT));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Deployment
    public static WebArchive getDeployment() {
        TestContext testContext = new TestContext().setWebXmlFile("blob_web.xml");
        return getTckDeployment(testContext)
            .addClass(BlobstoreTestBase.class)
            .addClass(IOUtils.class)
            .addClass(FileUploader.class)
            .addClass(ServeBlobServlet.class)
            .addClass(UploadUrlServerServlet.class)
            .addClass(UploadHandlerServlet.class)
            .addClass(BlobserviceServeServlet.class);
    }

    protected BlobKey writeNewBlobFile(String text) throws IOException {
        FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile file = fileService.createNewBlobFile("text/plain", "uploadedText.txt");
        FileWriteChannel channel = fileService.openWriteChannel(file, true);
        try {
            channel.write(ByteBuffer.wrap(text.getBytes()));
        } finally {
            channel.closeFinally();
        }
        return fileService.getBlobKey(file);
    }
}

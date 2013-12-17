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
import java.net.URL;
import java.nio.ByteBuffer;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.tck.blobstore.support.FileUploader;
import com.google.appengine.tck.blobstore.support.UploadHandlerServlet;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class BlobstoreUploadTest extends BlobstoreTestBase {

    @Test
    @RunAsClient
    @InSequence(10)
    public void testUploadedFileHasCorrectContent_upload(@ArquillianResource URL url) throws Exception {
        FileUploader fileUploader = new FileUploader();
        String uploadUrl = fileUploader.getUploadUrl(new URL(url, "getUploadUrl"));
        fileUploader.uploadFile(uploadUrl, "file", FILENAME, CONTENT_TYPE, UPLOADED_CONTENT);
    }

    @Test
    @InSequence(20)
    public void testUploadedFileHasCorrectContent_assert() throws Exception {
        BlobKey blobKey = UploadHandlerServlet.getLastUploadedBlobKey();
        assertNotNull("blobKey should not be null", blobKey);

        String contents = getFileContents(blobKey);
        assertEquals(new String(UPLOADED_CONTENT), contents);

        BlobInfo blobInfo = UploadHandlerServlet.getLastUploadedBlobInfo();
        assertNotNull("blobInfo should not be null", blobInfo);
        assertEquals(blobKey, blobInfo.getBlobKey());
        assertEquals(FILENAME, blobInfo.getFilename());
        assertEquals(CONTENT_TYPE, blobInfo.getContentType());
        assertEquals(UPLOADED_CONTENT.length, blobInfo.getSize());
        assertEquals(MD5_HASH, blobInfo.getMd5Hash());

        FileInfo fileInfo = UploadHandlerServlet.getLastUploadedFileInfo();
        assertNotNull("fileInfo should not be null", fileInfo);
        assertEquals(FILENAME, fileInfo.getFilename());
        assertEquals(CONTENT_TYPE, fileInfo.getContentType());
        assertEquals(UPLOADED_CONTENT.length, fileInfo.getSize());
        assertEquals(MD5_HASH, fileInfo.getMd5Hash());
    }

    @Test
    @InSequence(30)
    public void resetUploadHandlerServlet() throws Exception {
        UploadHandlerServlet.reset();
    }

    @Test
    @RunAsClient
    @InSequence(40)
    public void testSubmitMultipartFormWithoutFile_upload(@ArquillianResource URL url) throws Exception {
        FileUploader fileUploader = new FileUploader();
        String uploadUrl = fileUploader.getUploadUrl(new URL(url, "getUploadUrl"), FileUploader.Method.POST);
        fileUploader.uploadWithoutFile(uploadUrl, "file");
    }

    @Test
    @InSequence(50)
    public void testSubmitMultipartFormWithoutFile_assert() throws Exception {
        assertNull("blobKey should be null", UploadHandlerServlet.getLastUploadedBlobKey());
        assertNull("blobInfo should be null", UploadHandlerServlet.getLastUploadedBlobInfo());
        assertNull("fileInfo should be null", UploadHandlerServlet.getLastUploadedFileInfo());
    }

    private String getFileContents(BlobKey blobKey) throws IOException {
        AppEngineFile file = getAppEngineFile(blobKey);
        return getFileContents(file);
    }

    private AppEngineFile getAppEngineFile(BlobKey blobKey) {
        FileService fileService = FileServiceFactory.getFileService();
        return fileService.getBlobFile(blobKey);
    }

    private String getFileContents(AppEngineFile file) throws IOException {
        FileReadChannel channel = FileServiceFactory.getFileService().openReadChannel(file, true);
        try {
            return getStringFromChannel(channel, 1000);
        } finally {
            channel.close();
        }
    }

    private String getStringFromChannel(FileReadChannel channel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        int bytesRead = channel.read(buffer);

        byte[] bytes = new byte[bytesRead == -1 ? 0 : bytesRead];
        buffer.flip();
        buffer.get(bytes);

        return new String(bytes);
    }

}

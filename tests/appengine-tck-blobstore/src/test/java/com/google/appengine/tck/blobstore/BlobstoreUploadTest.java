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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
import org.junit.Assert;
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
public class BlobstoreUploadTest extends SimpleBlobstoreTestBase {
    private static Random RANDOM = new Random();

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

    @Test
    @RunAsClient
    @InSequence(60)
    public void testMaxPerBlob(@ArquillianResource URL url) throws Exception {
        FileUploader fileUploader = new FileUploader();
        // 5 is smaller then content's length
        String uploadUrl = fileUploader.getUploadUrl(new URL(url, "getUploadUrl"), Collections.singletonMap("max_per_blob", "5"));
        fileUploader.uploadFile(uploadUrl, "file", getRandomName(), CONTENT_TYPE, UPLOADED_CONTENT, 413);
    }

    @Test
    @RunAsClient
    @InSequence(70)
    public void testMaxAll(@ArquillianResource URL url) throws Exception {
        FileUploader fileUploader = new FileUploader();
        // 5 is smaller then content's length
        String uploadUrl = fileUploader.getUploadUrl(new URL(url, "getUploadUrl"), Collections.singletonMap("max_all", "5"));
        fileUploader.uploadFile(uploadUrl, "file", getRandomName(), CONTENT_TYPE, UPLOADED_CONTENT, 413);
    }

    @Test
    @RunAsClient
    @InSequence(80)
    public void testBucketName(@ArquillianResource URL url) throws Exception {
        FileUploader fileUploader = new FileUploader();
        String uploadUrl = fileUploader.getUploadUrl(new URL(url, "getUploadUrl"), Collections.singletonMap("bucket_name", "TheBucket"));
        String blobKey = fileUploader.uploadFile(uploadUrl, "file", getRandomName(), CONTENT_TYPE, UPLOADED_CONTENT);
        Assert.assertTrue(String.format("Received blobKey '%s'", blobKey), blobKey.contains("gs")); // TODO -- better way?
    }

    @Test
    @RunAsClient
    @InSequence(90)
    public void testGcs(@ArquillianResource URL url) throws Exception {
        FileUploader fileUploader = new FileUploader();
        Map<String, String> params = new HashMap<>();
        params.put("bucket_name", "GcsBucket");
        params.put("successPath", "gcsHandler");
        String uploadUrl = fileUploader.getUploadUrl(new URL(url, "getUploadUrl"), params);
        String result = fileUploader.uploadFile(uploadUrl, "file", getRandomName(), CONTENT_TYPE, "GcsTest".getBytes());
        Assert.assertEquals("GcsTest_123", result);
    }

    private static String getRandomName() {
        return String.format("file%s.txt", Math.abs(RANDOM.nextInt()));
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
        try (FileReadChannel channel = FileServiceFactory.getFileService().openReadChannel(file, true)) {
            return getStringFromChannel(channel, 1000);
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

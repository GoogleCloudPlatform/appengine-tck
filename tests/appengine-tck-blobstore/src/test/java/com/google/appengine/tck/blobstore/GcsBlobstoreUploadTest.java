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

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tck.blobstore.support.FileUploader;
import com.google.appengine.tck.blobstore.support.IOUtils;
import com.google.appengine.tck.lib.LibUtils;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class GcsBlobstoreUploadTest extends BlobstoreUploadTestBase {
    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getBaseDeployment();
        war.addClass(BlobstoreUploadTestBase.class);

        LibUtils libUtils = new LibUtils();
        libUtils.addLibrary(war, "com.google.guava", "guava");
        libUtils.addLibrary(war, "com.google.appengine.tools", "appengine-gcs-client");

        return war;
    }

    @Test
    @RunAsClient
    @InSequence(90)
    public void testGcs(@ArquillianResource URL url) throws Exception {
    	String filename = String.format("abc%s.txt", System.currentTimeMillis());

        FileUploader fileUploader = new FileUploader();
        Map<String, String> params = new HashMap<>();
        params.put("bucket_name", "GcsBucket");
        params.put("successPath", "gcsHandler?uploadedFilename=" + filename + "&uploadedContentType=" + CONTENT_TYPE);
        String uploadUrl = fileUploader.getUploadUrl(new URL(url, "getUploadUrl"), params);
        String result = fileUploader.uploadFile(uploadUrl, "file", filename, CONTENT_TYPE, "GcsTest".getBytes());
        Assert.assertEquals("GcsTest_123", result);
    }

    @Test
    @InSequence(100)
    public void testCreateGsBlobKey() throws Exception {
        final long ts = System.currentTimeMillis();
        final byte[] bytes = "FooBar".getBytes();

        GcsService service = GcsServiceFactory.createGcsService();
        GcsFilename filename = new GcsFilename("GcsBucket", String.valueOf(ts));
        GcsFileOptions options = new GcsFileOptions.Builder().mimeType(CONTENT_TYPE).build();
        try (GcsOutputChannel out = service.createOrReplace(filename, options)) {
            IOUtils.copy(Channels.newChannel(new ByteArrayInputStream(bytes)), out);
        }

        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        BlobKey key =  blobstoreService.createGsBlobKey("/gs/GcsBucket/" + ts);
        byte[] fetched = blobstoreService.fetchData(key, 0, 10);
        Assert.assertArrayEquals(bytes, fetched);
    }
}

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

package com.google.appengine.tck.images;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.images.ServingUrlOptions;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class ImageServingUrlTest extends ImagesServiceTestBase {

    private BlobKey blobKey;
    private BlobKey blobKey2;

    private FileService fileService;

    @Before
    public void setUp() throws Exception {
        fileService = FileServiceFactory.getFileService();
        AppEngineFile file = fileService.createNewBlobFile("image/png");
        FileWriteChannel channel = fileService.openWriteChannel(file, true);
        try {
            ReadableByteChannel in = Channels.newChannel(getImageStream("capedwarf.png"));
            try {
                copy(in, channel);
            } finally {
                in.close();
            }
        } finally {
            channel.closeFinally();
        }

        blobKey = fileService.getBlobKey(file);
        blobKey2 = blobKey;
    }

    @After
    public void tearDown() throws Exception {
        try {
            fileService.delete(fileService.getBlobFile(blobKey));
        } catch (IOException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void servingUrlWithNonexistentBlobKeyThrowsException() throws Exception {
        imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(new BlobKey("nonexistentBlob")));
    }

    @Test
    public void servingUrlWithImageSize() throws Exception {
        ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withBlobKey(blobKey);
        String baseUrl = imagesService.getServingUrl(servingUrlOptions);
        String actualUrl = imagesService.getServingUrl(servingUrlOptions.imageSize(32).crop(false));
        String expectedUrl = baseUrl + "=s32";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void servingUrlWithImageSizeAndCrop() throws Exception {
        ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withBlobKey(blobKey);
        String baseUrl = imagesService.getServingUrl(servingUrlOptions);
        String actualUrl = imagesService.getServingUrl(servingUrlOptions.imageSize(32).crop(true));
        String expectedUrl = baseUrl + "=s32-c";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void servingUrlWithSecureFlag() throws Exception {
        ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withBlobKey(blobKey);
        String url = imagesService.getServingUrl(servingUrlOptions.crop(false));
        assertStartsWith("http://", url);

        url = imagesService.getServingUrl(servingUrlOptions.imageSize(32).crop(false).secureUrl(false));
        assertStartsWith("http://", url);

        if (isRuntimeProduction() || execute("servingUrlWithSecureFlag")) {
            url = imagesService.getServingUrl(servingUrlOptions.secureUrl(true));
            assertStartsWith("https://", url);

            url = imagesService.getServingUrl(servingUrlOptions.imageSize(32).crop(false).secureUrl(true));
            assertStartsWith("https://", url);
        }
    }

    @Test
    public void servingUrlWithOptionsWithImageSize() throws Exception {
        String baseUrl = imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKey));
        String actualUrl = imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKey).imageSize(32));
        assertEquals(baseUrl + "=s32", actualUrl);
    }

    @Test
    public void servingUrlWithOptionsWithImageSizeAndCrop() throws Exception {
        String baseUrl = imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKey));
        String actualUrl = imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKey).imageSize(32).crop(true));
        assertEquals(baseUrl + "=s32-c", actualUrl);
    }

    @Test
    public void servingUrlWithOptionsWithSecureFlag() throws Exception {
        if (isRuntimeProduction() || execute("servingUrlWithOptionsWithSecureFlag")) {
            ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

            String url = imagesService.getServingUrl(options);
            assertStartsWith("http://", url);

            url = imagesService.getServingUrl(options.secureUrl(true));
            assertStartsWith("https://", url);
        }
    }

    private void assertStartsWith(String prefix, String url) {
        assertTrue("Expected string to start with \"" + prefix + "\", but was: " + url, url.startsWith(prefix));
    }

}

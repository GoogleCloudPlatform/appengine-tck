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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.tck.images.util.ImageRequest;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class ImageRequestTest extends ImagesServiceTestBase {

    @Test
    public void blobKeyOnly() throws Exception {
        ImageRequest request = new ImageRequest("/blobkey123/");
        assertEquals(new BlobKey("blobkey123"), request.getBlobKey());
        assertFalse(request.isTransformationRequested());
    }

    @Test
    public void blobKeyAndImageSize() throws Exception {
        ImageRequest request = new ImageRequest("/blobkey123/=s32");
        assertEquals(new BlobKey("blobkey123"), request.getBlobKey());
        assertTrue(request.isTransformationRequested());
        assertEquals(32, request.getImageSize());
        assertFalse(request.isCrop());
    }

    @Test
    public void blobKeyAndImageSizeAndCrop() throws Exception {
        ImageRequest request = new ImageRequest("/blobkey123/=s32-c");
        assertEquals(new BlobKey("blobkey123"), request.getBlobKey());
        assertTrue(request.isTransformationRequested());
        assertEquals(32, request.getImageSize());
        assertTrue(request.isCrop());
    }


}

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
import static org.junit.Assert.assertArrayEquals;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Tests tiff file.
 *
 * @author hchen@google.com (Hannah Chen)
 */

public class TifImageTest extends ImagesServiceTestBase {
    private static final String BEACH_TIF = "beach.tif";
    private static final OutputEncoding[] OUTPUT_ENCODE = {OutputEncoding.JPEG,
        OutputEncoding.PNG};
    private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
    private ImagesService imgService = ImagesServiceFactory.getImagesService();

    @Test
    public void testResize() throws FileNotFoundException, IOException {
        if (onAppServer()) {
            int resizeWidth = 150;
            int resizeHeigh = 150;
            Transform transform = ImagesServiceFactory.makeResize(resizeWidth, resizeHeigh);
            assertTransformation(transform, "Resize");
        }
    }

    @Test
    public void testRotate() throws FileNotFoundException, IOException {
        if (onAppServer()) {
            int rotageDegree = 90;
            Transform transform = ImagesServiceFactory.makeRotate(rotageDegree);
            assertTransformation(transform, "Rotate");
        }
    }

    @Test
    public void testHorizonalFlip() throws FileNotFoundException, IOException {
        if (onAppServer()) {
            Transform transform = ImagesServiceFactory.makeHorizontalFlip();
            assertTransformation(transform, "HorizontalFlip");
        }
    }

    @Test
    public void testVerticalFlip() throws FileNotFoundException, IOException {
        if (onAppServer()) {
            Transform transform = ImagesServiceFactory.makeVerticalFlip();
            assertTransformation(transform, "VerticalFlip");
        }
    }

    @Test
    public void testCrop() throws FileNotFoundException, IOException {
        if (onAppServer()) {
            String cropLeftX = "0.0";
            String cropTopY = "0.0";
            String cropRightX = "0.5";
            String cropBottomY = "0.5";
            Transform transform = ImagesServiceFactory.makeCrop(new Double(cropLeftX),
                new Float(cropTopY), new Float(cropRightX), new Float(cropBottomY));
            assertTransformation(transform, "Crop");
        }
    }

    @Test
    public void testFeelingLucky() throws FileNotFoundException, IOException {
        if (onAppServer()) {
            Transform transform = ImagesServiceFactory.makeImFeelingLucky();
            assertTransformation(transform, "ImFeelingLucky");
        }
    }

    private void assertTransformation(Transform transform, String transformType)
        throws FileNotFoundException, IOException {
        String expectedImage = "beach" + transformType + ".";
        for (OutputEncoding outType : OUTPUT_ENCODE) {
            String expectImageFile = expectedImage + outType.toString().toLowerCase();
            Image expected = readImage(expectImageFile);
            byte[] expect = expected.getImageData();
            Image image = readImage(BEACH_TIF);
            Image transImg = imgService.applyTransform(transform, image, outType);
            byte[] result = transImg.getImageData();
            assertArrayEquals(expect, result);
        }
    }
}

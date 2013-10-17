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

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.InputSettings;
import com.google.appengine.api.images.OutputSettings;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tck.event.Property;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class TransformationsTest extends ImagesServiceTestBase {
    protected static final String CAPEDWARF_PNG = "capedwarf.png";

    @Test
    public void testResize() throws IOException {
        InputSettings inputSettings = new InputSettings();
        OutputSettings outputSettings = new OutputSettings(OutputEncoding.PNG);

        Image originalImage = readImage(CAPEDWARF_PNG);
        assertEquals(200, originalImage.getWidth());
        assertEquals(143, originalImage.getHeight());

        originalImage = readImage(CAPEDWARF_PNG);
        Image resizedImage = imagesService.applyTransform(ImagesServiceFactory.makeResize(400, 286), originalImage);
        assertEquals(400, resizedImage.getWidth());
        assertEquals(286, resizedImage.getHeight());

        originalImage = readImage(CAPEDWARF_PNG);
        resizedImage = imagesService.applyTransform(ImagesServiceFactory.makeResize(300, 286), originalImage, inputSettings, outputSettings);
        assertEquals(300, resizedImage.getWidth());
        Property property = property("testResize");
        if (property != null) {
            assertEquals(property.getPropertyValue(), String.valueOf(resizedImage.getHeight()));
        } else {
            assertEquals(215, resizedImage.getHeight());
        }

        originalImage = readImage(CAPEDWARF_PNG);
        resizedImage = imagesService.applyTransform(ImagesServiceFactory.makeResize(400, 200), originalImage, outputSettings);
        assertEquals(280, resizedImage.getWidth());
        assertEquals(200, resizedImage.getHeight());
    }

    @Test
    public void testResizeWithStretch() throws IOException {
        int resizedWidth = 300;
        int resizedHeight = 200;
        Image originalImage = readImage(CAPEDWARF_PNG);
        Transform resize = ImagesServiceFactory.makeResize(resizedWidth, resizedHeight, true);
        Image resizedImage = imagesService.applyTransform(resize, originalImage);

        assertEquals(resizedWidth, resizedImage.getWidth());
        assertEquals(resizedHeight, resizedImage.getHeight());
    }

    @Test
    public void testResizeWithCrop() throws IOException {
        int resizedWidth = 300;
        int resizedHeight = 200;
        Image originalImage = readImage(CAPEDWARF_PNG);
        Transform resize = ImagesServiceFactory.makeResize(resizedWidth, resizedHeight, 0.5f,
             0.5f);
        Image resizedImage = imagesService.applyTransform(resize, originalImage);
        assertEquals(resizedWidth, resizedImage.getWidth());
        assertEquals(resizedHeight, resizedImage.getHeight());

        originalImage = readImage(CAPEDWARF_PNG);
        resize = ImagesServiceFactory.makeResize(resizedWidth, resizedHeight, 0.5, 0.5);
        resizedImage = imagesService.applyTransform(resize, originalImage);
        assertEquals(resizedWidth, resizedImage.getWidth());
        assertEquals(resizedHeight, resizedImage.getHeight());
    }
}

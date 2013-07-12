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

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class IntegrationTest extends ImagesServiceTestBase {

    @Test
    public void testMakeImage() throws IOException {
        Image image = readImage("capedwarf.png");
        assertNotNull(image);
    }

    @Test
    public void testHorizontalFlip() throws IOException {
        Image image = readImage("capedwarf.png");
        Transform horizontalFlip = ImagesServiceFactory.makeHorizontalFlip();

        Image flippedImage = imagesService.applyTransform(horizontalFlip, image);

        assertNotNull(flippedImage);
    }
}

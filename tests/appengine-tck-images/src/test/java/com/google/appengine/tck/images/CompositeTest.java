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

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.CompositeTransform;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.OutputSettings;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class CompositeTest extends ImagesServiceTestBase {

    @Test
    public void testDefault() throws IOException {
        // w - 200, h - 143
        Image originalImage = readImage(CAPEDWARF_PNG);
        Composite c1 = ImagesServiceFactory.makeComposite(originalImage, 0, 0, 1, Composite.Anchor.BOTTOM_LEFT);
        Image ci = imagesService.composite(Lists.newArrayList(c1), 200, 143, 0);
        assertImages(null, originalImage, ci);

    }

    @Test
    public void testEncoding() throws IOException {
        Image originalImage = readImage(CAPEDWARF_PNG);
        Composite c1 = ImagesServiceFactory.makeComposite(originalImage, 0, 0, 1, Composite.Anchor.BOTTOM_LEFT);
        Image ci = imagesService.composite(Lists.newArrayList(c1), 200, 143, 0, OutputEncoding.JPEG);
        assertImages(null, originalImage, ci);
    }

    @Test
    public void testSettings() throws IOException {
        Image originalImage = readImage(CAPEDWARF_PNG);
        Composite c1 = ImagesServiceFactory.makeComposite(originalImage, 0, 0, 1, Composite.Anchor.BOTTOM_LEFT);
        Image ci = imagesService.composite(Lists.newArrayList(c1), 200, 143, 0, new OutputSettings(OutputEncoding.JPEG));
        assertImages(null, originalImage, ci);
    }

    @Test
    public void testTransform() throws IOException {
        CompositeTransform transform = ImagesServiceFactory.makeCompositeTransform();
        transform.concatenate(ImagesServiceFactory.makeHorizontalFlip());
        transform.concatenate(ImagesServiceFactory.makeHorizontalFlip());
        Image originalImage = readImage(CAPEDWARF_PNG);
        Image result = imagesService.applyTransform(transform, originalImage);
        assertImages(transform, originalImage, result);

        transform = ImagesServiceFactory.makeCompositeTransform(Lists.newArrayList(ImagesServiceFactory.makeVerticalFlip(), ImagesServiceFactory.makeVerticalFlip()));
        originalImage = readImage(CAPEDWARF_PNG);
        result = imagesService.applyTransform(transform, originalImage);
        assertImages(transform, originalImage, result);
    }
}

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
import java.io.FileInputStream;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class MakeImageTest extends ImagesServiceTestBase {

    @Test
    public void makeImageCanReadJPG() throws IOException {
        byte[] imageData = readImageResource("capedwarf.jpg");
        assertMakeImageCanReadImage(imageData);
    }

    @Test
    public void makeImageCanReadPNG() throws IOException {
        byte[] imageData = readImageResource("capedwarf.png");
        assertMakeImageCanReadImage(imageData);
    }

    @Test
    public void makeImageCanReadGIF() throws IOException {
        byte[] imageData = readImageResource("capedwarf.gif");
        assertMakeImageCanReadImage(imageData);
    }

    @Test
    public void makeImageCanReadBMP() throws IOException {
        byte[] imageData = readImageResource("capedwarf.bmp");
        assertMakeImageCanReadImage(imageData);
    }

    @Test
    public void makeImageCanReadTIF() throws IOException {
        byte[] imageData = readImageResource("capedwarf.tif");
        assertMakeImageCanReadImage(imageData);
    }

    private void assertMakeImageCanReadImage(byte[] imageData) {
        Image image = ImagesServiceFactory.makeImage(imageData);
        assertNotNull(image);
    }

    private byte[] readImageResource(String resourceName) throws IOException {
        return toBytes(new FileInputStream(resourceName), true);
    }

}

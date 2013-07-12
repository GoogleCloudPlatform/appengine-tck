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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.images.util.ImageRequest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ImagesServiceTestBase extends TestBase {
    protected static final String[] TEST_FILES = {
        "jpgAttach.jpg",
        "pngAttach.png",
        "bmpAttach.bmp",
        "beach.tif",
        "beachCrop.jpeg", "beachCrop.png",
        "beachHorizontalFlip.jpeg", "beachHorizontalFlip.png",
        "beachImFeelingLucky.jpeg", "beachImFeelingLucky.png",
        "beachResize.jpeg", "beachResize.png",
        "beachRotate.jpeg", "beachRotate.png",
        "beachVerticalFlip.jpeg", "beachVerticalFlip.png",
        "capedwarf.jpg", "capedwarf.png", "capedwarf.gif", "capedwarf.bmp", "capedwarf.tif"};

    protected ImagesService imagesService;

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext();
        WebArchive war = getTckDeployment(context);

        war.addClasses(ImagesServiceTestBase.class, ImageRequest.class);

        for (String fName : TEST_FILES) {
            war.addAsResource("testdata/" + fName, fName);
        }

        return war;
    }

    @Before
    public void init() throws Exception {
        imagesService = ImagesServiceFactory.getImagesService();
    }

    protected InputStream getImageStream(String filename) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new IOException("No such resource: " + filename);
        }
        return is;
    }

    protected byte[] readImageBytes(String resourceName) throws IOException {
        return toBytes(getImageStream(resourceName), true);
    }

    protected Image readImage(String filename) throws IOException {
        return ImagesServiceFactory.makeImage(toByteArray(getImageStream(filename)));
    }

    protected static byte[] toBytes(InputStream is, boolean closeStream) throws IOException {
        return toBytes(is, 0, Long.MAX_VALUE, closeStream);
    }

    protected static byte[] toBytes(InputStream is, long start, long end, boolean closeStream)
        throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1 && end > 0) {
                if (start > 0)
                    continue;

                baos.write(b);
                start--;
                end--;
            }
            return baos.toByteArray();
        } finally {
            if (closeStream)
                try {
                    is.close();
                } catch (IOException ignored) {
                }
        }
    }

    protected static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[0x1000];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        from.close();
        to.close();
        return total;
    }

    protected static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    protected static String toString(InputStream in) throws IOException {
        return new String(toByteArray(in));
    }

    public static void copy(ReadableByteChannel in, WritableByteChannel out) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16 * 1024);
        while (in.read(buffer) != -1) {
            buffer.flip(); // Prepare the buffer to be drained
            while (buffer.hasRemaining()) {
                out.write(buffer);
            }
            buffer.clear(); // Empty buffer to get ready for filling
        }
    }
}

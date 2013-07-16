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

package org.jboss.capedwarf.tck;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import javax.imageio.ImageIO;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tck.event.AbstractImageLifecycle;
import com.google.appengine.tck.event.ImageLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycle;
import org.kohsuke.MetaInfServices;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(TestLifecycle.class)
public class CapeDwarfImageLifecycle extends AbstractImageLifecycle {
    protected void doBefore(ImageLifecycleEvent event) {
        event.setResult(true);
    }

    protected void doBeforeX(ImageLifecycleEvent event) {
        Image expected = event.getExpected();
        Image transformed = event.getTransformed();

        Raster er = getRaster(expected);
        Raster tr = getRaster(transformed);

        Transform op = event.getOp();
        String transformClassName = op.getClass().getName();
        // TODO
        if ("com.google.appengine.api.images.Resize".equals(transformClassName)) {
        } else if ("com.google.appengine.api.images.HorizontalFlip".equals(transformClassName)) {
            assertEquals(er.getWidth(), tr.getWidth());
            assertEquals(er.getHeight(), tr.getHeight());
            for (int y = 0; y < er.getHeight(); y++) {
                for (int x = 0; x < er.getWidth(); x++) {
                    if (assertPixelsEqual(er, x, y, tr, er.getWidth() - x - 1, y) == false) {
                        event.setResult(false);
                        return;
                    }
                }
            }
            event.setResult(true);
        } else if ("com.google.appengine.api.images.VerticalFlip".equals(transformClassName)) {
            assertEquals(er.getWidth(), tr.getWidth());
            assertEquals(er.getHeight(), tr.getHeight());
            for (int y = 0; y < er.getHeight(); y++) {
                for (int x = 0; x < er.getWidth(); x++) {
                    if (assertPixelsEqual(er, x, y, tr, er.getHeight() - x - 1, y) == false) {
                        event.setResult(false);
                        return;
                    }
                }
            }
            event.setResult(true);
        } else if ("com.google.appengine.api.images.Rotate".equals(transformClassName)) {
            int deg = getFieldValue(op, "degrees");
            if (deg == 90) {
                assertEquals(er.getWidth(), tr.getHeight());
                assertEquals(er.getHeight(), tr.getWidth());
                for (int y = 0; y < er.getHeight(); y++) {
                    for (int x = 0; x < er.getWidth(); x++) {
                        if (assertPixelsEqual(er, x, y, tr, tr.getWidth() - y - 1, x) == false) {
                            event.setResult(false);
                            return;
                        }
                    }
                }
                event.setResult(true);
            } else if (deg == 180) {
                assertEquals(er.getWidth(), tr.getWidth());
                assertEquals(er.getHeight(), tr.getHeight());
                for (int y = 0; y < er.getHeight(); y++) {
                    for (int x = 0; x < er.getWidth(); x++) {
                        if (assertPixelsEqual(er, x, y, tr, tr.getWidth() - x - 1, tr.getHeight() - y - 1) == false) {
                            event.setResult(false);
                            return;
                        }
                    }
                }
                event.setResult(true);
            } else if (deg == 270) {
                assertEquals(er.getWidth(), tr.getHeight());
                assertEquals(er.getHeight(), tr.getWidth());
                for (int y = 0; y < er.getHeight(); y++) {
                    for (int x = 0; x < er.getWidth(); x++) {
                        if (assertPixelsEqual(er, x, y, tr, y, tr.getHeight() - x - 1) == false) {
                            event.setResult(false);
                            return;
                        }
                    }
                }
                event.setResult(true);
            }
        } else if ("com.google.appengine.api.images.Crop".equals(transformClassName)) {
        } else if ("com.google.appengine.api.images.ImFeelingLucky".equals(transformClassName)) {
        } else if ("com.google.appengine.api.images.CompositeTransform".equals(transformClassName)) {
        }
    }

    protected void doAfter(ImageLifecycleEvent event) {
    }

    protected static WritableRaster getRaster(Image image) {
        BufferedImage bufferedImage = getBufferedImage(image);
        return bufferedImage.getRaster();
    }

    protected static BufferedImage getBufferedImage(Image image) {
        return convertToBufferedImage(image.getImageData());
    }

    protected static BufferedImage convertToBufferedImage(byte[] byteArray) {
        try {
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean assertPixelsEqual(Raster raster, int x1, int y1, Raster transformedRaster, int x2, int y2) {
        int pixel1[] = getPixel(raster, x1, y1);
        int pixel2[] = getPixel(transformedRaster, x2, y2);

        return arraysEqual(pixel1, pixel2);
    }

    public static int[] getPixel(Raster raster, int x, int y) {
        return raster.getPixel(x, y, (int[]) null);
    }

    public static boolean arraysEqual(int[] array1, int[] array2) {
        if (array1.length != array2.length) {
            return false;
        }

        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }

        return true;
    }

    public static String formatPixel(int[] pixel) {
        StringBuilder sbuf = new StringBuilder();
        for (int p : pixel) {
            sbuf.append(",").append(p);
        }
        return sbuf.substring(1);
    }

    @SuppressWarnings("unchecked")
    protected <V> V getFieldValue(Transform op, String fieldName) {
        return (V) getFieldValue(op, getAccessibleField(op.getClass(), fieldName));
    }

    @SuppressWarnings("unchecked")
    protected <V> V getFieldValue(Transform op, Field field) {
        try {
            return (V) field.get(op);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Field getAccessibleField(Class clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Class " + clazz + " does not seem to contain field " + fieldName, e);
        }
    }
}

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


/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@MetaInfServices(TestLifecycle.class)
public class CapeDwarfImageLifecycle extends AbstractImageLifecycle {

    public static final double DELTA = 15.0;

    protected void doBefore(ImageLifecycleEvent event) {
        BufferedImage expected = getBufferedImage(event.getExpected());
        BufferedImage actual = getBufferedImage(event.getTransformed());

        if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
            event.setResult(false);
            return;
        }

        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                int expectedARGB = expected.getRGB(x, y);
                int transformedARGB = actual.getRGB(x, y);
                double distance = distance(expectedARGB, transformedARGB);
                if (distance > DELTA) {
                    event.setResult(false);
                    return;
                }
            }
        }
        event.setResult(true);
    }

    private double distance(int argb1, int argb2) {
        int a1 = argb1 >> 24 & 0xff;
        int a2 = argb2 >> 24 & 0xff;
        int r1 = argb1 >> 16 & 0xff;
        int r2 = argb2 >> 16 & 0xff;
        int g1 = argb1 >> 8 & 0xff;
        int g2 = argb2 >> 8 & 0xff;
        int b1 = argb1 & 0xff;
        int b2 = argb2 & 0xff;
        return Math.sqrt((a1 - a2) ^ 2 + (r1 - r2) ^ 2 + (g1 - g2) ^ 2 + (b1 - b2) ^ 2);
    }

    protected void doAfter(ImageLifecycleEvent event) {
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

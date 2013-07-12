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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Tests for images service.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class ImagesServiceTest extends ImagesServiceTestBase {
    private static String[] FNAMES = {"jpgAttach.jpg", "pngAttach.png", "bmpAttach.bmp"};
    // ROTATE, NORMAL for Rotate; HMINUS for VerticalFlip; WMINUS for HorizontalFlip.
    private enum ChkType { NORMAL, ROTATE, WMINUS, HMINUS, CROP }
    private static int[] DEGREES = {90, 180, 270, 360};
    private static int[] QUALITY = {10, 50, 100};
    private static int[][] NEW_SIZES = {{500, 500}, {50, 500}, {500, 50}};
    private static OutputEncoding[] ENCODES = {OutputEncoding.JPEG, OutputEncoding.PNG};
    private ImagesService imgService = ImagesServiceFactory.getImagesService();

    @Test
    public void testFeelLucky() throws IOException {
        // I'm Feeling Lucky is not available in dev_appserver
        if (onAppServer()) {
            Transform transform = ImagesServiceFactory.makeImFeelingLucky();
            for (String sfile : FNAMES) {
                for (OutputEncoding encoding : ENCODES) {
                    applyAndVerify(sfile, transform, ChkType.NORMAL, encoding);
                }
            }
        }
    }

    @Test
    public void testHorizontalFlip() throws IOException {
        ChkType chkType = ChkType.NORMAL;

        if (onDevAppServer()) {
            chkType = ChkType.WMINUS;
        }
        Transform transform = ImagesServiceFactory.makeHorizontalFlip();
        for (String sfile : FNAMES) {
            for (OutputEncoding encoding : ENCODES) {
                applyAndVerify(sfile, transform, chkType, encoding);
            }
        }
    }

    @Test
    public void testVerticalFlip() throws IOException {
        ChkType chkType = ChkType.NORMAL;
        if (onDevAppServer()) {
            chkType = ChkType.HMINUS;
        }
        Transform transform = ImagesServiceFactory.makeVerticalFlip();
        for (String sfile : FNAMES) {
            for (OutputEncoding encoding : ENCODES) {
                applyAndVerify(sfile, transform, chkType, encoding);
            }
        }
    }

    @Test
    public void testResize() throws IOException {
        Image image;
        for (String sfile : FNAMES) {
            image = readImage(sfile);
            for (int[] exptSize : NEW_SIZES) {
                Transform transform = ImagesServiceFactory.makeResize(exptSize[0], exptSize[1]);
                for (OutputEncoding encoding : ENCODES) {
                    image = imgService.applyTransform(transform, readImage(sfile), encoding);
                    assertTrue((exptSize[0] == image.getWidth()) ||
                               (exptSize[1] == image.getHeight()));
                }
            }
        }
    }

    @Test
    public void testRotate() throws IOException {
        ChkType chkType;
        for (int dg : DEGREES) {
            Transform transform = ImagesServiceFactory.makeRotate(dg);
            if ((dg == 90) || (dg == 270)) {
                chkType = ChkType.ROTATE;
            } else {
                chkType = ChkType.NORMAL;
            }
            for (String sfile : FNAMES) {
                for (OutputEncoding encoding : ENCODES) {
                    applyAndVerify(sfile, transform, chkType, encoding);
                }
            }
        }
    }

    @Test
    public void testChop() throws IOException {
        ChkType chkType = ChkType.CROP;
        String cropLeftX = "0.0";
        String cropTopY = "0.0";
        String cropRightX = "0.5";
        String cropBottomY = "0.5";
        Transform transform = ImagesServiceFactory.makeCrop(new Float(cropLeftX),
             new Float(cropTopY), new Float(cropRightX), new Float(cropBottomY));
        for (OutputEncoding encoding : ENCODES) {
            applyAndVerify("pngAttach.png", transform, chkType, encoding);
        }
    }

    @Test
    public void testHistogram() throws IOException {
        int[][] expect = {{1, 63, 148, 23036}, {1408, 80, 79, 22192}, {1329, 82, 83, 22243}};
        Image image = readImage("jpgAttach.jpg");
        int[][] color = imgService.histogram(image);
        assertEquals(3, color.length);
        for (int i = 0; i < color.length; i++) {
            assertEquals(256, color[i].length);
            assertEquals(expect[i][0], color[i][0]);
            assertEquals(expect[i][1], color[i][100]);
            assertEquals(expect[i][2], color[i][200]);
            assertEquals(expect[i][3], color[i][255]);
        }
    }

    private void applyAndVerify(String fname, Transform transform, ChkType chkType,
                              OutputEncoding outType) throws IOException {
        int[] exptSize = new int[2];
        Image image = readImage(fname);
        if (chkType == ChkType.NORMAL) {
            exptSize[0] = image.getWidth();
            exptSize[1] = image.getHeight();
        } else if (chkType == ChkType.ROTATE) {
            exptSize[0] = image.getHeight();
            exptSize[1] = image.getWidth();
        } else if (chkType == ChkType.WMINUS) {
            exptSize[0] = image.getWidth() - 1;
            exptSize[1] = image.getHeight();
        } else if (chkType == ChkType.HMINUS) {
            exptSize[0] = image.getWidth();
            exptSize[1] = image.getHeight() - 1;
        } else if (chkType == ChkType.CROP) {
            exptSize[0] = image.getWidth() / 2;
            exptSize[1] = image.getHeight() / 2;
        }
        Image transImg = imgService.applyTransform(transform, image, outType);
        assertEquals(exptSize[0], transImg.getWidth());
        assertEquals(exptSize[1], transImg.getHeight());
    }
}

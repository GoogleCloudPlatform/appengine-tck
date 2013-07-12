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

package com.google.appengine.tck.images.util;

import com.google.appengine.api.blobstore.BlobKey;

import java.util.StringTokenizer;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class ImageRequest {

    public static final String SIZE_TOKEN = "=s";
    public static final String CROP_TOKEN = "-c";

    private BlobKey blobKey;
    private Integer imageSize;
    private boolean crop;

    public ImageRequest(String pathInfo) {
        StringTokenizer tokenizer = new StringTokenizer(pathInfo, "/");
        blobKey = new BlobKey(tokenizer.nextToken());
        if (tokenizer.hasMoreTokens()) {
            parseSizeAndCrop(tokenizer.nextToken());
        }
    }

    private void parseSizeAndCrop(String str) {
        if (str.startsWith(SIZE_TOKEN)) {
            if (str.endsWith(CROP_TOKEN)) {
                crop = true;
                str = str.substring(0, str.length() - CROP_TOKEN.length());
            }

            imageSize = Integer.parseInt(str.substring(SIZE_TOKEN.length()));
        }
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

    public boolean isTransformationRequested() {
        return imageSize != null;
    }

    public int getImageSize() {
        return imageSize;
    }

    public boolean isCrop() {
        return crop;
    }
}

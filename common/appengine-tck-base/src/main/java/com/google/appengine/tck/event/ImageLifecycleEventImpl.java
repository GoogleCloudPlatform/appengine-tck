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

package com.google.appengine.tck.event;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.Transform;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class ImageLifecycleEventImpl extends AbstractTestLifecycleEventImpl<Transform> implements ImageLifecycleEvent {
    private Image expected;
    private Image transformed;
    private Boolean result;

    ImageLifecycleEventImpl(Class<?> caller, Transform context, Image expected, Image transformed) {
        super(caller, context);
        this.expected = expected;
        this.transformed = transformed;
    }

    public Transform getOp() {
        return getContext();
    }

    public Image getExpected() {
        return expected;
    }

    public Image getTransformed() {
        return transformed;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Boolean result() {
        return result;
    }
}

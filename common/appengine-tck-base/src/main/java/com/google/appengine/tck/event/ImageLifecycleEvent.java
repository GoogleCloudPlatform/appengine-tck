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
public interface ImageLifecycleEvent extends TestLifecycleEvent {
    /**
     * Get transform op.
     *
     * @return the transform op
     */
    Transform getOp();

    /**
     * Get expected image.
     *
     * @return the expected image
     */
    Image getExpected();

    /**
     * Get transformer image.
     *
     * @return the transformed image
     */
    Image getTransformed();

    /**
     * Set result.
     *
     * @param result the result
     */
    void setResult(boolean result);

    /**
     * Get result, if it was determined / computed.
     *
     * @return true if images are equal, false otherwise, or null if no result
     */
    Boolean result();
}

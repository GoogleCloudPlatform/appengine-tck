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

package com.google.appengine.tck.endpoints.support;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiSerializationProperty;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Foo {
    private final int x;
    private final int y;
    private final int z;

    public Foo(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    @ApiSerializationProperty(ignored = AnnotationBoolean.TRUE)
    public int getY() {
        return y;
    }

    @ApiSerializationProperty(name = "qwerty")
    public int getZ() {
        return z;
    }
}

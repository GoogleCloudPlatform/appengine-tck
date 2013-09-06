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

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Api(
    name = SerializationEndPoint.NAME,
    version = SerializationEndPoint.VERSION)
public class SerializationEndPoint {

    public static final String NAME = "serializationEndPoint";
    public static final String VERSION = "v1";

    @ApiMethod(name = "s.bar", httpMethod = HttpMethod.GET)
    public Bar bar() {
        return new Bar(1, 2);
    }

    @ApiMethod(name = "s.foo", httpMethod = HttpMethod.POST)
    public Foo foo() {
        return new Foo(1, 2, 3);
    }

}

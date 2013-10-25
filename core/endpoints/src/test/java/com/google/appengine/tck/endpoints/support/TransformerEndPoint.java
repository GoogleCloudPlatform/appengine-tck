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
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@Api(
    name = TransformerEndPoint.NAME,
    version = TransformerEndPoint.VERSION,
    description = "Used for testing the Transformer interface.",
    transformers = { BazTransformer.class })
public class TransformerEndPoint {

    public static final String NAME = "transformerEndPoint";
    public static final String VERSION = "v1";

    @ApiMethod(name = "s.bar", httpMethod = HttpMethod.GET)
    public BarWrapper bar() {
        // Bar is transformed into a String, so we have to wrap it in an object since
        // String and primitive types cannot be returned.
        return new BarWrapper(new Bar(1, 2));
    }

    @ApiMethod(name = "s.baz", httpMethod = HttpMethod.GET)
    public BazWrapper baz() {
        // The BazTransformer is declared as a transformer in @Api above,
        // not on the BazTransformer class like BarTransformer is.
        return new BazWrapper(new Baz(3, 4));
    }

    @ApiMethod(name = "s.foo", httpMethod = HttpMethod.POST)
    public Foo foo() {
        return new Foo(1, 2, 3);
    }

}

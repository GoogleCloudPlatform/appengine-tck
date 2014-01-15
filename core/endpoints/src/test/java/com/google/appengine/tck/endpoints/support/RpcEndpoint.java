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

import java.util.Date;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

/**
 * @author Gregor Sfiligoj
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Api(name = RpcEndpoint.NAME, version = RpcEndpoint.VERSION, description = "RPC Endpoint API", scopes = { })
public class RpcEndpoint {

    public static final String NAME = "rpcendpoint";
    public static final String VERSION = "v1";

    @ApiMethod(name = "data.get", path = "data", httpMethod = ApiMethod.HttpMethod.GET)
    public TestData getTestData() {
        return new TestData("This is a string!", 9.87654321D, 1234567890L, 0.123456789F, 1234567890, true, new Date());
    }

    @ApiMethod(name = "data.echo", path = "data/echo", httpMethod = ApiMethod.HttpMethod.POST)
    public TestData echo(TestData testData) {
        return testData;
    }

}

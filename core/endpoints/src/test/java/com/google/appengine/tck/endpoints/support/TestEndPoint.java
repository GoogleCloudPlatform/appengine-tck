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

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Api(
    name = TestEndPoint.NAME,
    version = TestEndPoint.VERSION)
public class TestEndPoint {

    public static final String NAME = "testEndPoint";
    public static final String VERSION = "v2";

    @ApiMethod(name = "foo.withoutParameters", httpMethod = HttpMethod.GET)
    public EndPointResponse withoutParameters() {
        return new EndPointResponse("method withoutParameters was invoked");
    }

    @ApiMethod(name = "foo.withParameterInQueryString", path = "withParameterInQueryString", httpMethod = HttpMethod.GET)
    public EndPointResponse withParameterInQueryString(@Named("param") String param) {
        return new EndPointResponse("The param was " + param);
    }

    @ApiMethod(name = "foo.withParameterInPath", path = "withParameterInPath/{param}", httpMethod = HttpMethod.GET)
    public EndPointResponse withParameterInPath(@Named("param") String param) {
        return new EndPointResponse("The param was " + param);
    }

    @ApiMethod(name = "foo.post", httpMethod = HttpMethod.POST)
    public EndPointResponse post() {
        return new EndPointResponse("method post was invoked");
    }

    @ApiMethod(name = "foo.put", httpMethod = HttpMethod.PUT)
    public EndPointResponse put() {
        return new EndPointResponse("method put was invoked");
    }

    @ApiMethod(name = "foo.delete", httpMethod = HttpMethod.DELETE)
    public EndPointResponse delete() {
        return new EndPointResponse("method delete was invoked");
    }

}

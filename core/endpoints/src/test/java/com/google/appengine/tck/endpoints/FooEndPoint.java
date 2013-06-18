package com.google.appengine.tck.endpoints;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Api(
    name = FooEndPoint.NAME,
    version = FooEndPoint.VERSION)
public class FooEndPoint {

    public static final String NAME = "zuEndPoint";
    public static final String VERSION = "v1";

    @ApiMethod(name = "foo.withoutParameters", httpMethod = HttpMethod.GET)
    public EndPointResponse withoutParameters() {
        return new EndPointResponse("method withoutParameters was invoked");
    }

    @ApiMethod(name = "foo.withParameterInQueryString", httpMethod = HttpMethod.GET)
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
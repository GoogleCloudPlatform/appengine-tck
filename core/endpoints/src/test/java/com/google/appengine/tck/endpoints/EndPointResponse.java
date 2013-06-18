package com.google.appengine.tck.endpoints;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class EndPointResponse {

    private String response;

    public EndPointResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}

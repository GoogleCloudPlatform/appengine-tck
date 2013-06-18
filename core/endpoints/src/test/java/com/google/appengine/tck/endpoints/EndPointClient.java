package com.google.appengine.tck.endpoints;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class EndPointClient {

    private final URL url;

    public EndPointClient(URL url) {
        this.url = url;
    }

    public String doGet() throws Exception {
        return doRequest(new HttpGet(url.toURI()));
    }

    public String doPost() throws Exception {
        return doRequest(new HttpPost(url.toURI()));
    }

    public String doPut() throws Exception {
        return doRequest(new HttpPut(url.toURI()));
    }

    public String doDelete() throws Exception {
        return doRequest(new HttpDelete(url.toURI()));
    }

    private String doRequest(HttpUriRequest get) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(get);
        return EntityUtils.toString(response.getEntity());
    }
}

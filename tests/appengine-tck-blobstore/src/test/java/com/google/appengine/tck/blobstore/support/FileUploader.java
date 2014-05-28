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

package com.google.appengine.tck.blobstore.support;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FileUploader {
    public static enum Method {
        GET,
        POST
    }

    public String getUploadUrl(URL url) throws URISyntaxException, IOException {
        return getUploadUrl(url, Method.GET);
    }

    public String getUploadUrl(URL url, Method method) throws URISyntaxException, IOException {
        return getUploadUrl(url, method, Collections.<String, String>emptyMap());
    }

    public String getUploadUrl(URL url, Map<String, String> params) throws URISyntaxException, IOException {
        return getUploadUrl(url, Method.POST, params);
    }

    private String getUploadUrl(URL url, Method method, Map<String, String> params) throws URISyntaxException, IOException {
        URIBuilder builder = new URIBuilder(url.toURI());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.addParameter(entry.getKey(), entry.getValue());
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpUriRequest request;
            switch (method) {
                case GET:
                    request = new HttpGet(builder.build());
                    break;
                case POST:
                    request = new HttpPost(builder.build());
                    break;
                default:
                    throw new IllegalArgumentException(String.format("No such method: %s", method));
            }
            HttpResponse response = client.execute(request);
            return EntityUtils.toString(response.getEntity()).trim();
        }
    }

    public String uploadFile(String uri, String partName, String filename, String mimeType, byte[] contents) throws URISyntaxException, IOException {
        return uploadFile(uri, partName, filename, mimeType, contents, 200);
    }

    public String uploadFile(String uri, String partName, String filename, String mimeType, byte[] contents, int expectedResponseCode) throws URISyntaxException, IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(uri);
            ByteArrayBody contentBody = new ByteArrayBody(contents, ContentType.create(mimeType), filename);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart(partName, contentBody);
            post.setEntity(builder.build());
            HttpResponse response = client.execute(post);
            String result = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(String.format("Invalid response code, %s", statusCode), expectedResponseCode, statusCode);
            return result;
        }
    }

    /**
     * This method simulates a HTTP multipart form POST, where the user submits the form without actually selecting a file
     * to upload. Most browsers leave the "filename" part of the content-disposition header empty (they do not omit it
     * completely).
     */
    public String uploadWithoutFile(String uri, String partName) throws URISyntaxException, IOException {
        return uploadFile(uri, partName, "", "application/octet-stream", new byte[0]);
    }

}

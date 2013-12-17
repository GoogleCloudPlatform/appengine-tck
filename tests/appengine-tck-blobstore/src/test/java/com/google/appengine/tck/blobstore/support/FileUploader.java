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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

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
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpUriRequest request;
            switch (method) {
                case GET:
                    request = new HttpGet(url.toURI());
                    break;
                case POST:
                    request = new HttpPost(url.toURI());
                    break;
                default:
                    throw new IllegalArgumentException(String.format("No such method: %s", method));
            }
            HttpResponse response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity()).trim();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public String uploadFile(String uri, String partName, String filename, String mimeType, byte[] contents) throws URISyntaxException, IOException {
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpPost post = new HttpPost(uri);
            MultipartEntity entity = new MultipartEntity();
            ByteArrayBody contentBody = new ByteArrayBody(contents, mimeType, filename);
            entity.addPart(partName, contentBody);
            post.setEntity(entity);

            HttpResponse response = httpClient.execute(post);
            return EntityUtils.toString(response.getEntity());
        } finally {
            httpClient.getConnectionManager().shutdown();
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

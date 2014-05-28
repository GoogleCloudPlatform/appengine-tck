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

package com.google.appengine.tck.blobstore;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.appengine.tck.blobstore.support.FileUploader;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class BlobstoreServeTest extends SimpleBlobstoreTestBase {
    @Test
    @RunAsClient
    public void testUploadedFileHasCorrectContent_upload(@ArquillianResource URL url) throws Exception {
        FileUploader fileUploader = new FileUploader();
        String uploadUrl = fileUploader.getUploadUrl(new URL(url, "getUploadUrl"));
        final String blobKey = fileUploader.uploadFile(uploadUrl, "file", FILENAME, CONTENT_TYPE, UPLOADED_CONTENT);

        final String content = new String(UPLOADED_CONTENT);
        final URI uri = new URL(url, "blobserviceserve?blobKey=" + blobKey).toURI();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            doTest(client, uri, null, null, content);
            doTest(client, uri, Collections.<Header>singleton(new BasicHeader("Range", "bytes=1-3")), null, content.substring(1, 3 + 1));
            doTest(client, uri, null, Collections.singletonMap("blobRange", "2"), content.substring(2));
            doTest(client, uri, null, Collections.singletonMap("blobRangeString", "bytes=2-5"), content.substring(2, 5 + 1));
        }
    }

    private void doTest(HttpClient client, URI uri, Set<Header> headers, Map<String, String> params, String expected) throws Exception {
        URIBuilder builder = new URIBuilder(uri);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        HttpGet get = new HttpGet(builder.build());
        if (headers != null) {
            for (Header h : headers) {
                get.addHeader(h);
            }
        }
        HttpResponse response = client.execute(get);
        Assert.assertEquals(expected, EntityUtils.toString(response.getEntity()));
    }
}

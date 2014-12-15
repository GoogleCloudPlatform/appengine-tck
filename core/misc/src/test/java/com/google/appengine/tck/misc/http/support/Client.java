/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.appengine.tck.misc.http.support;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Client {
    private CloseableHttpClient client;

    public String post(String url) throws Exception {
        HttpPost post = new HttpPost(url);
        HttpResponse response = getClient().execute(post);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        return EntityUtils.toString(response.getEntity());
    }

    protected synchronized HttpClient getClient() {
        if (client == null) {
            client = HttpClients.createDefault();
        }
        return client;
    }

    public void close() throws Exception {
        if (client != null) {
            client.close();
        }
    }
}

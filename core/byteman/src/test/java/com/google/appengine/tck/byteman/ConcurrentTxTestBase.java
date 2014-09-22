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

package com.google.appengine.tck.byteman;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ConcurrentTxTestBase extends BytemanTestBase {
    protected static WebArchive getBaseDeployment() {
        WebArchive war = getBytemanDeployment();
        war.addClasses(ConcurrentTxTestBase.class);
        return war;
    }

    protected static Thread execute(final CloseableHttpClient client, final HttpUriRequest request, final Holder holder) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    try (CloseableHttpResponse response = client.execute(request)) {
                        holder.out = EntityUtils.toString(response.getEntity());
                    }
                } catch (IOException ignore) {
                }
            }
        });
        thread.start();
        return thread;
    }

    protected static void join(Iterable<Thread> threads) throws Exception {
        for (Thread thread : threads) {
            thread.join();
        }
    }

    protected static class Holder {
        String out;

        @Override
        public String toString() {
            return String.valueOf(out);
        }
    }
}

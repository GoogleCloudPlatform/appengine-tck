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

package com.google.appengine.tck.taskqueue;

import java.util.Collections;

import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test options methods, for any akward behavior.
 * They do *not* test runtime behavior.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class TaskOptionsTest extends QueueTestBase {

    @Test
    public void testParams() {
        TaskOptions options = TaskOptions.Builder.withParam("foo", "bar");
        options.removeParam("foo");

        options = TaskOptions.Builder.withParam("foo", "bar".getBytes());
        options.clearParams();
    }

    @Test
    public void testHeaders() {
        TaskOptions options = TaskOptions.Builder.withHeaders(Collections.<String, String>singletonMap("foo", "bar"));
        options.removeHeader("foo");
    }

    @Test
    public void testMisc() throws Exception {
        TaskOptions options = TaskOptions.Builder.withTag("tag".getBytes()).etaMillis(1000L).payload("p").url("/frc");
        Assert.assertEquals("tag", options.getTag());
        Assert.assertArrayEquals("tag".getBytes(), options.getTagAsBytes());
        Assert.assertEquals(Long.valueOf(1000), options.getEtaMillis());
        Assert.assertArrayEquals("p".getBytes(), options.getPayload());
        Assert.assertEquals("/frc", options.getUrl());
    }

    @Test
    public void testWithX() throws Exception {
        TaskOptions.Builder.withCountdownMillis(1000L);
        TaskOptions.Builder.withEtaMillis(1000L);
        TaskOptions.Builder.withPayload("foo", "UTF-8");
        TaskOptions.Builder.withPayload("foo".getBytes(), "UTF-8");
        // retry options
        TaskOptions.Builder.withRetryOptions(RetryOptions.Builder.withMaxBackoffSeconds(10));
        TaskOptions.Builder.withRetryOptions(RetryOptions.Builder.withMaxDoublings(2));
        TaskOptions.Builder.withRetryOptions(RetryOptions.Builder.withMinBackoffSeconds(5));
        TaskOptions.Builder.withRetryOptions(RetryOptions.Builder.withTaskAgeLimitSeconds(15));
    }
}

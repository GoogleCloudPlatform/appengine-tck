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

package com.google.appengine.tck.gcsclient;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Date;

/**
 * Google Cloud Storage client test.
 *
 * @author <a href="mailto:hchen@google.com">Hannah Chen</a>
 */
@RunWith(Arquillian.class)
public class GCSClientTest extends GCSClientTestBase {
    GcsService gcsService = GcsServiceFactory.createGcsService();
    // use default cloud storage buckets
    private static String BUCKET = SystemProperty.applicationId.get() + ".appspot.com";
    private static String OBJECT_NAME = "tckobj" + new Date().getTime();
    private static String CONTENT = "Hello from BigStore - " + new Date();
    private static String MORE_WORDS = "And miles to go before I sleep.";

    @Test
    @InSequence(1)
    public void testCreateGsObj() throws IOException {
        GcsFilename filename = new GcsFilename(BUCKET, OBJECT_NAME);
        GcsFileOptions option = new GcsFileOptions.Builder()
            .mimeType("text/html")
            .acl("public-read")
            .build();
        GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, option);
        PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
        out.println(CONTENT);
        out.flush();

        writeChannel.waitForOutstandingWrites();
        writeChannel.write(ByteBuffer.wrap(MORE_WORDS.getBytes()));
        writeChannel.close();
        assertEquals(BUCKET, filename.getBucketName());
        assertEquals(OBJECT_NAME, filename.getObjectName());
    }

    @Test
    @InSequence(2)
    public void testReadGsObj() throws FileNotFoundException, IOException {
        GcsFilename filename = new GcsFilename(BUCKET, OBJECT_NAME);
        GcsInputChannel readChannel = gcsService.openReadChannel(filename, 0);
        BufferedReader reader = new BufferedReader(Channels.newReader(readChannel, "UTF8"));
        String line;
        String objContent = "";
        while ((line = reader.readLine()) != null) {
            objContent = objContent + line;
        }
        readChannel.close();
        assertTrue(objContent.indexOf(CONTENT) == 0);
        assertTrue(objContent.indexOf(MORE_WORDS) > 0);
    }

    @Test
    @InSequence(3)
    public void testDelGsObj() throws IOException {
        GcsFilename filename = new GcsFilename(BUCKET, OBJECT_NAME);
        assertTrue(gcsService.delete(filename));
    }
}

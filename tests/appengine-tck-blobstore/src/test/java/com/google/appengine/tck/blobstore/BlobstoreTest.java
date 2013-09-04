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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class BlobstoreTest extends BlobstoreTestBase {

    @Test
    @RunAsClient
    public void testBlobServedWhenResponseContainsBlobKeyHeader(@ArquillianResource URL url) throws Exception {
        String MIME_TYPE = "foo/bar";
        String CONTENTS = "foobar";
        URL pageUrl = new URL(url, "serveblob?name=testblob.txt&mimeType=" + MIME_TYPE + "&contents=" + CONTENTS);

        HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
        try {
            String response = readFullyAndClose(connection.getInputStream());

            assertEquals(MIME_TYPE, connection.getContentType());
            assertEquals(CONTENTS, response);
            assertNull("header should have been removed from response", connection.getHeaderField("X-AppEngine-BlobKey"));
        } finally {
            connection.disconnect();
        }
    }

    @Test
    @RunAsClient
    public void testOnlyPartOfBlobServedWhenResponseContainsBlobRangeHeader(@ArquillianResource URL url) throws Exception {
        String CONTENTS = "abcdefghijklmnopqrstuvwxyz";
        URL pageUrl = new URL(url, "serveblob?name=testrange.txt&mimeType=text/plain&contents=" + CONTENTS + "&blobRange=bytes=2-5");

        HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
        try {
            String response = readFullyAndClose(connection.getInputStream());

            int PARTIAL_CONTENT = 206;
            assertEquals(PARTIAL_CONTENT, connection.getResponseCode());
            assertEquals("bytes 2-5/26", connection.getHeaderField("Content-Range"));
            assertEquals(CONTENTS.substring(2, 5 + 1), response);
            assertNull("header should have been removed from response", connection.getHeaderField("X-AppEngine-BlobRange"));

        } finally {
            connection.disconnect();
        }
    }

    @Test
    @RunAsClient
    public void testBlobRangeEndGreaterThanContentSize(@ArquillianResource URL url) throws Exception {
        String CONTENTS = "abcdefghijklmnopqrstuvwxyz";
        URL pageUrl = new URL(url, "serveblob?name=testrangeend.txt&mimeType=text/plain&contents=" + CONTENTS + "&blobRange=bytes=2-1000");

        HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
        try {
            String response = readFullyAndClose(connection.getInputStream());

            int PARTIAL_CONTENT = 206;
            assertEquals(PARTIAL_CONTENT, connection.getResponseCode());
            assertEquals("bytes 2-25/26", connection.getHeaderField("Content-Range"));
            assertEquals(CONTENTS.substring(2), response);
        } finally {
            connection.disconnect();
        }
    }

    @Test
    @RunAsClient
    public void testRequestedRangeNotSatisfiableWhenBlobRangeHeaderIsInvalid(@ArquillianResource URL url) throws Exception {
        int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "invalidBlobRange"));
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "bytes="));
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "bytes=1000-0"));
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "bytes=-1-5"));
        assertServletReturnsResponseCode(REQUESTED_RANGE_NOT_SATISFIABLE, urlWithBlobRange(url, "bytes=1000-2000"));
    }

    private void assertServletReturnsResponseCode(int responseCode, URL pageUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
        try {
            assertEquals(pageUrl.toString(), responseCode, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    private URL urlWithBlobRange(URL url, String blobRange) throws MalformedURLException {
        String contents = "whatever";
        return new URL(url, "serveblob?name=testinvalidrange.txt&mimeType=text/plain&contents=" + contents + "&blobRange=" + blobRange);
    }

    private String readFullyAndClose(InputStream in) throws IOException {
        try {
            StringBuilder sbuf = new StringBuilder();
            int ch;
            while ((ch = in.read()) != -1) {
                sbuf.append((char) ch);
            }
            return sbuf.toString();
        } finally {
            in.close();
        }
    }

}

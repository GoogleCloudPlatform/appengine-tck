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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.tck.blobstore.support.IOUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class BlobInputStreamTest extends SimpleBlobstoreTestBase {

    @Test
    public void testBlobInputStream() throws Exception {
        String CONTENT = "BlobInputStreamTest";
        BlobKey blobKey = writeNewBlobFile(CONTENT);

        BlobstoreInputStream stream = new BlobstoreInputStream(blobKey);
        assertEquals(CONTENT, toString(stream));
    }

    @Test
    public void testBlobInputStreamWithOffset() throws Exception {
        BlobKey blobKey = writeNewBlobFile("BlobInputStreamTest");

        int OFFSET = 4;
        BlobstoreInputStream stream = new BlobstoreInputStream(blobKey, OFFSET);
        assertEquals("InputStreamTest", toString(stream));
    }

    private String toString(BlobstoreInputStream in) throws IOException {
        byte[] contents = IOUtils.toBytes(in, true);
        return new String(contents);
    }

}

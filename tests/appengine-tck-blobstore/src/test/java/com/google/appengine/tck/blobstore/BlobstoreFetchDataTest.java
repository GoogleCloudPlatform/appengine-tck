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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class BlobstoreFetchDataTest extends SimpleBlobstoreTestBase {

    private BlobstoreService blobstore;
    private BlobKey blobKey;

    @Before
    public void setUp() throws Exception {
        blobstore = BlobstoreServiceFactory.getBlobstoreService();
        blobKey = writeNewBlobFile("Uploaded text");
    }

    @After
    public void tearDown() {
        blobstore.delete(blobKey);
    }

    @Test
    public void testFetchDataHandlesIndexesCorrectly() {
        assertEquals("Uploaded text", new String(blobstore.fetchData(blobKey, 0, 100)));
        assertEquals("Upload", new String(blobstore.fetchData(blobKey, 0, 5)));
        assertEquals("loaded", new String(blobstore.fetchData(blobKey, 2, 7)));
    }

    @Test
    public void testFetchDataThrowsIAEWhenBlobKeyDoesNotExist() {
        assertFetchDataThrowsIAE(new BlobKey("nonexistent"), 0, 10);
    }

    @Test
    public void testFetchDataThrowsIAEWhenIndexNegative() {
        assertFetchDataThrowsIAE(blobKey, -1, 10);
        assertFetchDataThrowsIAE(blobKey, 0, -10);
    }

    @Test
    public void testFetchDataThrowsIAEWhenEndIndexLessThanStartIndex() {
        assertFetchDataThrowsIAE(blobKey, 10, 5);
    }

    @Test
    public void testFetchDataHonorsMaxBlobFetchSize() throws Exception {
        // NOTE: endIndex is inclusive, so we're actually fetching (endIndex - startIndex + 1) bytes
        assertEquals("Uploaded text", new String(blobstore.fetchData(blobKey, 0, BlobstoreService.MAX_BLOB_FETCH_SIZE - 1)));
        assertFetchDataThrowsIAE(blobKey, 0, BlobstoreService.MAX_BLOB_FETCH_SIZE);
    }

    private void assertFetchDataThrowsIAE(BlobKey blobKey, int startIndex, int endIndex) {
        try {
            blobstore.fetchData(blobKey, startIndex, endIndex);
            fail("Expected IllegalArgumentException when invoking fetchData(blobKey, " + startIndex + ", " + endIndex + ")");
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

}

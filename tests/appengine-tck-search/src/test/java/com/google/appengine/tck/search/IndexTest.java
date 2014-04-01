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

package com.google.appengine.tck.search;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.Schema;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:hchen@google.com">Hannah Chen</a>
 */
@RunWith(Arquillian.class)
public class IndexTest extends SearchTestBase {

    @Test
    public void testGetIndexes() throws InterruptedException, ParseException {
        String indexName = "indextest";
        addData(indexName);
        GetIndexesRequest request = GetIndexesRequest.newBuilder()
            .setIndexNamePrefix(indexName)
            .setOffset(0)
            .setLimit(10)
            .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();
        assertEquals(2, listIndexes.size());

        for (Index oneIndex : listIndexes) {
            String name = oneIndex.getName();
            assertTrue(name.startsWith(indexName));
            verifyDocCount(oneIndex, -1);
        }
    }

    @Test
    public void testNamespaceWithBug() throws InterruptedException, ParseException {
        String ns = "ns-indextest";
        String indexName = "ns-index";
        int docCount = 5;
        NamespaceManager.set(ns);
        SearchService searchService2 = SearchServiceFactory.getSearchService();
        Index index = searchService2.getIndex(IndexSpec.newBuilder()
                .setName(indexName)
                .build());
        delDocs(index);
        addDocs(index, docCount);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
            .setIndexNamePrefix(indexName)
            .setOffset(0)
            .setNamespace(ns)
            .setLimit(10)
            .build();
        assertEquals(ns, request.getNamespace());
        GetResponse<Index> response = searchService2.getIndexes(request);
        List<Index> listIndexes = response.getResults();
        for (Index oneIndex : listIndexes) {
            assertEquals(ns, listIndexes.get(0).getNamespace());
            assertEquals(indexName, listIndexes.get(0).getName());
            verifyDocCount(oneIndex, docCount);
        }
        assertEquals(ns, searchService2.getNamespace());
        NamespaceManager.set("");
    }

    //@Test   Before the namespace setting bug get fixed, run testNamespaceWithBug
    public void testNamespace() throws InterruptedException, ParseException {
        String ns = "ns-indextest";
        String indexName = "ns-index";
        int docCount = 5;
        NamespaceManager.set(ns);
        Index index = searchService.getIndex(IndexSpec.newBuilder()
                .setName(indexName)
                .build());
        delDocs(index);
        addDocs(index, docCount);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
            .setIndexNamePrefix(indexName)
            .setOffset(0)
            .setNamespace(ns)
            .setLimit(10)
            .build();
        assertEquals(ns, request.getNamespace());
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();
        for (Index oneIndex : listIndexes) {
            assertEquals(ns, listIndexes.get(0).getNamespace());
            assertEquals(indexName, listIndexes.get(0).getName());
            verifyDocCount(oneIndex, docCount);
        }
        assertEquals(ns, searchService.getNamespace());
        NamespaceManager.set("");
    }

    @Test
    public void testSchemas() throws InterruptedException, ParseException {
        String[] fields = {"textfield", "numfield", "datefield", "htmlfield", "atomfield", "geofield"};
        Field.FieldType[] fieldTypes = {Field.FieldType.TEXT, Field.FieldType.NUMBER,
                                        Field.FieldType.DATE, Field.FieldType.HTML,
                                        Field.FieldType.ATOM, Field.FieldType.GEO_POINT};
        String indexName = "indextest";
        addData(indexName);
        GetIndexesRequest.Builder builder = GetIndexesRequest.newBuilder()
            .setIndexNamePrefix(indexName)
            .setSchemaFetched(true);
        GetResponse<Index> response = searchService.getIndexes(builder);
        for (Index index : response) {
            Schema schema = index.getSchema();
            for (int i = 0; i < fields.length; i++) {
                List<Field.FieldType> typesForField = schema.getFieldTypes(fields[i]);
                assertEquals(1, typesForField.size());
                assertEquals(fieldTypes[i], typesForField.get(0));
            }
            List<Field.FieldType> typesForField = schema.getFieldTypes("mixfield");
            assertEquals(2, typesForField.size());
            for (Field.FieldType fieldType : typesForField) {
                assertTrue((fieldType == Field.FieldType.TEXT) ||
                           (fieldType == Field.FieldType.NUMBER));
            }
        }
    }

    @Test
    public void testPutDeleteDocs() throws InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        List<String> docIdList = new ArrayList<>();
        Index index = searchService.getIndex(IndexSpec.newBuilder()
            .setName(indexName)
            .build());

        Field field = Field.newBuilder().setName("subject").setText("put(Document.Builder)").build();
        Document.Builder docBuilder = Document.newBuilder()
            .setId(docId + "1")
            .addField(field);
        index.put(docBuilder);
        docIdList.add(docId + "1");

        field = Field.newBuilder().setName("subject").setText("put(Document)").build();
        Document document = Document.newBuilder()
            .setId(docId + "2")
            .addField(field).build();
        index.put(document);
        docIdList.add(docId + "1");

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
            .setIndexNamePrefix(indexName)
            .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();
        for (Index oneIndex : listIndexes) {
            Field retField = oneIndex.get(docId + "1").getOnlyField("subject");
            assertEquals("put(Document.Builder)", retField.getText());
            retField = oneIndex.get(docId + "2").getOnlyField("subject");
            assertEquals("put(Document)", retField.getText());
            oneIndex.delete(docIdList.get(0));
            sync();
            assertNull(oneIndex.get(docIdList.get(0)));
        }
    }

    @Test
    public void testPutDeleteAsyncDocsByString() throws InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        List<String> docIdList = new ArrayList<>();
        Index index = searchService.getIndex(IndexSpec.newBuilder()
                .setName(indexName)
                .build());

        Field field = Field.newBuilder().setName("subject").setText("put(Document.Builder)").build();
        Document.Builder docBuilder = Document.newBuilder()
                .setId(docId + "1")
                .addField(field);
        index.put(docBuilder);
        docIdList.add(docId + "1");

        field = Field.newBuilder().setName("subject").setText("put(Document)").build();
        Document document = Document.newBuilder()
                .setId(docId + "2")
                .addField(field).build();
        index.put(document);
        docIdList.add(docId + "1");

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();
        for (Index oneIndex : listIndexes) {
            Field retField = oneIndex.get(docId + "1").getOnlyField("subject");
            assertEquals("put(Document.Builder)", retField.getText());
            retField = oneIndex.get(docId + "2").getOnlyField("subject");
            assertEquals("put(Document)", retField.getText());
            oneIndex.deleteAsync(docIdList.get(0));
            sync();
            assertNull(oneIndex.get(docIdList.get(0)));
        }
    }

    @Test
    public void testPutDeleteAsyncDocsByIterable() throws InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        Index index = createIndex(indexName, docId);

        List<String> dList = new ArrayList<>();
        Results<ScoredDocument> found = searchDocs(index, "", 0);
        Iterator<ScoredDocument> it = found.iterator();

        ScoredDocument doc = it.next();
        assertEquals(docId + "1", doc.getId());
        dList.add(doc.getId());

        doc = it.next();
        assertEquals(docId + "2", doc.getId());
        dList.add(doc.getId());

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            oneIndex.deleteAsync(dList);
            sync();
            verifyDocCount(index, 0);
        }
    }

    @Test
    public void testPutGetRangeGetRequest() throws InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        Index index = createIndex(indexName, docId);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            GetResponse<Document> docs = oneIndex.getRange(GetRequest.newBuilder().setStartId(docId + "1").setLimit(10).build());
            sync();
            assertEquals(docs.getResults().size(), 2);
        }
    }

    @Test
    public void testPutGetRangeBuilder() throws InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        Index index = createIndex(indexName, docId);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            GetResponse<Document> docs = oneIndex.getRange(GetRequest.newBuilder().setStartId(docId + "1").setLimit(10));
            sync();
            assertEquals(docs.getResults().size(), 2);
        }
    }

    @Test
    public void testPutGetRangeAsyncGetResponse() throws InterruptedException, ExecutionException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        Index index = createIndex(indexName, docId);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            Future<GetResponse<Document>> futurDocs = oneIndex.getRangeAsync(GetRequest.newBuilder().setStartId(docId + "1").setLimit(10).build());
            sync();
            assertEquals(futurDocs.get().getResults().size(), 2);
        }
    }

    @Test
    public void testPutGetRangeAsyncBuilder() throws InterruptedException, ExecutionException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        Index index = createIndex(indexName, docId);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            Future<GetResponse<Document>> futurDocs = oneIndex.getRangeAsync(GetRequest.newBuilder().setStartId(docId + "1").setLimit(10));
            sync();
            assertEquals(futurDocs.get().getResults().size(), 2);
        }
    }

    @Test
    public void testPutAsyncBuilder() {
        String indexName = "put-index";
        String docId = "testPutDocs";

        Index index = searchService.getIndex(IndexSpec.newBuilder()
                .setName(indexName)
                .build());

        Field field = Field.newBuilder().setName("subject").setText("put(Document.Builder)").build();
        Document.Builder docBuilder = Document.newBuilder()
                .setId(docId + "1")
                .addField(field);

        index.putAsync(docBuilder);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            Field retField = oneIndex.get(docId + "1").getOnlyField("subject");
            sync();
            assertEquals("put(Document.Builder)", retField.getText());
        }
    }

    @Test
    public void testPutAsyncDocument() {
        String indexName = "put-index";
        String docId = "testPutDocs";

        Index index = searchService.getIndex(IndexSpec.newBuilder()
                .setName(indexName)
                .build());

        Field field = Field.newBuilder().setName("subject").setText("put(Document)").build();
        Document document = Document.newBuilder()
                .setId(docId + "1")
                .addField(field).build();

        Future<PutResponse> resp = index.putAsync(document);

        while (!resp.isDone()) {
            if (resp.isCancelled()) {
                break;
            }
        }

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            Field retField = oneIndex.get(docId + "1").getOnlyField("subject");
            sync();
            assertEquals("put(Document)", retField.getText());
        }
    }

    @Test
    public void testPutAsyncIterable() {
        String indexName = "put-index";
        String docId = "testPutDocs";

        Index index = searchService.getIndex(IndexSpec.newBuilder()
                .setName(indexName)
                .build());

        List<Document> documents = new ArrayList<>();
        Field field = Field.newBuilder().setName("subject").setText("put(Document)").build();
        Document document1 = Document.newBuilder()
                .setId(docId + "1")
                .addField(field).build();
        field = Field.newBuilder().setName("subject").setText("put(Document)").build();
        Document document2 = Document.newBuilder()
                .setId(docId + "2")
                .addField(field).build();

        documents.add(document1);
        documents.add(document2);

        Future<PutResponse> resp = index.putAsync(documents);

        while (!resp.isDone()) {
            if (resp.isCancelled()) {
                break;
            }
        }

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            Field retField = oneIndex.get(docId + "1").getOnlyField("subject");
            assertEquals("put(Document)", retField.getText());
            retField = oneIndex.get(docId + "2").getOnlyField("subject");
            assertEquals("put(Document)", retField.getText());
            sync();
        }
    }

    private Index createIndex(String indexName, String docId) {
        Index index = searchService.getIndex(IndexSpec.newBuilder()
                .setName(indexName)
                .build());

        Field field = Field.newBuilder().setName("subject").setText("put(Document.Builder)").build();
        Document.Builder docBuilder = Document.newBuilder()
                .setId(docId + "1")
                .addField(field);
        index.put(docBuilder);

        field = Field.newBuilder().setName("subject").setText("put(Document)").build();
        Document document = Document.newBuilder()
                .setId(docId + "2")
                .addField(field).build();
        index.put(document);
        return index;
    }

    private void addData(String indexName) throws InterruptedException, ParseException {
        Index index;
        index = searchService.getIndex(IndexSpec.newBuilder().setName(indexName + "3"));
        delDocs(index);
        addDocs(index, 3);
        index = searchService.getIndex(IndexSpec.newBuilder().setName(indexName + "7"));
        delDocs(index);
        addDocs(index, 7);
    }

    private void verifyDocCount(Index index, int docCount) {
        if (docCount == -1) {
            String name = index.getName();
            docCount = Integer.valueOf(name.substring(name.length() - 1));
        }
        List<Document> docList = index.getRange(GetRequest.newBuilder().build()).getResults();
        assertEquals(docCount, docList.size());
    }
}

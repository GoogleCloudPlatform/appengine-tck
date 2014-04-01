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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceConfig;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:hchen@google.com">Hannah Chen</a>
 */
@RunWith(Arquillian.class)
public class SearchServiceTest extends SearchTestBase {
    private Index index;

    @Before
    public void addData() throws ParseException, InterruptedException {
        super.setUp();
        index = searchService.getIndex(IndexSpec.newBuilder().setName("appenginetck"));
        delDocs(index);
        addDocs(index, 3);
    }

    @Test
    public void testSimpleSearch() {
        Results<ScoredDocument> result = searchDocs(index, "", 0);
        assertEquals(3, result.getNumberFound());

        result = searchDocs(index, "", 2);
        assertEquals(3, result.getNumberFound());
        assertEquals(2, result.getNumberReturned());

        result = searchDocs(index, "text with num", 0);
        assertEquals(3, result.getNumberFound());

        result = searchDocs(index, "text with num 0", 0);
        assertEquals(1, result.getNumberFound());
    }

    @Test
    public void testFieldSearch() {
        Results<ScoredDocument> result = searchDocs(index, "textfield:text with num", 0);
        assertEquals(3, result.getNumberFound());

        result = searchDocs(index, "textfield:text with num 0", 0);
        assertEquals(1, result.getNumberFound());

        result = searchDocs(index, "numfield:0", 0);
        assertEquals(1, result.getNumberFound());

        result = searchDocs(index, "numfield:-1", 0);
        assertEquals(0, result.getNumberFound());
    }

    @Test(expected = IllegalArgumentException.class)
    public void  testMaxQueryLimit() {
        String q = "";
        for (int i = 0; i < 57; i++) {
            String one = "(searchproperty" + i + ":" + "1234567890" + i + ")";
            if (q.length() == 0) {
                q = one;
            } else {
                q += " AND " + one;
            }
            if (q.length() > 2000) {
                break;
            }
        }
        searchDocs(index, q, 0);
    }

    @Test
    public void  testGetField() {
        String namelist = "geofield; textfield; numfield; atomfield; datefield; htmlfield; mixfield";
        Results<ScoredDocument> result = searchDocs(index, "", 0);
        for (ScoredDocument doc : result) {
            assertEquals(2, doc.getFieldCount("textfield"));
            String id = doc.getId();
            assertNotNull(id);
            if (id.endsWith("0")) {
                assertEquals(new Locale("cn"), doc.getLocale());
            } else {
                assertEquals(new Locale("en"), doc.getLocale());
            }

            for (String name : doc.getFieldNames()) {
                assertTrue(namelist.contains(name));
            }

            Iterator<Field> fields = doc.getFields().iterator();
            int count = 0;
            for ( ; fields.hasNext() ; ++count ) {
                fields.next();
            }
            assertEquals(9, count);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void  testGetOnlyField() {
        Results<ScoredDocument> result = searchDocs(index, "text with num 0", 0);
        for (ScoredDocument doc : result) {
            @SuppressWarnings("UnusedDeclaration")
            Field field = doc.getOnlyField("textfield");
        }
    }

    @Test
    public void testLocale() {
        Results<ScoredDocument> result = searchDocs(index, "", 0);
        for (ScoredDocument doc : result) {
            String id = doc.getId();
            if (id.endsWith("0")) {
                assertEquals(new Locale("cn"), doc.getLocale());
            } else {
                assertEquals(new Locale("en"), doc.getLocale());
            }

            boolean found = false;
            for (Field textField : doc.getFields("textfield")) {
                Locale locale = textField.getLocale();
                if ((locale != null) && locale.equals(Locale.FRENCH)) {
                    found = true;
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void  testSearchOperation() {
        Results<ScoredDocument> result = searchDocs(index, "NOT text", 0);
        assertEquals(0, result.getNumberFound());

        result = searchDocs(index, "0 OR 1", 0);
        assertEquals(2, result.getNumberFound());
    }

    @Test
    public void testSortOptions() {
        for (SortExpression.SortDirection direction : SortExpression.SortDirection.values()) {
            SortExpression sortExpression = SortExpression.newBuilder()
                .setExpression("numfield")
                .setDirection(direction)
                .setDefaultValueNumeric(9999)
                .build();
            SortOptions sortOptions = SortOptions.newBuilder()
                .addSortExpression(sortExpression)
                .build();
            QueryOptions options = QueryOptions.newBuilder()
                .setFieldsToReturn("numfield", "datefield")
                .setSortOptions(sortOptions)
                .build();
            Query.Builder queryBuilder = Query.newBuilder().setOptions(options);
            Results<ScoredDocument> results = index.search(queryBuilder.build(""));

            double pre = -9999;
            if (direction.equals(SortExpression.SortDirection.DESCENDING)) {
                pre = 9999;
            }
            for (ScoredDocument doc : results) {
                assertEquals(2, doc.getFieldNames().size());
                for (Field numField : doc.getFields("numfield")) {
                    if (direction.equals(SortExpression.SortDirection.DESCENDING)) {
                        assertTrue(pre > numField.getNumber());
                    } else {
                        assertTrue(pre < numField.getNumber());
                    }
                    pre = numField.getNumber();
                }
            }
        }
    }

    @Test
    public void testSearchAsyncQuery() throws ExecutionException, InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        Index index = createIndex(indexName, docId);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            QueryOptions.Builder optionBuilder = QueryOptions.newBuilder();
            optionBuilder.setLimit(10);
            Query.Builder queryBuilder = Query.newBuilder().setOptions(optionBuilder.build());
            Future<Results<ScoredDocument>> Fres = oneIndex.searchAsync(queryBuilder.build(""));

            Iterator<ScoredDocument> it = Fres.get().iterator();
            assertEquals(docId + "1", it.next().getId());
            assertEquals(docId + "2", it.next().getId());
            sync();
        }
    }

    @Test
    public void testGetIndexesAsyncBuilder() throws ExecutionException, InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        Index index = createIndex(indexName, docId);

        GetIndexesRequest.Builder builder = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName);

        Future<GetResponse<Index>> response = searchService.getIndexesAsync(builder);
        List<Index> listIndexes = response.get().getResults();

        for (Index oneIndex : listIndexes) {
            Future<Results<ScoredDocument>> Fres = oneIndex.searchAsync("");

            Iterator<ScoredDocument> it = Fres.get().iterator();
            assertEquals(docId + "1", it.next().getId());
            assertEquals(docId + "2", it.next().getId());
            sync();
        }
    }

    @Test
    public void testGetIndexesAsyncRequest() throws ExecutionException, InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        Index index = createIndex(indexName, docId);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();

        Future<GetResponse<Index>> response = searchService.getIndexesAsync(request);
        List<Index> listIndexes = response.get().getResults();

        for (Index oneIndex : listIndexes) {
            Future<Results<ScoredDocument>> Fres = oneIndex.searchAsync("");

            Iterator<ScoredDocument> it = Fres.get().iterator();
            assertEquals(docId + "1", it.next().getId());
            assertEquals(docId + "2", it.next().getId());
            sync();
        }
    }

    @Test
    public void testSearchAsyncString() throws ExecutionException, InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        Index index = createIndex(indexName, docId);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            Future<Results<ScoredDocument>> rRes = oneIndex.searchAsync("");

            Iterator<ScoredDocument> it = rRes.get().iterator();
            assertEquals(docId + "1", it.next().getId());
            assertEquals(docId + "2", it.next().getId());
            sync();
        }
    }

    @Test
    public void testSearchServiceConfig() throws ExecutionException, InterruptedException {
        String indexName = "put-index";
        String docId = "testPutDocs";
        searchService = SearchServiceFactory.getSearchService(SearchServiceConfig.newBuilder().setDeadline(10.).build());

        Index index = createIndex(indexName, docId);

        GetIndexesRequest request = GetIndexesRequest.newBuilder()
                .setIndexNamePrefix(indexName)
                .build();
        GetResponse<Index> response = searchService.getIndexes(request);
        List<Index> listIndexes = response.getResults();

        for (Index oneIndex : listIndexes) {
            Results<ScoredDocument> res = oneIndex.search("");

            Iterator<ScoredDocument> it = res.iterator();
            assertEquals(docId + "1", it.next().getId());
            assertEquals(docId + "2", it.next().getId());
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
}

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Document.Builder;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;


/**
 * @author <a href="mailto:hchen@google.com">Hannah Chen</a>
 */
public abstract class SearchTestBase extends TestBase {
    protected SearchService searchService;

    @Before
    public void setUp() {
        searchService = SearchServiceFactory.getSearchService();
    }

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getTckDeployment();
        war.addClass(SearchTestBase.class);
        return war;
    }

    protected void delDocs(Index index) throws InterruptedException {
        List<String> dList = new ArrayList<>();
        Results<ScoredDocument> found = searchDocs(index, "", 0);
        for (ScoredDocument document : found) {
            dList.add(document.getId());
        }
        index.delete(dList);
        sync();
    }

    protected void addDocs(Index index, int docCount) throws ParseException, InterruptedException {
        if (searchDocs(index, "", 0).getNumberFound() == 0) {
            List<Document> documents = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            DateFormat dfDate = new SimpleDateFormat("yyyy,M,d");
            for (int i = 0; i < docCount; i++) {
                Builder docBuilder = Document.newBuilder();
                // two text field with different locale
                docBuilder.addField(Field.newBuilder().setName("textfield").setText("text with num " + i));
                Field field = Field.newBuilder().setName("textfield").setText("C'est la vie " + i).setLocale(Locale.FRENCH).build();
                docBuilder.addField(field);
                docBuilder.addField(Field.newBuilder().setName("numfield").setNumber(i));
                String dateVal = "" + cal.get(Calendar.YEAR) + ",";
                dateVal += cal.get(Calendar.MONTH) + ",";
                int day = cal.get(Calendar.DATE) + i;
                dateVal += day;
                docBuilder.addField(Field.newBuilder().setName("datefield").setDate(dfDate.parse(dateVal)));
                docBuilder.addField(Field.newBuilder().setName("htmlfield").setHTML("<B>html</B> " + i));
                docBuilder.addField(Field.newBuilder().setName("atomfield").setAtom("atom" + i + ".com"));
                GeoPoint geoPoint = new GeoPoint((double) i, (double) (100 + i));
                docBuilder.addField(Field.newBuilder().setName("geofield").setGeoPoint(geoPoint));
                // two field in same name and with different field type
                docBuilder.addField(Field.newBuilder().setName("mixfield").setText("text and number mix field"));
                docBuilder.addField(Field.newBuilder().setName("mixfield").setNumber(987));
                docBuilder.setId("selfid" + i);
                // only doc(id="selfid0") has "cn" locale, others have "en" locale
                if (i == 0) {
                    docBuilder.setLocale(new Locale("cn"));
                } else {
                    docBuilder.setLocale(new Locale("en"));
                }
                documents.add(docBuilder.build());
            }
            index.put(documents);
            sync();
        }
    }

    protected Results<ScoredDocument> searchDocs(Index index, String query, int limit) {
        if (limit > 0) {
            QueryOptions.Builder optionBuilder = QueryOptions.newBuilder();
            optionBuilder.setLimit(limit);
            Query.Builder queryBuilder = Query.newBuilder().setOptions(optionBuilder.build());
            return index.search(queryBuilder.build(query));
        } else {
            return index.search(query);
        }
    }
}

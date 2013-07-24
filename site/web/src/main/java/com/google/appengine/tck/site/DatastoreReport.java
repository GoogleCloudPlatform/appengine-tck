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

package com.google.appengine.tck.site;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DatastoreReport implements Report {
    private String buildType;
    private Entity data;

    public DatastoreReport(String buildType) throws Exception {
        this.buildType = buildType;
    }

    public boolean hasData(DatastoreService ds) throws Exception {
        Query query = new Query(DatastoreReport.class.getSimpleName());
        query.addSort("buildId", Query.SortDirection.DESCENDING);
        query.setFilter(new Query.FilterPredicate("buildType", Query.FilterOperator.EQUAL, buildType));

        PreparedQuery pq = ds.prepare(query);
        QueryResultList<Entity> list = pq.asQueryResultList(FetchOptions.Builder.withLimit(1));

        if (list.size() > 0) {
            data = list.get(0);
        }

        return (list.size() > 0);
    }

    private int getInt(String property) {
        Object value = data.getProperty(property);
        return Number.class.cast(value).intValue();
    }

    public int getFailedTests() throws Exception {
        return getInt("failedTests");
    }

    public int getPassedTests() throws Exception {
        return getInt("passedTests");
    }

    public int getIgnoredTests() throws Exception {
        return getInt("ignoredTests");
    }

    @SuppressWarnings("unchecked")
    public List<Test> getListOfFailedTests() throws Exception {
        List<String> list = (List<String>) data.getProperty("failedTestsList");
        List<Test> tests = new ArrayList<Test>();
        for (String elt : list) {
            String[] split = elt.split("_#_");
            Test test = new Test();
            test.setClassName(split[0]);
            test.setMethodName(split[1]);
            test.setError(split[2]);
            tests.add(test);
        }
        return tests;
    }

    public String getFailedTestError(Test test) throws Exception {
        return test.getError();
    }
}

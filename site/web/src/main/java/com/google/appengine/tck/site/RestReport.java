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

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import org.json.JSONObject;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class RestReport implements Report {
    private int buildId;
    private JSONObject stats;

    public RestReport(String buildType) throws Exception {
        buildId = RestUtils.getLatestBuild(buildType);
    }

    public boolean hasData(DatastoreService ds) throws Exception {
        if (buildId > 0) {
            stats = RestUtils.getStats(buildId);
        }
        return (buildId > 0);
    }

    public int getFailedTests() throws Exception {
        return RestUtils.getFailedTests(stats);
    }

    public int getPassedTests() throws Exception {
        return RestUtils.getPassedTests(stats);
    }

    public int getIgnoredTests() throws Exception {
        return RestUtils.getIgnoredTests(stats);
    }

    public List<Test> getListOfFailedTests() throws Exception {
        return RestUtils.getListOfFailedTests(buildId);
    }

    public String getFailedTestError(Test test) throws Exception {
        return RestUtils.getFailedTestError(test);
    }
}

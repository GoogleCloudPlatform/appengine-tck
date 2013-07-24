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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class RestUtils {
    private static final String TCK_URL = "http://173.255.112.175";

    protected static InputStream getInputStream(String urlString) throws Exception {
        URL url = new URL(urlString);
        HTTPRequest request = new HTTPRequest(url, HTTPMethod.GET);
        request.addHeader(new HTTPHeader("Accept", "application/json"));
        URLFetchService service = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse response = service.fetch(request);
        return new ByteArrayInputStream(response.getContent());
    }

    protected static JSONObject readJSON(InputStream inputStream) throws Exception {
        try {
            return new JSONObject(new JSONTokener(new InputStreamReader(inputStream)));
        } finally {
            inputStream.close();
        }
    }

    protected static JSONObject getJSON(String url) throws Exception {
        return readJSON(getInputStream(url));
    }

    protected static int getStat(JSONObject stats, String key) throws Exception {
        JSONArray array = stats.getJSONArray("property");
        for (int i = 0; i < array.length(); i++) {
            JSONObject jo = array.getJSONObject(i);
            String name = jo.getString("name");
            if (key.equals(name)) {
                return jo.getInt("value");
            }
        }
        return -1;
    }

    protected static JSONObject getBuilds(String buildType) throws Exception {
        String url = String.format(TCK_URL + "/guestAuth/app/rest/builds/?locator=buildType:id:%s", buildType);
        return getJSON(url);
    }

    protected static JSONObject getStats(int buildId) throws Exception {
        String url = String.format(TCK_URL + "/guestAuth/app/rest/builds/id:%s/statistics", buildId);
        return getJSON(url);
    }

    public static int getLatestBuild(String buildType) throws Exception {
        JSONObject builds = getBuilds(buildType);
        JSONArray array = builds.getJSONArray("build");
        return (array.length() == 0) ? -1 : array.getJSONObject(0).getInt("id");
    }

    public static int getAllTests(JSONObject stats) throws Exception {
        return getFailedTests(stats) + getPassedTests(stats) + getIgnoredTests(stats);
    }

    public static int getFailedTests(JSONObject stats) throws Exception {
        return getStat(stats, "FailedTestCount");
    }

    public static int getPassedTests(JSONObject stats) throws Exception {
        return getStat(stats, "PassedTestCount");
    }

    public static int getIgnoredTests(JSONObject stats) throws Exception {
        return getStat(stats, "IgnoredTestCount");
    }

    public static List<Test> getListOfFailedTests(int buildId) throws Exception {
        return null;
    }

    public static String getFailedTestError(Test test) throws Exception {
        return null;
    }
}

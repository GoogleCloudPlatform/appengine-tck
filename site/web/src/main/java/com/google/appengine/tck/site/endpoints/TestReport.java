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

package com.google.appengine.tck.site.endpoints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import static com.google.appengine.api.datastore.Query.Filter;
import static com.google.appengine.api.datastore.Query.FilterOperator;
import static com.google.appengine.api.datastore.Query.SortDirection.DESCENDING;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a build test report.
 *
 * @author <a href="mailto:kevin.pollet@serli.com">Kevin Pollet</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestReport implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The default fetch limit for find queries.
     */
    private static final int DEFAULT_FETCH_LIMIT = 20;

    /**
     * The {@code TestReport} entity kind.
     */
    private static final String TEST_REPORT = TestReport.class.getSimpleName();

    /**
     * Returns an instance of {@code TestReport} representing the given test report entity.
     *
     * @param entity the test report entity.
     * @return the {@code TestReport} instance.
     * @throws java.lang.NullPointerException     if {@code entity} is {@code null}.
     * @throws java.lang.IllegalArgumentException if {@code entity} king is not {@code TestReport}.
     */
    public static TestReport from(Entity entity) {
        checkNotNull(entity, "'entity' parameter cannot be null");
        checkArgument(TEST_REPORT.equals(entity.getKind()), "'entity' parameter wrong kind");

        final List<Test> failedTests = new ArrayList<>();
        try {

            final JSONArray jsonArray = new JSONArray(new JSONTokener(((Text) entity.getProperty("failedTests")).getValue()));
            for (int i = 0; i < jsonArray.length(); i++) {
                final Test test = Test.valueOf(jsonArray.getJSONObject(i));
                failedTests.add(test);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return new TestReport(
                (String) entity.getProperty("buildTypeId"),
                ((Long) entity.getProperty("buildId")).intValue(),
                (Date) entity.getProperty("buildDate"),
                ((Long) entity.getProperty("buildDuration")),
                ((Long) entity.getProperty("numberOfPassedTests")).intValue(),
                ((Long) entity.getProperty("numberOfIgnoredTests")).intValue(),
                ((Long) entity.getProperty("numberOfFailedTests")).intValue(),
                failedTests
        );
    }

    /**
     * Finds the {@code TestReport} with the given build type id ordered by build id in the descendant order.
     *
     * @param buildTypeId      the build type id.
     * @param limit            the optional fetch limit, by default {@link com.google.appengine.tck.site.endpoints.TestReport#DEFAULT_FETCH_LIMIT}.
     * @param reports          the reports entry point
     * @return the matching test reports list or an empty one if none.
     */
    @SuppressWarnings("unchecked")
    public static List<TestReport> findByBuildTypeIdOrderByBuildIdDesc(String buildTypeId, Optional<Integer> limit, Reports reports) {
        final MemcacheService memcacheService = reports.getMemcacheService();
        List<TestReport> results = (List<TestReport>) memcacheService.get(buildTypeId);
        if (results == null) {
            final Filter buildTypeFilter = new Query.FilterPredicate("buildTypeId", FilterOperator.EQUAL, buildTypeId);
            final Query query = new Query(TEST_REPORT).setFilter(buildTypeFilter).addSort("buildId", DESCENDING);

            final DatastoreService datastoreService = reports.getDatastoreService();
            final PreparedQuery preparedQuery = datastoreService.prepare(query);
            final List<Entity> entities = preparedQuery.asList(FetchOptions.Builder.withLimit(limit.or(DEFAULT_FETCH_LIMIT)));

            results = new ArrayList<>();
            for (Entity oneEntity : entities) {
                final TestReport report = from(oneEntity);
                results.add(report);
            }

            memcacheService.put(buildTypeId, results);
        }
        return results;
    }

    private String buildTypeId;
    private int buildId;
    private Date buildDate;
    private long buildDuration;
    private int numberOfPassedTests;
    private int numberOfIgnoredTests;
    private int numberOfFailedTests;
    private List<Test> failedTests;

    /**
     * Constructs a {@code TestReport} instance.
     *
     * @param buildId              the build id.
     * @param buildDate            the build date.
     * @param buildDuration        the build duration.
     * @param numberOfPassedTests  the number of passed tests.
     * @param numberOfIgnoredTests the number of ignored tests.
     * @param numberOfFailedTests  the number of failed tests.
     * @param failedTests          the failed tests.
     * @throws java.lang.NullPointerException     if {@code buildTypeId}, {@code buildDate} and/or {@code failedTests} are {@code null}.
     * @throws java.lang.IllegalArgumentException if {@code buildTypeId} is empty.
     */
    public TestReport(String buildTypeId, int buildId, Date buildDate, long buildDuration, int numberOfPassedTests, int numberOfIgnoredTests, int numberOfFailedTests, List<Test> failedTests) {
        checkNotNull(buildDate, "'buildDate' parameter cannot be null");
        checkNotNull(failedTests, "'failedTests' parameter cannot be null");
        checkNotNull(buildTypeId, "'buildTypeId' parameter cannot be null");
        checkArgument(!buildTypeId.isEmpty(), "'buildTypeId' parameter cannot be empty");

        this.buildTypeId = buildTypeId;
        this.buildId = buildId;
        this.buildDate = buildDate;
        this.buildDuration = buildDuration;
        this.numberOfPassedTests = numberOfPassedTests;
        this.numberOfIgnoredTests = numberOfIgnoredTests;
        this.numberOfFailedTests = numberOfFailedTests;
        this.failedTests = ImmutableList.copyOf(failedTests);
    }

    /**
     * For JSON.
     */
    private TestReport() {
        // ensure that the failed tests list is never null
        this.failedTests = ImmutableList.of();
    }

    /**
     * Returns the build type id.
     *
     * @return the build type id, never {@code null} or empty.
     */
    public String getBuildTypeId() {
        return buildTypeId;
    }

    /**
     * Returns the build id.
     *
     * @return the build id.
     */
    public int getBuildId() {
        return buildId;
    }

    /**
     * Returns the build date.
     *
     * @return the build date, never {@code null}.
     */
    public Date getBuildDate() {
        return buildDate;
    }

    /**
     * Returns the build duration in seconds.
     *
     * @return the build duration in seconds.
     */
    public long getBuildDuration() {
        return buildDuration;
    }

    /**
     * Returns the passed tests number.
     *
     * @return the passed tests number.
     */
    public int getNumberOfPassedTests() {
        return numberOfPassedTests;
    }

    /**
     * Returns the ignored tests number.
     *
     * @return the ignored tests number.
     */
    public int getNumberOfIgnoredTests() {
        return numberOfIgnoredTests;
    }

    /**
     * Returns the failed tests number.
     *
     * @return the failed tests number.
     */
    public int getNumberOfFailedTests() {
        return numberOfFailedTests;
    }

    /**
     * Returns the failing tests.
     *
     * @return the failed tests list or an empty one if none.
     */
    public List<Test> getFailedTests() {
        return failedTests;
    }

    /**
     * Persists {@code this} test report to the given {@link com.google.appengine.api.datastore.DatastoreService}
     * and invalidate the cache.
     *
     * @param reports          the reports entry point
     */
    public void save(Reports reports) {
        final Entity entity = new Entity(TEST_REPORT, buildTypeId + buildId);
        entity.setProperty("buildTypeId", buildTypeId);
        entity.setProperty("buildId", buildId);
        entity.setProperty("buildDate", buildDate);
        entity.setProperty("buildDuration", buildDuration);
        entity.setProperty("numberOfPassedTests", numberOfPassedTests);
        entity.setProperty("numberOfIgnoredTests", numberOfIgnoredTests);
        entity.setProperty("numberOfFailedTests", numberOfFailedTests);

        final JSONArray jsonArray = new JSONArray();
        for (Test oneFailingTest : failedTests) {
            jsonArray.put(oneFailingTest.asJson());
        }

        entity.setProperty("failedTests", new Text(jsonArray.toString()));

        final DatastoreService datastoreService = reports.getDatastoreService();
        datastoreService.put(entity);

        final MemcacheService memcacheService = reports.getMemcacheService();
        memcacheService.delete(buildTypeId); // invalidate cache
    }
}

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

import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Nullable;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.tck.site.utils.Constants;
import com.google.common.base.Optional;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod.GET;
import static com.google.api.server.spi.config.ApiMethod.HttpMethod.POST;

/**
 * The Google cloud endpoint Reports API.
 *
 * @author <a href="mailto:kevin.pollet@serli.com">Kevin Pollet</a>
 */
@Api(
        name = "reports",
        version = "v1",
        scopes = Constants.EMAIL_SCOPE,
        clientIds = { Constants.TEAMCITY_APP_ID }
)
public class Reports {
    /**
     * The {@link com.google.appengine.api.datastore.DatastoreService} where build reports are persisted.
     */
    private final DatastoreService datastoreService;

    /**
     * The {@link com.google.appengine.api.memcache.MemcacheService} where build reports are cached.
     */
    private final MemcacheService memcacheService;

    /**
     * Constructs an instance of {@code Reports}.
     */
    public Reports() {
        this.datastoreService = DatastoreServiceFactory.getDatastoreService();
        this.memcacheService = MemcacheServiceFactory.getMemcacheService();
    }

    DatastoreService getDatastoreService() {
        return datastoreService;
    }

    MemcacheService getMemcacheService() {
        return memcacheService;
    }

    /**
     * Returns the {@link com.google.appengine.tck.site.endpoints.TestReport} with the given build type id ordered by
     * build id in the descendant order.
     *
     * @param buildTypeId the build type id.
     * @param limit       the optional fetch limit, by default {@link com.google.appengine.tck.site.endpoints.TestReport#DEFAULT_FETCH_LIMIT}.
     * @return the matching test reports list or an empty one if none.
     */
    @ApiMethod(
            name = "tests.list",
            path = "{buildTypeId}/tests",
            httpMethod = GET
    )
    public List<TestReport> listTestReports(@Named("buildTypeId") String buildTypeId, @Nullable @Named("limit") Integer limit) {
        return TestReport.findByBuildTypeIdOrderByBuildIdDesc(buildTypeId, Optional.fromNullable(limit), this);
    }

    /**
     * Inserts the given {@code TestReport}.
     *
     * @param report the test report to insert.
     * @param user   the authenticated application.
     * @throws OAuthRequestException if request is unauthenticated.
     */
    @ApiMethod(
            name = "tests.insert",
            path = "tests",
            httpMethod = POST
    )
    public void insertTestReport(TestReport report, User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("Test report cannot be inserted if request is unauthenticated");
        }

        report.save(this);
    }
}

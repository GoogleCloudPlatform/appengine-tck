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

package com.google.appengine.tck.teamcity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.appspot.cloud_test_compatibility_kit.reports.Reports;
import com.appspot.cloud_test_compatibility_kit.reports.model.Test;
import com.appspot.cloud_test_compatibility_kit.reports.model.TestReport;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.BuildStatistics;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.STestRun;
import jetbrains.buildServer.serverSide.TestFailureInfo;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:kevin.pollet@serli.com">Kevin Pollet</a>
 */
public class ReportsFeature extends BuildFeature {
    private static final Logger log = Logger.getLogger(ReportsFeature.class.getName());

    @NotNull
    private final String editParametersUrl;

    @NotNull
    private final JsonFactory jsonFactory;

    @NotNull
    private final NetHttpTransport httpTransport;

    @NotNull
    private final ReportsConstants constants;

    public ReportsFeature(EventDispatcher<BuildServerListener> dispatcher, @NotNull ReportsDescriptor descriptor, @NotNull ReportsConstants constants) {
        this.editParametersUrl = descriptor.getFeaturePath();
        this.constants = constants;
        this.jsonFactory = JacksonFactory.getDefaultInstance();

        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        if (dispatcher != null) {
            dispatcher.addListener(new BuildServerAdapter() {
                @Override
                public void buildFinished(SRunningBuild build) {
                    handleBuildFinished(build);
                }
            });
        }
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed() {
        return false;
    }

    @NotNull
    @Override
    public String getType() {
        return "appengine.tck.reports";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Google App Engine TCK Reports";
    }

    @NotNull
    @Override
    public String getEditParametersUrl() {
        return editParametersUrl;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> params) {
        return "Google Cloud Endpoint Client Credentials";
    }

    //TODO errors
    //TODO OAuth here??
    @Nullable
    @Override
    public PropertiesProcessor getParametersProcessor() {
        return new PropertiesProcessor() {
            @NotNull
            public Collection<InvalidProperty> process(@Nullable final Map<String, String> params) {
                final Collection<InvalidProperty> result = new ArrayList<>();
                if (params == null) {
                    return result;
                }

                // check parameters
                final String clientId = params.get(constants.getApplicationClientId());
                if (clientId == null) {
                    result.add(new InvalidProperty(constants.getApplicationClientId(), "Missing the application client id"));
                }

                final String clientSecret = params.get(constants.getApplicationClientSecret());
                if (clientSecret == null) {
                    result.add(new InvalidProperty(constants.getApplicationClientSecret(), "Missing the application client secret"));
                }

                final String applicationOAuthCode = params.get(constants.getApplicationOauthCode());
                if (applicationOAuthCode == null) {
                    result.add(new InvalidProperty(constants.getApplicationOauthCode(), "Missing the application oauth code"));
                }

                // get the refresh and access token
                if (result.isEmpty()) {
                    handleTokens(params);
                }

                return result;
            }
        };
    }

    protected void handleTokens(Map<String, String> params) {
        final GoogleClientSecrets secrets = new GoogleClientSecrets().setInstalled(
                new GoogleClientSecrets.Details().
                        setClientId(params.get(constants.getApplicationClientId())).
                        setClientSecret(params.get(constants.getApplicationClientSecret()))
        );

        try {
            final GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    this.httpTransport,
                    this.jsonFactory,
                    secrets.getDetails().getClientId(),
                    secrets.getDetails().getClientSecret(),
                    params.get(constants.getApplicationOauthCode()),
                    constants.getRedirectUri()
            ).execute();

            params.put(constants.getApplicationRefreshToken(), tokenResponse.getRefreshToken());
            params.put(constants.getApplicationAccessToken(), tokenResponse.getAccessToken());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultParameters() {
        return Collections.emptyMap();
    }

    protected void handleBuildFinished(SRunningBuild build) {
        final SBuildType bt = build.getBuildType();
        if (bt == null) {
            return;
        }

        for (SBuildFeatureDescriptor feature : bt.getBuildFeatures()) {
            if (getType().equalsIgnoreCase(feature.getType())) {
                handleBuildFinished(build, feature);
                break;
            }
        }
    }

    protected void handleBuildFinished(SRunningBuild build, SBuildFeatureDescriptor feature) {
        final Map<String, String> parameters = feature.getParameters();
        final BuildStatistics fullBuildStatistics = build.getFullStatistics();

        // prepare the endpoint authentication
        final GoogleClientSecrets secrets = new GoogleClientSecrets().setInstalled(
                new GoogleClientSecrets.Details().
                        setClientId(parameters.get(constants.getApplicationClientId())).
                        setClientSecret(parameters.get(constants.getApplicationClientSecret()))
        );

        final Credential credential = new GoogleCredential.Builder().
                setJsonFactory(jsonFactory).
                setClientSecrets(secrets).
                setTransport(httpTransport).
                build();

        credential.setAccessToken(parameters.get(constants.getApplicationAccessToken()));
        credential.setRefreshToken(parameters.get(constants.getApplicationRefreshToken()));

        // build the test report
        final Reports reports = new Reports.Builder(httpTransport, jsonFactory, credential).
                setApplicationName(constants.getApplicationName()).
                build();

        final String buildTypeExternalId = build.getBuildTypeExternalId();
        final Integer buildNumber = Integer.valueOf(build.getBuildNumber());

        final TestReport testReport = new TestReport().
                setBuildTypeId(buildTypeExternalId).
                setBuildId(buildNumber).
                setBuildDate(new DateTime(build.getStartDate())).
                setBuildDuration(build.getDuration()).
                setNumberOfFailedTests(fullBuildStatistics.getFailedTestCount()).
                setNumberOfIgnoredTests(fullBuildStatistics.getIgnoredTestCount()).
                setNumberOfPassedTests(fullBuildStatistics.getPassedTestCount());

        final List<Test> failedTests = new ArrayList<>();
        for (STestRun oneTestRun : fullBuildStatistics.getFailedTests()) {
            final STest failedTest = oneTestRun.getTest();
            final TestName failedTestName = failedTest.getName();

            TestFailureInfo failureInfo = oneTestRun.getFailureInfo();
            String error = (failureInfo != null) ? failureInfo.getShortStacktrace() : "[NO-FAILURE-INFO]";

            failedTests.add(
                    new Test().
                            setPackageName(failedTestName.getPackageName()).
                            setClassName(failedTestName.getClassName()).
                            setMethodName(failedTestName.getTestMethodName()).
                            setError(error)
            );
        }
        testReport.setFailedTests(failedTests);

        final List<Test> ignoredTests = new ArrayList<>();
        for (STestRun oneTestRun : fullBuildStatistics.getIgnoredTests()) {
            final STest ignoredTest = oneTestRun.getTest();
            final TestName ignoredTestName = ignoredTest.getName();

            ignoredTests.add(
                    new Test().
                            setPackageName(ignoredTestName.getPackageName()).
                            setClassName(ignoredTestName.getClassName()).
                            setMethodName(ignoredTestName.getTestMethodName()).
                            setError(oneTestRun.getIgnoreComment())
            );
        }
        testReport.setIgnoredTests(ignoredTests);

        log.info(String.format("Pushing build results for '%s' [%s] ...", buildTypeExternalId, buildNumber));
        // publish results to appspot application
        try {
            reports.tests().insert(testReport).execute();

            log.info(String.format("Build results push for '%s' [%s] is done.", buildTypeExternalId, buildNumber));
        } catch (IOException e) {
            log.warning(String.format("Error pushing build results for '%s' [%s]!", buildTypeExternalId, buildNumber));
            throw new RuntimeException(e);
        }
    }
}

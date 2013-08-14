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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.util.EventDispatcher;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ales Justin
 */
public class ReportsFeature extends BuildFeature {
    private static final Logger log = Logger.getLogger(ReportsFeature.class.getName());

    private static final String TYPE = "appengine.tck.reports";

    private final String editParametersUrl;
    private HttpClient client;

    public ReportsFeature(EventDispatcher<BuildServerListener> dispatcher, ReportsDescriptor descriptor) {
        dispatcher.addListener(new BuildServerAdapter() {
            @Override
            public void buildFinished(SRunningBuild build) {
                handleBuildFinished(build);
            }
        });
        editParametersUrl = descriptor.getFeaturePath();
    }

    public void start() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", 80, new PlainSocketFactory()));
        registry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        ClientConnectionManager ccm = new PoolingClientConnectionManager(registry);
        client = new DefaultHttpClient(ccm);
    }

    public void stop() {
        if (client != null) {
            client.getConnectionManager().shutdown();
            client = null;
        }
    }

    @NotNull
    @Override
    public String getType() {
        return TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Google App Engine TCK Reports";
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return editParametersUrl;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> params) {
        return "Update site parameters";
    }

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

                final String url = params.get(UIConstants.URL);
                if (url == null) {
                    result.add(new InvalidProperty(UIConstants.URL, "Missing site URL!"));
                }
                if (url.startsWith("http") == false) {
                    result.add(new InvalidProperty(UIConstants.URL, "Invalid URL: " + url));
                }

                final String token = params.get(UIConstants.TOKEN);
                if (token == null || token.trim().length() == 0) {
                    result.add(new InvalidProperty(UIConstants.TOKEN, "Missing site token!"));
                }

                return result;
            }
        };
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultParameters() {
        return Collections.singletonMap(UIConstants.URL, "http://www.appengine-tck.org");
    }

    protected void handleBuildFinished(SRunningBuild build) {
        SBuildType bt = build.getBuildType();
        if (bt == null) return;

        for (SBuildFeatureDescriptor feature : bt.getBuildFeatures()) {
            if (feature.getType().equals(TYPE) == false) continue;

            handleBuildFinished(build, feature);
        }
    }

    protected void handleBuildFinished(SRunningBuild build, SBuildFeatureDescriptor feature) {
        String buildTypeId = build.getBuildTypeExternalId();
        long buildId = build.getBuildId();

        BuildStatistics statistics = build.getFullStatistics();
        int failedTests = statistics.getFailedTestCount();
        int passedTests = statistics.getPassedTestCount();
        int ignoredTests = statistics.getIgnoredTestCount();

        final Map<String, String> parameters = feature.getParameters();
        final String url = parameters.get(UIConstants.URL);
        final String updateToken = parameters.get(UIConstants.TOKEN);

        try {
            final URIBuilder builder = new URIBuilder(url);
            builder.setParameter("updateToken", updateToken);
            builder.setParameter("buildTypeId", buildTypeId);
            builder.setParameter("buildId", String.valueOf(buildId));
            builder.setParameter("failedTests", String.valueOf(failedTests));
            builder.setParameter("passedTests", String.valueOf(passedTests));
            builder.setParameter("ignoredTests", String.valueOf(ignoredTests));

            HttpPut put = new HttpPut(builder.build());

            StringBuilder sb = new StringBuilder();
            for (STestRun tr : statistics.getFailedTests()) {
                STest test = tr.getTest();
                TestName name = test.getName();
                // com.acme.foo.SomeTest_#_testBar_#_this is err msg
                sb.append(name.getTestClass()).append("_#_").append(name.getTestMethodName()).append("_#_").append(tr.getFailureInfo().getStacktraceMessage()).append("\n");
            }
            put.setEntity(new StringEntity(sb.toString()));

            log.info(String.format("Executing PUT [#%s]: %s", buildId, put));

            HttpResponse response = client.execute(put);
            log.info(String.format("Response [#%s]: %s", buildId, response));
        } catch (Throwable t) {
            log.log(Level.SEVERE, String.format("Error pushing TCK report [#%s]: %s", buildId, t.getMessage()), t);
            throw new IllegalStateException(t);
        }
    }
}

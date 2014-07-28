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

package com.google.appengine.tck.mapreduce;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.lib.LibUtils;
import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public abstract class MapReduceTestBase extends TestBase {

    protected static WebArchive getDefaultDeployment() {
        TestContext context = new TestContext();
        context.setWebXmlFile("mr-web.xml");

        WebArchive war = getTckDeployment(context);
        war.addClass(MapReduceTestBase.class);

        war.addAsWebInfResource("queue.xml", "queue.xml");

        LibUtils libUtils = new LibUtils();
        libUtils.addGaeAsLibrary(war);
        libUtils.addLibrary(war, "com.google.appengine.tools", "appengine-mapreduce");
        libUtils.addLibrary(war, "com.google.appengine.tools", "appengine-pipeline");

        // GCS
        libUtils.addLibrary(war, "com.google.appengine.tools", "appengine-gcs-client");
        libUtils.addLibrary(war, "joda-time", "joda-time");
        libUtils.addLibrary(war, "com.google.api-client", "google-api-client");
        libUtils.addLibrary(war, "com.google.http-client", "google-http-client");
        libUtils.addLibrary(war, "com.google.http-client", "google-http-client-appengine");
        libUtils.addLibrary(war, "com.google.http-client", "google-http-client-jackson2");
        libUtils.addLibrary(war, "com.google.api-client", "google-api-client-appengine");
        libUtils.addLibrary(war, "com.google.apis", "google-api-services-storage");
        libUtils.addLibrary(war, "com.fasterxml.jackson.core", "jackson-core");

        libUtils.addLibrary(war, "com.google.guava", "guava");
        libUtils.addLibrary(war, "it.unimi.dsi", "fastutil");
        libUtils.addLibrary(war, "com.googlecode.charts4j", "charts4j");
        libUtils.addLibrary(war, "commons-logging", "commons-logging");
        libUtils.addLibrary(war, "org.json", "json");

        return war;
    }

    protected JobInfo getJobInfo(final String phase, final String handle) throws Exception {
        PipelineService pipelineService = PipelineServiceFactory.newPipelineService();
        return getJobInfo(pipelineService, phase, handle);
    }

    protected JobInfo getJobInfo(PipelineService pipelineService, String phase, final String handle) throws Exception {
        JobInfo jobInfo = pipelineService.getJobInfo(handle);
        Assert.assertNotNull("Missing JobInfo - [ " + phase + " ] - handle: " + handle, jobInfo);
        return jobInfo;
    }

    protected JobInfo waitToFinish(final String phase, final String handle) throws Exception {
        PipelineService pipelineService = PipelineServiceFactory.newPipelineService();
        JobInfo jobInfo = getJobInfo(pipelineService, phase, handle);
        JobInfo.State state = jobInfo.getJobState();
        int N = 24; // 2min
        while (isRunning(state) && N > 0) {
            N--;
            sync(5 * 1000L); // 5sec
            // new info lookup
            jobInfo = getJobInfo(pipelineService, phase, handle);
            state = jobInfo.getJobState();
        }
        if (N == 0 && isRunning(state)) {
            throw new IllegalStateException("Failed to finish the job [ " + phase + " ]: " + handle + ", info: " + toInfo(jobInfo));
        }
        if (state != JobInfo.State.COMPLETED_SUCCESSFULLY) {
            throw new IllegalStateException("Job " + handle + " failed [ " + phase + " ]: " + toInfo(jobInfo));
        }
        return jobInfo;
    }

    protected static String toInfo(JobInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("JobInfo[ ").append(info).append("]");
        if (info != null) {
            sb.append(" --> ");
            sb.append("state - ").append(info.getJobState()).append(", ");
            sb.append("output - ").append(info.getOutput()).append(", ");
            sb.append("error - ").append(info.getError());
        }
        return sb.toString();
    }

    protected boolean isRunning(JobInfo.State state) {
        return (state == null || state == JobInfo.State.RUNNING);
    }

    protected MapReduceSettings getSettings() {
        return new MapReduceSettings.Builder().setWorkerQueueName("mapreduce-workers").build();
    }
}

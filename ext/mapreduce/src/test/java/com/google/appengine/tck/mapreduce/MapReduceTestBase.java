/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.google.appengine.tck.mapreduce;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.lib.LibUtils;
import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import org.junit.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
        libUtils.addLibrary(war, "com.google.appengine", "appengine-mapper");
        libUtils.addLibrary(war, "com.google.guava", "guava");
        libUtils.addLibrary(war, "com.googlecode.charts4j", "charts4j");
        libUtils.addLibrary(war, "commons-logging", "commons-logging");
        libUtils.addLibrary(war, "org.apache.hadoop", "hadoop-core");
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
        sb.append("JobInfo[ ").append(info).append( "]");
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
        return new MapReduceSettings().setWorkerQueueName("mapreduce-workers").setControllerQueueName("default");
    }
}

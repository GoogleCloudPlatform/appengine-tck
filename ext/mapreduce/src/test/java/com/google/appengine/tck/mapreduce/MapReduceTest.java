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

import java.util.Arrays;
import java.util.List;

import com.google.appengine.tck.mapreduce.support.CountMapper;
import com.google.appengine.tck.mapreduce.support.CountReducer;
import com.google.appengine.tck.mapreduce.support.EntityCreator;
import com.google.appengine.tools.mapreduce.Counter;
import com.google.appengine.tools.mapreduce.Counters;
import com.google.appengine.tools.mapreduce.KeyValue;
import com.google.appengine.tools.mapreduce.MapReduceJob;
import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.mapreduce.MapReduceSpecification;
import com.google.appengine.tools.mapreduce.Marshallers;
import com.google.appengine.tools.mapreduce.inputs.ConsecutiveLongInput;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.mapreduce.outputs.InMemoryOutput;
import com.google.appengine.tools.mapreduce.outputs.NoOutput;
import com.google.appengine.tools.mapreduce.reducers.NoReducer;
import com.google.appengine.tools.pipeline.JobInfo;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MapReduceTest extends MapReduceTestBase {
    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getDefaultDeployment();
        war.addPackage(EntityCreator.class.getPackage());
        return war;
    }

    @Test
    public void testCountJob() throws Exception {
        List<String> payloads = Arrays.asList("capedwarf", "jboss", "redhat");
        int shardCount = 1;

        final String createHandle = MapReduceJob.start(
            MapReduceSpecification.of(
                "Create MapReduce entities",
                new ConsecutiveLongInput(0, payloads.size() * (long) shardCount, shardCount),
                new EntityCreator("MapReduceTest", payloads),
                Marshallers.getVoidMarshaller(),
                Marshallers.getVoidMarshaller(),
                NoReducer.<Void, Void, Void>create(),
                NoOutput.<Void, Void>create(1)),
            getSettings());

        JobInfo createJI = waitToFinish("CREATE", createHandle);
        Object create = createJI.getOutput();
        log.warning("----- Create: " + create);

        int mapShardCount = 1;
        int reduceShardCount = 1;

        String countHandle = MapReduceJob.start(
            MapReduceSpecification.of(
                "MapReduceTest stats",
                new DatastoreInput("MapReduceTest", mapShardCount),
                new CountMapper(),
                Marshallers.getStringMarshaller(),
                Marshallers.getLongMarshaller(),
                new CountReducer(),
                new InMemoryOutput<KeyValue<String, Long>>(reduceShardCount)),
            getSettings());

        JobInfo countJI = waitToFinish("COUNT", countHandle);
        Object count = countJI.getOutput();
        log.warning("----- Count: " + count);

        Assert.assertTrue(count instanceof MapReduceResult);
        MapReduceResult result = MapReduceResult.class.cast(count);
        int[] chars = toChars(payloads);
        Counters counters = result.getCounters();
        for (int i = 0; i < chars.length; i++) {
            char ch = (char) ('a' + i);
            Counter c = counters.getCounter(CountMapper.toKey(ch));
            Assert.assertEquals(String.format("Invalid count for '%s'.", ch), chars[i], c.getValue());
        }
    }

    protected static int[] toChars(List<String> payloads) {
        int[] chars = new int['z' - 'a' + 1];
        for (String payload : payloads) {
            for (int i = 0; i < payload.length(); i++) {
                chars[payload.charAt(i) - 'a']++;
            }
        }
        return chars;
    }
}

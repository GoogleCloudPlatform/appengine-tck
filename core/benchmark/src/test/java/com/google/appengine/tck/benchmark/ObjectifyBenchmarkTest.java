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

package com.google.appengine.tck.benchmark;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.benchmark.support.Data;
import com.google.appengine.tck.benchmark.support.Root;
import com.google.appengine.tck.lib.LibUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.VoidWork;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author Terry Okamoto
 */
@RunWith(Arquillian.class)
public class ObjectifyBenchmarkTest extends TestBase {
    private static final Random RANDOM = new Random();

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext().setUseSystemProperties(true).setCompatibilityProperties(TCK_PROPERTIES);
        WebArchive war = getTckDeployment(context);
        war.addPackage(Data.class.getPackage());
        LibUtils libUtils = new LibUtils();
        libUtils.addLibrary(war, "com.googlecode.objectify", "objectify");
        libUtils.addLibrary(war, "com.google.guava", "guava");
        return war;
    }

    @Before
    public void setUp() {
        ObjectifyService.register(Root.class);
        ObjectifyService.register(Data.class);
    }

    @After
    public void tearDown() {
        ObjectifyService.reset();
    }

    @Test
    public void testInserts() throws Exception {
        final int N = Integer.parseInt(getTestSystemProperty("benchmark.datastore.size", "6000"));
        log.info(String.format(">>>> N = %s", N));

        Root root = new Root();
        root.setName("Root" + System.currentTimeMillis());
        Result<Key<Root>> result = ObjectifyService.ofy().save().entity(root);
        final Key<Root> parent = result.now();

        // wrap inserts in same Tx -- as expected
        ObjectifyService.ofy().transact(new VoidWork() {
            public void vrun() {
                doInsert(generateData(N, parent));
            }
        });

        // do it w/o Tx
        doInsert(generateData(N, parent));
    }

    protected List<Data> generateData(int N, Key parent) {
        final List<Data> list = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            Data data = new Data();

            data.setParent(parent);

            data.setCount(i);
            data.setDate(new Date());
            data.setName(UUID.randomUUID().toString());

            data.setStart(RANDOM.nextFloat());
            data.setStop(RANDOM.nextFloat());

            data.setDown(RANDOM.nextFloat());
            data.setUp(RANDOM.nextFloat());

            list.add(data);
        }
        return list;
    }

    protected void doInsert(List<Data> list) {
        long start = System.currentTimeMillis();
        Map<Key<Data>, Data> saved = ObjectifyService.ofy().save().entities(list).now();
        long end = System.currentTimeMillis();

        long totalMillis = (end - start);
        log.info(String.format(">>>> Save [%s] time: %sms", list.size(), totalMillis));

        Assert.assertEquals(list.size(), saved.size());
    }
}

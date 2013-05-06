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

package com.google.appengine.tck.datastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * datastore statistics test.
 * http://code.google.com/appengine/docs/java/datastore/stats.html
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class StatsTest extends DatastoreTestBase {
    private static final String[] stats = {"Total__", "Kind__", "Kind_IsRootEntity__",
        "Kind_NotRootEntity__", "PropertyType__", "PropertyType_Kind__", "PropertyName_Kind__",
        "PropertyType_PropertyName_Kind__"};

    @Test
    public void testDummy() {
        // TODO -- remove once some real test is in place
    }

//  @Test
//  public void testStatics() {
//    if (!TestContextHelper.get().getReq().getServerName().endsWith("localhost")) {
//      // __Stat_Namespace__ is used to list all aggregated namespaces.
//      checkCount("__Stat_Namespace__");
//      for (int i = 0; i < stats.length; i++) {
//        // check stats that are empty namespace.
//        checkCount("__Stat_" + stats[i]);
//        // check stats that are namespace specific.
//        checkCount("__Stat_Ns_" + stats[i]);
//      }
//    }
//  }

    private void checkCount(String statsKind) {
        FetchOptions fo = FetchOptions.Builder.withDefaults();
        Query query = new Query(statsKind);
        assertTrue(service.prepare(query).countEntities(fo) > 0);
        for (Entity readRec : service.prepare(query).asIterable()) {
            assertTrue((Long) readRec.getProperty("count") > 0);
        }
    }
}

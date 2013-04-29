/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.appengine.tck.datastore;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.ImplicitTransactionManagementPolicy;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.datastore.ReadPolicy.Consistency;
import com.google.apphosting.api.ApiProxy.ApiDeadlineExceededException;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DatastoreService Config Settings test.
 */
@RunWith(Arquillian.class)
public class ConfigTest extends DatastoreTestBase {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConfigBuilder() {
      DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withDefaults();
      assertEquals(new ReadPolicy(Consistency.STRONG).getConsistency(), 
          config.getReadPolicy().getConsistency());

      config = DatastoreServiceConfig.Builder.withDeadline(10);
      assertEquals(new Double(10), config.getDeadline());
      config.deadline(20);
      assertEquals(new Double(20), config.getDeadline());

      config = DatastoreServiceConfig.Builder
          .withImplicitTransactionManagementPolicy(ImplicitTransactionManagementPolicy.AUTO);
      assertEquals(ImplicitTransactionManagementPolicy.AUTO,
                   config.getImplicitTransactionManagementPolicy());
      config.implicitTransactionManagementPolicy(ImplicitTransactionManagementPolicy.NONE);
      assertEquals(ImplicitTransactionManagementPolicy.NONE,
                   config.getImplicitTransactionManagementPolicy());

      config = DatastoreServiceConfig.Builder.withMaxEntityGroupsPerRpc(5);
      assertEquals(new Integer(5), config.getMaxEntityGroupsPerRpc());
      config.maxEntityGroupsPerRpc(2);
      assertEquals(new Integer(2), config.getMaxEntityGroupsPerRpc());

      config = DatastoreServiceConfig.Builder
          .withReadPolicy(new ReadPolicy(Consistency.EVENTUAL));
      assertEquals(new ReadPolicy(Consistency.EVENTUAL).getConsistency(),
          config.getReadPolicy().getConsistency());
      config.readPolicy(new ReadPolicy(Consistency.STRONG));
      assertEquals(new ReadPolicy(Consistency.STRONG).getConsistency(),
          config.getReadPolicy().getConsistency());
    }

    @Test
    public void testDeadlineConfig() {
      DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withDeadline(0.00001);      
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService(config);
      assertNotNull(ds);
      Entity g1 = new Entity("test");
      g1.setProperty("deadline", "0.00001");
      thrown.expect(ApiDeadlineExceededException.class);
      ds.put(g1);
    }

    @Test
    public void testTranManagePolicyAsyncInvalidConfig() {
      DatastoreServiceConfig config = DatastoreServiceConfig.Builder
          .withImplicitTransactionManagementPolicy(ImplicitTransactionManagementPolicy.AUTO);

      // Async Service does not support AUTO
      thrown.expect(IllegalArgumentException.class);
      AsyncDatastoreService sds = DatastoreServiceFactory.getAsyncDatastoreService(config);
    }
}

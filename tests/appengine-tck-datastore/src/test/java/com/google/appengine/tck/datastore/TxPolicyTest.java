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

import java.util.Collections;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.ImplicitTransactionManagementPolicy;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test AUTO tx policy.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class TxPolicyTest extends DatastoreTestBase {
    @Test
    public void testAutoPolicy() throws Exception {
        DatastoreServiceConfig config = DatastoreServiceConfig.Builder.withImplicitTransactionManagementPolicy(ImplicitTransactionManagementPolicy.AUTO);
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService(config);

        Key k1 = null;
        Transaction tx = ds.beginTransaction();
        try {
            // this one should be part of auto Tx
            k1 = ds.put(new Entity("PutAutoTx"));
        } finally {
            tx.rollback();
        }

        Assert.assertTrue(ds.get(Collections.singleton(k1)).isEmpty());

        k1 = ds.put(new Entity("DeleteAutoTx"));
        try {
            Assert.assertNotNull(ds.get(k1));

            tx = ds.beginTransaction();
            try {
                // this one should be part of auto Tx
                ds.delete(k1);
            } finally {
                tx.rollback();
            }

            Assert.assertNotNull(ds.get(k1));
        } finally {
            ds.delete(k1);
        }
    }
}

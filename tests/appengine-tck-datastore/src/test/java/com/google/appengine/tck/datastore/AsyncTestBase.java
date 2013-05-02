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

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AsyncTestBase extends DatastoreHelperTestBase {
    protected static WebArchive getAsynchDeployment() {
        return getHelperDeployment().addClass(AsyncTestBase.class);
    }

    protected <T> T inTx(Action<T> action) throws Exception {
        AsyncDatastoreService ads = DatastoreServiceFactory.getAsyncDatastoreService();
        Transaction tx = ads.beginTransaction().get();
        boolean ok = false;
        try {
            T result = action.run(ads);
            ok = true;
            return result;
        } finally {
            if (ok)
                tx.commitAsync();
            else
                tx.rollbackAsync();

            sync(); // wait for tx to finish
        }
    }

    protected static interface Action<T> {
        T run(AsyncDatastoreService ads) throws Exception;
    }
}

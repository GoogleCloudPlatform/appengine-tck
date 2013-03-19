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

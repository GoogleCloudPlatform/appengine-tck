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

package com.google.appengine.tck.mapreduce.support;

import java.util.List;
import java.util.Random;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.Mapper;

/**
 * From Google AppEngine MapReduce Examples.
 *
 * @author Google App Engine Team
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EntityCreator extends Mapper<Long, Void, Void> {
    private static final long serialVersionUID = 1L;

    private final String kind;
    private final List<String> payloads;
    private final Random random = new Random();

    private transient DatastoreMutationPool pool;

    public EntityCreator(String kind, List<String> payloads) {
        if (kind == null)
            throw new IllegalArgumentException("Null kind");

        this.kind = kind;
        this.payloads = payloads;
    }

    @Override
    public void beginShard() {
        pool = DatastoreMutationPool.forManualFlushing();
    }

    public void map(Long index) {
        String name = String.valueOf(random.nextLong() & Long.MAX_VALUE);
        Entity e = new Entity(kind, name);
        e.setProperty("payload", new Text(payloads.get((int) (index % payloads.size()))));
        pool.put(e);
        pool.flush();
    }
}

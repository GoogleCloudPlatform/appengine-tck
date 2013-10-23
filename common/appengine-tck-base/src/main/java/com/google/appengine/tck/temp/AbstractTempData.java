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

package com.google.appengine.tck.temp;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractTempData implements TempData {
    private long timestamp;

    protected AbstractTempData() {
        timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> toProperties(DatastoreService ds) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", getTimestamp());
        toProperties(ds, map);
        return map;
    }

    protected abstract void toProperties(DatastoreService ds, Map<String, Object> map);

    public void fromProperties(Map<String, Object> propeties) {
        setTimestamp((Long) propeties.get("timestamp"));
        fromPropertiesInternal(propeties);
    }

    protected abstract void fromPropertiesInternal(Map<String, Object> propeties);

    public void preGet(DatastoreService ds) throws Exception {
    }

    public void postGet(DatastoreService ds) throws Exception {
    }

    public void prePut(DatastoreService ds) throws Exception {
    }

    public void postPut(DatastoreService ds) throws Exception {
    }

    public void preDelete(DatastoreService ds) throws Exception {
    }

    public void postDelete(DatastoreService ds) throws Exception {
    }
}

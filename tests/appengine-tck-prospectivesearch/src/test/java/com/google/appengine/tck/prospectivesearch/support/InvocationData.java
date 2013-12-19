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

package com.google.appengine.tck.prospectivesearch.support;

import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tck.temp.AbstractTempData;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class InvocationData extends AbstractTempData {
    private transient Key lrd;
    Entity lastReceivedDocument;
    long resultsOffset;
    long resultsCount;
    List<String> subIds;
    String key;
    String topic;

    public InvocationData() {
        super();
    }

    protected void toProperties(DatastoreService ds, Map<String, Object> map) {
        if (lastReceivedDocument != null) {
            map.put("lrd", ds.put(lastReceivedDocument));
        }
        map.put("resultsOffset", resultsOffset);
        map.put("resultsCount", resultsCount);
        map.put("subIds", subIds);
        map.put("key", key);
        map.put("topic", topic);
    }

    @SuppressWarnings("unchecked")
    public void fromPropertiesInternal(Map<String, Object> propeties) {
        lrd = (Key) propeties.get("lrd");
        resultsOffset = (long) propeties.get("resultsOffset");
        resultsCount = (long) propeties.get("resultsCount");
        subIds = (List<String>) propeties.get("subIds");
        key = (String) propeties.get("key");
        topic = (String) propeties.get("topic");
    }

    public void postGet(DatastoreService ds) throws Exception {
        if (lrd != null) {
            lastReceivedDocument = ds.get(lrd);
        }
    }

    public void preDelete(DatastoreService ds) {
        Key ek = (lastReceivedDocument != null) ? lastReceivedDocument.getKey() : lrd;
        if (ek != null) {
            ds.delete(ek);
        }
    }

    public Entity getDocument() {
        return lastReceivedDocument;
    }

    public long getResultsOffset() {
        return resultsOffset;
    }

    public long getResultsCount() {
        return resultsCount;
    }

    public List<String> getSubIds() {
        return subIds;
    }

    public String getKey() {
        return key;
    }

    public String getTopic() {
        return topic;
    }
}

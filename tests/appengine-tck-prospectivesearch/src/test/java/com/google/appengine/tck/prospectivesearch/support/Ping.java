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

import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tck.temp.AbstractTempData;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Ping extends AbstractTempData {
    boolean invoked;
    Entity lastReceivedDocument;
    private Key lrd;

    protected void toProperties(DatastoreService ds, Map<String, Object> map) {
        map.put("invoked", invoked);
        map.put("lrd", (lastReceivedDocument != null) ? ds.put(lastReceivedDocument) : null);
    }

    public void fromPropertiesInternal(Map<String, Object> propeties) {
        invoked = (boolean) propeties.get("invoked");
        lrd = (Key) propeties.get("lrd");
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

    public boolean isInvoked() {
        return invoked;
    }

    public void setInvoked(boolean invoked) {
        this.invoked = invoked;
    }

    public Entity getLastReceivedDocument() {
        return lastReceivedDocument;
    }

    public void setLastReceivedDocument(Entity lastReceivedDocument) {
        this.lastReceivedDocument = lastReceivedDocument;
    }
}

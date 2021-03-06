/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.appengine.tck.misc.servlet.support;

import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.tck.temp.AbstractTempData;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WarmupData extends AbstractTempData {
    private String path;

    public WarmupData() {
    }

    public WarmupData(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    protected void toProperties(DatastoreService ds, Map<String, Object> map) {
        map.put("path", path);
    }

    protected void fromPropertiesInternal(Map<String, Object> propeties) {
        path = (String) propeties.get("path");
    }
}

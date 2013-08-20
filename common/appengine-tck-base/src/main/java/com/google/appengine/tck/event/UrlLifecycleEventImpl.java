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

package com.google.appengine.tck.event;

import java.net.URL;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class UrlLifecycleEventImpl extends TestLifecycleEventImpl implements UrlLifecycleEvent {
    private URL original;
    private URL https;

    UrlLifecycleEventImpl(Class<?> caller, URL original) {
        super(caller);
        this.original = original;
        this.https = original; // use original as https
    }

    public URL getOriginal() {
        return original;
    }

    public void setHttps(URL httpsURL) {
        this.https = httpsURL;
    }

    public URL getHttps() {
        return https;
    }
}

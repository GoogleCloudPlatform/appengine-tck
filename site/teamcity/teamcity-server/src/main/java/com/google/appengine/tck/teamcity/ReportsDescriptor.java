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

package com.google.appengine.tck.teamcity;

import jetbrains.buildServer.web.openapi.PluginDescriptor;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:kevin.pollet@serli.com.org">Kevin Pollet</a>
 */
public class ReportsDescriptor {
    private final PluginDescriptor descriptor;

    public ReportsDescriptor(final PluginDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public String getFeaturePath() {
        return descriptor.getPluginResourcesPath("feature.html");
    }

    public String getViewPath() {
        return descriptor.getPluginResourcesPath("feature.jsp");
    }
}

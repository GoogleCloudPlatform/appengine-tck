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

package com.google.appengine.tck.lib;

import java.io.File;

import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LibUtils {
    private PomEquippedResolveStage resolver;

    public void addGaeAsLibrary(LibraryContainer deployment) {
        addLibrary(deployment, "com.google.appengine:appengine-api-1.0-sdk");
    }

    public void addLibrary(LibraryContainer deployment, String groupId, String artifactId) {
        addLibrary(deployment, groupId + ":" + artifactId);
    }

    public void addLibrary(LibraryContainer deployment, String coordinate) {
        deployment.addAsLibraries(getDependency(coordinate));
    }

    // ------------

    protected PomEquippedResolveStage getResolver() {
        if (resolver == null)
            resolver = Maven.resolver().loadPomFromFile(buildPomPath());
        return resolver;
    }

    protected String buildPomPath() {
        return "pom.xml";
    }

    private File getDependency(final String coordinates) {
        return getResolver().resolve(coordinates).withoutTransitivity().asSingle(File.class);
    }
}

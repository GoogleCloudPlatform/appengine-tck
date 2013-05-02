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

package com.google.appengine.tck.transformers;

import java.io.File;
import java.util.Arrays;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ShrinkWrapTransformer extends JavassistTransformer {
    protected static PomEquippedResolveStage getResolver(String pom) {
        return Maven.resolver().loadPomFromFile(pom);
    }

    protected static File[] resolve(PomEquippedResolveStage resolver, String... coordinates) {
        final File[] files = resolver.resolve(coordinates).withoutTransitivity().as(File.class);
        if (files == null || files.length == 0)
            throw new IllegalArgumentException("Null or empty files (" + Arrays.toString(files) + "): " + Arrays.toString(coordinates));
        return files;
    }
}

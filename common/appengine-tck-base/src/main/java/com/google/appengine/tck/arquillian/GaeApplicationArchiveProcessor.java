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

package com.google.appengine.tck.arquillian;

import java.io.File;
import java.util.Set;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GaeApplicationArchiveProcessor extends AbstractApplicationArchiveProcessor {
    protected void handleWebArchive(WebArchive war) {
        final Node lib = war.get("WEB-INF/lib");
        if (lib != null) {
            final Set<Node> libs = lib.getChildren();
            for (Node jar : libs) {
                if (jar.getPath().get().contains("appengine-api"))
                    return;
            }
        }

        // do not add GAE jar; e.g. CapeDwarf can work off GAE module
        boolean ignoreGaeJar = Boolean.getBoolean("ignore.gae.jar");
        if (ignoreGaeJar == false) {
            war.addAsLibraries(Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("com.google.appengine:appengine-api-1.0-sdk")
                .withTransitivity()
                .as(File.class)
            );
        }
    }
}

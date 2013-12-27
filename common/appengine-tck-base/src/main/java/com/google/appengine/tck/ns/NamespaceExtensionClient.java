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

package com.google.appengine.tck.ns;

import java.util.logging.Logger;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.kohsuke.MetaInfServices;

/**
 * @author Aslak Knutsen
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class NamespaceExtensionClient implements LoadableExtension {
    private final boolean ignoreWithinNamespace;

    public NamespaceExtensionClient() {
        ignoreWithinNamespace = Boolean.getBoolean("tck.ignore.within.namespace");
        if (ignoreWithinNamespace) {
            Logger.getLogger(NamespaceExtensionClient.class.getName()).info("@WithinNamespace is ignored.");
        }
    }

    public void register(ExtensionBuilder builder) {
        if (ignoreWithinNamespace == false) {
            builder.service(AuxiliaryArchiveAppender.class, NamespaceArchiveAppender.class);
        }
    }
}

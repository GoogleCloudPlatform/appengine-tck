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

package com.google.appengine.tck.multisuite;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.google.appengine.tck.multisuite.scan.ScanMultiProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MultiDeployment {
    @Deployment
    public static WebArchive getDeployment() throws Exception {
        final ClassLoader cl = MultiDeployment.class.getClassLoader();
        final URL arqXml = cl.getResource("multisuite.marker");
        if (arqXml == null) {
            throw new IllegalArgumentException("Missing multisuite.marker?!");
        }

        Properties overrides = new Properties();
        try (InputStream is = arqXml.openStream()) {
            overrides.load(is);
        }

        String name = overrides.getProperty("deployment.name", "gae-multisuite-tck.war");
        WebArchive war = ShrinkWrap.create(WebArchive.class, name);
        File root = new File(arqXml.toURI()).getParentFile();

        MultiContext mc = new MultiContext(war, root, cl);

        MultiProvider provider = new ScanMultiProvider(overrides);
        provider.provide(mc);

        return war;
    }
}

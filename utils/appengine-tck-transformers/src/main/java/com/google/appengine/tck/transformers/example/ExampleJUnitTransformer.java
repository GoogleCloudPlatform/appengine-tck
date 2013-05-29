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

package com.google.appengine.tck.transformers.example;

import com.google.appengine.tck.transformers.ArquillianJUnitTransformer;
import com.google.appengine.tck.transformers.MatchingClassFileTransformer;

import javassist.CtClass;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * A simple example of how to enable an existing test that does not follow the TCK framework.
 *
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public class ExampleJUnitTransformer  extends ArquillianJUnitTransformer implements MatchingClassFileTransformer {

    // See the maven-transformer-plugin configuration in the pom.xml.
    public boolean match(String className) {
        return className.endsWith("Test") || className.endsWith("TestCase");
    }

    protected String getDeploymentMethodBody(CtClass clazz) throws Exception {
        return "{return com.google.appengine.tck.transformers.example.ExampleJUnitTransformer.buildArchive(\"" + clazz.getName() + "\");}";
    }

    public static WebArchive buildArchive(String clazz) {
        WebArchive war = createWar();
        addClasses(war, clazz);

        // Your test suite is most likely built separately from the TCK.  You would include all the
        // dependencies in the pom.xml, and then declare the packages and classes here.
        war.addPackage("com.google.appengine.sometests");

        // Include this even though you may not have an appengine-web.xml for your tests.
        war.addAsWebInfResource("appengine-web.xml");

        final PomEquippedResolveStage resolver = getResolver("pom.xml");

        // Necessary to run under App Engine.
        war.addAsLibraries(resolve(resolver, "com.google.appengine:appengine-api-1.0-sdk"));

        // TCK Internals necessary for any tests to run under the TCK.
        war.addAsLibraries(resolve(resolver, "com.google.appengine.tck:appengine-tck-transformers")); // cleanup dep
        war.addAsLibraries(resolve(resolver, "com.google.appengine.tck:appengine-tck-base")); // lifecycle dep

        return war;
    }

    private static void addClasses(WebArchive war, String clazz) {
        try {
            ClassLoader cl = ExampleJUnitTransformer.class.getClassLoader();
            Class<?> current = cl.loadClass(clazz);
            addClasses(war, current);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addClasses(WebArchive war, Class<?> current) {
        while (current != null && current != Object.class && "junit.framework.TestCase".equals(current.getName()) == false) {
            war.addClass(current);
            current = current.getSuperclass();
        }
    }

    @Override
    protected String tearDownSrc() {
        return "com.google.appengine.tck.transformers.TestUtils.clean();" + super.tearDownSrc();
    }

}

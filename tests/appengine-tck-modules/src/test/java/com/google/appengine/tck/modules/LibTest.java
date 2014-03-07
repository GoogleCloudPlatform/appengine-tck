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

package com.google.appengine.tck.modules;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tck.lib.LibUtils;
import com.google.appengine.tck.modules.support.LibHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class LibTest extends ModulesTestBase {
    @Deployment
    public static EnterpriseArchive getDeployment() {
        WebArchive module1 = getTckSubDeployment(1);
        module1.addClass(LibTest.class);

        // dummy module
        WebArchive module2 = getTckSubDeployment(2);

        final EnterpriseArchive ear = getEarDeployment(module1, module2);

        JavaArchive lib = ShrinkWrap.create(JavaArchive.class);
        lib.addClass(LibHelper.class);

        new LibUtils().addGaeAsLibrary(ear);
        ear.addAsLibraries(lib);

        return ear;
    }

    @Test // check if module and lib/ GAE classes play nicely
    @Ignore // Ignore for now, as SDK doesn't know how to handle lib/
    public void testLibCompatibility() throws Exception {
        final Key key = LibHelper.put(new Entity("LibTest"));
        Assert.assertNotNull(key);
        try {
            Entity entity = LibHelper.get(key);
            Assert.assertNotNull(entity);
        } finally {
            LibHelper.delete(key);
        }
    }
}

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.labs.modules.ModulesService;
import com.google.appengine.api.labs.modules.ModulesServiceFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.protocol.modules.OperateOnModule;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class ModulesServiceTest extends ModulesTestBase {
    @Deployment
    public static EnterpriseArchive getDeployment() {
        WebArchive module1 = getTckSubDeployment(1).addClass(ModulesServiceTest.class);
        addModulesLib(module1);

        WebArchive module2 = getTckSubDeployment(2).addClass(ModulesServiceTest.class);
        addModulesLib(module2);

        return getEarDeployment(module1, module2);
    }

    private static Set<String> toSet(String... strings) {
        return new HashSet<>(Arrays.asList(strings));
    }

    @Test
    public void testCurrentModule1() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals("default", service.getCurrentModule());
    }

    @Test
    @OperateOnModule("m2")
    public void testCurrentModule2() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals("m2", service.getCurrentModule());
    }

    @Test
    public void testCurrentVersion1() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals("1", service.getCurrentVersion());
    }

    @Test
    @OperateOnModule("m2")
    public void testCurrentVersion2() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals("1", service.getCurrentVersion());
    }

    @Test
    public void testDefaultVersion1() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals("1", service.getDefaultVersion("default"));
    }

    @Test
    @OperateOnModule("m2")
    public void testDefaultVersion2() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals("1", waitOnFuture(service.getDefaultVersionAsync("m2")));
    }

    @Test
    public void testModules1() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals(toSet("default", "m2"), service.getModules());
    }

    @Test
    @OperateOnModule("m2")
    public void testModules2() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals(toSet("default", "m2"), waitOnFuture(service.getModulesAsync()));
    }

    @Test
    public void testModuleInstances1() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals(1L, service.getNumInstances("default", "1"));
    }

    @Test
    @OperateOnModule("m2")
    public void testModuleInstances2() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals(Long.valueOf(1L), waitOnFuture(service.getNumInstancesAsync("m2", "1")));
    }

    @Test
    public void testModuleVersions1() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals(toSet("1"), service.getVersions("m2"));
    }

    @Test
    @OperateOnModule("m2")
    public void testModuleVersions2() {
        ModulesService service = ModulesServiceFactory.getModulesService();
        Assert.assertEquals(toSet("1"), waitOnFuture(service.getVersionsAsync("default")));
    }
}

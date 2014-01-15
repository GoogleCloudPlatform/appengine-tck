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

package com.google.appengine.tck.endpoints;

import java.net.URL;

import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.endpoints.support.RpcEndpoint;
import com.google.appengine.tck.endpoints.support.TestData;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Gregor Sfiligoj
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class RpcTest extends EndPointsTestBase {

    @Drone
    private WebDriver driver;

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext().setWebXmlFile("rpc-web.xml");
        WebArchive war = getDefaultDeployment(context);
        war.addClasses(RpcEndpoint.class, TestData.class);
        war.add(new ClassLoaderAsset("xindex.html"), "index.html");
        war.add(new ClassLoaderAsset("js/base.js"), "js/base.js");
        war.addAsWebInfResource("rpcendpoint-v1-rest.discovery");
        war.addAsWebInfResource("rpcendpoint-v1-rpc.discovery");
        return war;
    }

    @Test
    @RunAsClient
    public void testRpc(@ArquillianResource URL url) throws Exception {
        driver.get(url.toExternalForm());

        sync();

        WebElement getButton = driver.findElement(By.id("getButton"));
        getButton.click();

        sync();

        WebElement response = driver.findElement(By.id("response"));
        Assert.assertTrue(String.format("Expecting non-empty response!"), response.getText().trim().length() > 0);
    }
}

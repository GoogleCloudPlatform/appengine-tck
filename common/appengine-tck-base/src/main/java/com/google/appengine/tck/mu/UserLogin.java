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

package com.google.appengine.tck.mu;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import java.net.URL;

public class UserLogin {

    @Drone
    private WebDriver driver;

    @ArquillianResource
    private URL url;

    @RunAsClient
    public void login(@Observes Before event, TestClass testClass) throws NoSuchFieldException, IllegalAccessException {
        if (testClass.isAnnotationPresent(UserIsLoggedIn.class)) {
            UserIsLoggedIn annotation = testClass.getAnnotation(UserIsLoggedIn.class);
            String email = annotation.email();

            driver.manage().deleteAllCookies();
            driver.get(url + "/user-service-email?location=https://appengine.google.com/");

            try {
                String loginUrl = driver.findElement(By.tagName("pre")).getText();
                driver.get(url + loginUrl);

                driver.findElement(By.id("email")).clear();
                driver.findElement(By.id("email")).sendKeys(email);

                // driver.findElement(By.id("isAdmin")).click();
                driver.findElements(By.name("action")).get(0).click();
            } catch (NoSuchElementException nsee) {
                String errMsg = driver.getCurrentUrl() + " ----- " + driver.getPageSource();
                throw new NoSuchElementException(nsee.toString() + " ----- " + errMsg);
            }
        }
    }
}

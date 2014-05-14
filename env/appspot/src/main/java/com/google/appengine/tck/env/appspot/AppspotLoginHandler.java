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

package com.google.appengine.tck.env.appspot;

import com.google.appengine.tck.driver.LoginContext;
import com.google.appengine.tck.driver.LoginHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AppspotLoginHandler implements LoginHandler {
    public void login(WebDriver driver, LoginContext context) {
        WebElement email = driver.findElement(By.id("Email"));
        if (email.getAttribute("readonly") == null) {
            email.clear();
            email.sendKeys(context.getEmail());
        }

        WebElement password = driver.findElement(By.id("Passwd"));
        password.sendKeys(context.getPassword());

        driver.findElement(By.name("signIn")).click();
    }
}

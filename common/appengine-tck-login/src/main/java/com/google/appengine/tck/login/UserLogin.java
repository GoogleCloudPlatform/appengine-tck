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

package com.google.appengine.tck.login;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Set;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.driver.DefaultLoginHandler;
import com.google.appengine.tck.driver.LoginHandler;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.test.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.container.test.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.protocol.modules.ModulesApi;
import org.jboss.arquillian.protocol.modules.ModulesProtocolConfiguration;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * @author Julien Deray
 * @author Ales Justin
 */
public class UserLogin {
    static final String USER_LOGIN_SERVLET_PATH = "user-login-builtin";

    @Inject
    private Instance<ProtocolRegistry> registry;

    private ProtocolMetaData protocolMetaData;

    public void prepare(@Observes AfterDeploy event, ProtocolMetaData data) {
        this.protocolMetaData = data;
    }

    protected URI getBaseURI(Method method) throws Exception {
        ProtocolDefinition definition = registry.get().getProtocol(ProtocolDescription.DEFAULT);
        ModulesProtocolConfiguration configuration = (ModulesProtocolConfiguration) definition.createProtocolConfiguration();
        HTTPContext context = ModulesApi.findHTTPContext(configuration, protocolMetaData, method);
        return context.getServlets().get(0).getBaseURI();
    }

    public void login(@Observes EventContext<Before> event) throws Exception {
        Before before = event.getEvent();

        UserIsLoggedIn userIsLoggedIn = null;
        if (before.getTestMethod().isAnnotationPresent(UserIsLoggedIn.class)) {
            userIsLoggedIn = before.getTestMethod().getAnnotation(UserIsLoggedIn.class);
        } else if (before.getTestClass().isAnnotationPresent(UserIsLoggedIn.class)) {
            userIsLoggedIn = before.getTestClass().getAnnotation(UserIsLoggedIn.class);
        }

        if (userIsLoggedIn != null) {
            final URI baseUri = getBaseURI(before.getTestMethod());
            final WebDriver driver = createWebDriver();
            try {
                driver.manage().deleteAllCookies();

                driver.navigate().to(baseUri + USER_LOGIN_SERVLET_PATH + "?location=" + URLEncoder.encode(userIsLoggedIn.location(), "UTF-8"));
                String loginURL = driver.findElement(By.id("login-url")).getText();

                // go-to login page
                driver.navigate().to(baseUri + loginURL);
                // find custom login handler, if exists
                LoginHandler loginHandler = TestBase.instance(getClass(), LoginHandler.class);
                if (loginHandler == null) {
                    loginHandler = new DefaultLoginHandler();
                }
                loginHandler.login(driver, new UserLoginContext(userIsLoggedIn));
                // copy cookies
                Set<Cookie> cookies = driver.manage().getCookies();
                for (Cookie cookie : cookies) {
                    ModulesApi.addCookie(cookie.getName(), cookie.getValue());
                }
            } finally {
                driver.close();
            }
        }

        event.proceed();
    }

    private WebDriver createWebDriver() {
        return new HtmlUnitDriver();
    }
}

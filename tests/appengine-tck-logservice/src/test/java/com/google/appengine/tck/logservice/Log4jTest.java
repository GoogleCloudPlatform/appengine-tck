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

package com.google.appengine.tck.logservice;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Make sure log4j.properties file doesn't break deployment.
 *
 * @author Ales Justin
 */
@RunWith(Arquillian.class)
public class Log4jTest extends LoggingTestBase {
    @Deployment
    public static WebArchive getDeployment() throws Exception {
        WebArchive war = getDefaultDeployment(newTestContext());
        war.addAsResource("log4j-test.properties", "log4j.properties");
        return war;
    }

    @Test
    public void testSmoke() {
    }
}

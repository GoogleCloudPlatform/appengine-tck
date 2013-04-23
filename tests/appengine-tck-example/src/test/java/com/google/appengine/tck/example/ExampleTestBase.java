/*
    * Copyright (C) 2013 Google Inc.
    *
    * Licensed under the Apache License, Version 2.0 (the "License"); you may not
    * use this file except in compliance with the License. You may obtain a copy of
    * the License at
    *
    * http://www.apache.org/licenses/LICENSE-2.0
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    * License for the specific language governing permissions and limitations under
    * the License.
*/

package com.google.appengine.tck.example;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;


/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public abstract class ExampleTestBase extends TestBase {

    /**
      Every test package needs to declare a *TestBase class which
      getDeployment() declares the resources needed to run the test.
      Basically this is how you specify the WEB-INF files, and the
      test classes themselves.

      Note that tools may indicate that getDeployment() is not used
      since it is called dynamically.
     */
    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext();

        // Add a custom web.xml.  These resource files are located:
        // in tests/appengine-tck-the-test/src/test/resource
        //
        // context.setWebXmlFile("web-taskqueue.xml");

        WebArchive war = getTckDeployment(context);

        // Add all the test class files here.  Maven knows how to compile the
        // tests, declaring them here is for the arquillian framework.
        //
        war.addClass(TestBase.class);
        war.addClass(ExampleTestBase.class);
        war.addClasses(SimpleExampleTest.class, Simple2ExampleTest.class);
        war.addAsWebResource("examplePage.jsp");

        // Add a taskqueue configuration.
        //
        // war.addAsWebInfResource("queue.xml");

      return war;
    }
}

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

package com.google.appengine.tck.sql;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.sql.support.SqlUtil;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;


/**
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public abstract class CloudSqlTestBase extends TestBase {

    @Deployment
    public static WebArchive getDeployment() {

        // -Dtck.sql.connection=//my_project:test_instance
        // -Dtck.sql.user
        // -Dtck.sql.pw

        TestContext context = new TestContext().setUseSystemProperties(true)
            .setCompatibilityProperties(TCK_PROPERTIES);

        // Declares usage of com.mysql.jdbc.GoogleDriver
        context.setAppEngineWebXmlFile("sql-appengine-web.xml");

        WebArchive war = getTckDeployment(context);
        war.addClass(TestBase.class);
        war.addClass(CloudSqlTestBase.class);
        war.addClasses(SqlGaeDriverTest.class, SqlUtil.class);

        return war;
    }
}

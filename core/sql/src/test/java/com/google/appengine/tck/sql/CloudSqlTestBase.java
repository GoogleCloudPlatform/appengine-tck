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

import java.sql.Connection;
import java.sql.SQLException;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import com.google.appengine.tck.sql.support.SqlUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

import static java.util.logging.Level.WARNING;


/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class CloudSqlTestBase extends TestBase {
    protected String connectionStr;
    protected String user;
    protected String pw;

    @Deployment
    public static WebArchive getDeployment() {
        // -Dtck.sql.connection=//my_project:test_instance
        // -Dtck.sql.user
        // -Dtck.sql.pw

        TestContext context = new TestContext().setUseSystemProperties(true).setCompatibilityProperties(TCK_PROPERTIES);

        // Declares usage of com.mysql.jdbc.GoogleDriver
        context.setAppEngineWebXmlFile("sql-appengine-web.xml");

        WebArchive war = getTckDeployment(context);
        war.addClasses(CloudSqlTestBase.class, SqlUtil.class);

        return war;
    }

    @Before
    public void initConnectionProperties() {
        connectionStr = getTestSystemProperty("tck.sql.connection");
        user = getTestSystemProperty("tck.sql.user");
        pw = getTestSystemProperty("tck.sql.pw");

        if (connectionStr == null && isRequired("initConnectionProperties", "tck.sql.connection")) {
            throw new IllegalStateException("-Dtck.sql.connection is not defined.");
        }
        if (user == null && isRequired("initConnectionProperties", "tck.sql.user")) {
            throw new IllegalStateException("-Dtck.sql.user is not defined.");
        }
        if (pw == null) {
            pw = "";
        }

        log.info("tck.sql.connection = " + connectionStr);
    }

    protected boolean doIgnore(String context) {
        return (execute(context) == false);
    }

    protected boolean isRequired(String context, String property) {
        return (required(property) && (doIgnore(context) == false));
    }

    protected void createAndAssertTable(Connection conn, String tableName) throws Exception {
        SqlUtil.createTable(conn, tableName);
        try {
            SqlUtil.insertValues(conn, tableName);

            SqlUtil.assertTableValues(conn, tableName);
        } finally {
            SqlUtil.dropTable(conn, tableName);
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    log.log(WARNING, "Unable to close db connection", ex);
                }
            }
        }
    }

}

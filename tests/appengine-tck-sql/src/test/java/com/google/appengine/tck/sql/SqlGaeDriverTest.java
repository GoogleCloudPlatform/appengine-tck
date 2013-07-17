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

import static java.util.logging.Level.WARNING;

import com.google.appengine.api.rdbms.AppEngineDriver;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tck.sql.support.SqlUtil;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * See https://developers.google.com/appengine/docs/java/cloud-sql/developers-guide
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class SqlGaeDriverTest extends CloudSqlTestBase {

    private String connectionStr;
    private String user;
    private String pw;

    private void initConnectionProperties() {
        try {
            connectionStr = readProperties(TCK_PROPERTIES).getProperty("tck.sql.connection");
            user = readProperties(TCK_PROPERTIES).getProperty("tck.sql.user");
            pw = readProperties(TCK_PROPERTIES).getProperty("tck.sql.pw");

            if (connectionStr == null) {
                throw new IllegalStateException("-Dtck.sql.connection is not defined.");
            }
            if (user == null) {
                throw new IllegalStateException("-Dtck.sql.user is not defined.");
            }
            if (pw == null) {
                pw = "";
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }

        log.info("tck.sql.connection = " + connectionStr);
    }

    @Before
    public void setUp() {
        initConnectionProperties();
    }

    @Test
    public void testConnectionAppEngineDriver() throws Exception {
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
            log.info("This test is not intended to run with the SDK.");
            return;
        }

        String fullConnectionStr = String.format("jdbc:google:rdbms://%s?user=%s", connectionStr, user);
        DriverManager.registerDriver(new AppEngineDriver());

        Connection conn;
        conn = DriverManager.getConnection(fullConnectionStr);

        String tableName = "tck_test_" + System.currentTimeMillis();
        createAndAssertTable(conn, tableName);
    }

    @Test
    public void testConnectionMySqlDriver() throws Exception {
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
            log.info("This test is not intended to run with the SDK.");
            return;
        }

        String fullConnectionStr = String.format("jdbc:google:mysql://%s?user=%s&password=%s",
            connectionStr, user, pw);

        // This needs to be enabled in appengine-web.xml to make the jdbc driver available.
        //   <use-google-connector-j>true</use-google-connector-j>
        Class.forName("com.mysql.jdbc.GoogleDriver");

        Connection conn;
        conn = DriverManager.getConnection(fullConnectionStr);

        String tableName = "tck_test_" + System.currentTimeMillis();
        createAndAssertTable(conn, tableName);
    }

    private void createAndAssertTable(Connection conn, String tableName) throws Exception {

        SqlUtil.createTable(conn, tableName);
        SqlUtil.insertValues(conn, tableName);

        SqlUtil.assertTableValues(conn, tableName);

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

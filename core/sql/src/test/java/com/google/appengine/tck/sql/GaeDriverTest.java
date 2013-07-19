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
import java.sql.Driver;
import java.sql.DriverManager;

import com.google.appengine.api.rdbms.AppEngineDriver;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * See https://developers.google.com/appengine/docs/java/cloud-sql/developers-guide
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class GaeDriverTest extends CloudSqlTestBase {
    @Test
    public void testConnectionAppEngineDriver() throws Exception {
        if (doIgnore("testConnectionAppEngineDriver")) {
            log.info("This test is not intended to run with the dev env.");
            return;
        }

        final Driver driver = new AppEngineDriver();

        DriverManager.registerDriver(driver);
        try {
            String fullConnectionStr = String.format("jdbc:google:rdbms://%s?user=%s", connectionStr, user);
            Connection conn = DriverManager.getConnection(fullConnectionStr);

            String tableName = "tck_test_" + System.currentTimeMillis();
            createAndAssertTable(conn, tableName);
        } finally {
            DriverManager.deregisterDriver(driver);
        }
    }
}

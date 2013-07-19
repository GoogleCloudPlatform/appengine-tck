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

package com.google.appengine.tck.sql.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Create a sql table with all data types.
 *
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class SqlUtil {
    // Column names of the various data types
    private static final String TINYINT_COLUMN = "TINYINT_VALUE";
    private static final String TINYINTUNSIGNED_COLUMN = "TINYINTUNSIGNED_VALUE";
    private static final String SMALLINT_COLUMN = "SMALLINT_VALUE";
    private static final String SMALLINTUNSIGNED_COLUMN = "SMALLINTUNSIGNED_VALUE";
    private static final String MEDIUMINT_COLUMN = "MEDIUMINT_VALUE";
    private static final String MEDIUMINTUNSIGNED_COLUMN = "MEDIUMINTUNSIGNED_VALUE";
    private static final String INT_COLUMN = "INT_VALUE";
    private static final String INTUNSIGNED_COLUMN = "INTUNSIGNED_VALUE";
    private static final String BIGINT_COLUMN = "BIGINT_VALUE";
    private static final String BIGINTUNSIGNED_COLUMN = "BIGINTUNSIGNED_VALUE";

    private static final String CHAR_COLUMN = "CHAR_VALUE";
    private static final String VARCHAR_COLUMN = "VARCHAR_VALUE";

    private static final String BOOL_COLUMN = "BOOL_VALUE";
    private static final String BOOLINT_COLUMN = "BOOLINT_VALUE";

    private static final String FLOAT_COLUMN = "FLOAT_VALUE";
    private static final String DOUBLE_COLUMN = "DOUBLE_VALUE";
    private static final String DECIMAL_COLUMN = "DECIMAL_VALUE";

    private static final String DATE_COLUMN = "DATE_VALUE";
    private static final String DATETIME_COLUMN = "DATETIME_VALUE";
    private static final String TIMESTAMP_COLUMN = "TIMESTAMP_VALUE";

    // Values to be inserted for the data types
    // TODO -- not all DBs support unsigned, hence values still in the limit of basic types
    private static final int TINYINT_VALUE = 127;
    private static final int TINYINTUNSIGNED_VALUE = 127;
    private static final int SMALLINT_VALUE = 32767;
    private static final int SMALLINTUNSIGNED_VALUE = 32767;
    private static final int MEDIUMINT_VALUE = 8388607;
    private static final int MEDIUMINTUNSIGNED_VALUE = 8388607;
    private static final int INT_VALUE = 2147483647;
    private static final long INTUNSIGNED_VALUE = 2147483647L;
    private static final long BIGINT_VALUE = 9223372036854775807L;
    private static final BigInteger BIGINTUNSIGNED_VALUE = new BigInteger("9223372036854775807");

    private static final String CHAR_VALUE = "temp_char";
    private static final String VARCHAR_VALUE = "temp_varchar";

    private static final Boolean BOOL_VALUE = true;
    private static final int BOOLINT_VALUE = 1;

    private static final float FLOAT_VALUE = new Float(3.40282e+38);
    private static final double DOUBLE_VALUE = 9E253;
    private static final BigDecimal DECIMAL_VALUE = new BigDecimal(100000);

    private static final Date DATE_VALUE = Date.valueOf("9999-12-31");
    private static final Timestamp DATETIME_VALUE = Timestamp.valueOf("9999-12-31 23:59:59");
    private static final Timestamp TIMESTAMP_VALUE = Timestamp.valueOf("2037-12-31 23:59:59");

    private static String getCreateTableCommand(String tableName) {
        return String.format(
            "CREATE TABLE %s (%s TINYINT, %s TINYINT UNSIGNED, %s SMALLINT, %s SMALLINT UNSIGNED," +
                " %s MEDIUMINT, %s MEDIUMINT UNSIGNED, %s INT, %s INT UNSIGNED, %s BIGINT," +
                " %s BIGINT UNSIGNED, %s CHAR(255), %s VARCHAR(255), %s BIT, %s BIT, %s FLOAT," +
                " %s DOUBLE, %s DECIMAL, %s DATE, %s DATETIME, %s TIMESTAMP)",
            tableName,
            TINYINT_COLUMN,
            TINYINTUNSIGNED_COLUMN,
            SMALLINT_COLUMN,
            SMALLINTUNSIGNED_COLUMN,
            MEDIUMINT_COLUMN,
            MEDIUMINTUNSIGNED_COLUMN,
            INT_COLUMN,
            INTUNSIGNED_COLUMN,
            BIGINT_COLUMN,
            BIGINTUNSIGNED_COLUMN,
            CHAR_COLUMN,
            VARCHAR_COLUMN,
            BOOL_COLUMN,
            BOOLINT_COLUMN,
            FLOAT_COLUMN,
            DOUBLE_COLUMN,
            DECIMAL_COLUMN,
            DATE_COLUMN,
            DATETIME_COLUMN,
            TIMESTAMP_COLUMN
        );

    }

    private static void executeStatement(Connection conn, String command) throws SQLException {
        java.sql.Statement statement = conn.createStatement();
        try {
            statement.executeUpdate(command);
        } finally {
            statement.close();
        }
    }

    public static void dropTable(Connection conn, String tableName) throws SQLException {
        executeStatement(conn, String.format("DROP TABLE IF EXISTS %s", tableName));
    }

    public static void createTable(Connection conn, String tableName) throws SQLException {
        dropTable(conn, tableName);
        executeStatement(conn, getCreateTableCommand(tableName));
    }

    public static void insertValues(Connection conn, String tableName) throws SQLException {
        String insertCommand = String.format(
            "INSERT INTO %s VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", tableName);
        PreparedStatement ps = conn.prepareStatement(insertCommand);

        try {
            ps.setInt(1, TINYINT_VALUE);
            ps.setInt(2, TINYINTUNSIGNED_VALUE);
            ps.setInt(3, SMALLINT_VALUE);
            ps.setInt(4, SMALLINTUNSIGNED_VALUE);
            ps.setInt(5, MEDIUMINT_VALUE);
            ps.setInt(6, MEDIUMINTUNSIGNED_VALUE);
            ps.setInt(7, INT_VALUE);
            ps.setLong(8, INTUNSIGNED_VALUE);
            ps.setLong(9, BIGINT_VALUE);
            ps.setString(10, BIGINTUNSIGNED_VALUE.toString());

            ps.setString(11, CHAR_VALUE);
            ps.setString(12, VARCHAR_VALUE);

            ps.setBoolean(13, BOOL_VALUE);
            ps.setInt(14, BOOLINT_VALUE);

            ps.setFloat(15, FLOAT_VALUE);
            ps.setDouble(16, DOUBLE_VALUE);
            ps.setBigDecimal(17, DECIMAL_VALUE);

            ps.setDate(18, DATE_VALUE);
            ps.setTimestamp(19, DATETIME_VALUE);
            ps.setTimestamp(20, TIMESTAMP_VALUE);

            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public static void assertTableValues(Connection conn, String tableName) throws SQLException {

        ResultSet rs = null;
        try {
            String selectCommand = String.format("SELECT * FROM %s", tableName);
            rs = conn.createStatement().executeQuery(selectCommand);

            assertTrue(rs.next());

            assertEquals(TINYINT_VALUE, rs.getInt(TINYINT_COLUMN));
            assertEquals(TINYINTUNSIGNED_VALUE, rs.getInt(TINYINTUNSIGNED_COLUMN));
            assertEquals(SMALLINT_VALUE, rs.getInt(SMALLINT_COLUMN));
            assertEquals(SMALLINTUNSIGNED_VALUE, rs.getInt(SMALLINTUNSIGNED_COLUMN));
            assertEquals(MEDIUMINT_VALUE, rs.getInt(MEDIUMINT_COLUMN));
            assertEquals(MEDIUMINTUNSIGNED_VALUE, rs.getInt(MEDIUMINTUNSIGNED_COLUMN));
            assertEquals(INT_VALUE, rs.getInt(INT_COLUMN));
            assertEquals(INTUNSIGNED_VALUE, rs.getLong(INTUNSIGNED_COLUMN));
            assertEquals(BIGINT_VALUE, rs.getLong(BIGINT_COLUMN));
            assertEquals(BIGINTUNSIGNED_VALUE, new BigInteger(rs.getString(BIGINTUNSIGNED_COLUMN)));

            assertEquals(CHAR_VALUE, rs.getString(CHAR_COLUMN));
            assertEquals(VARCHAR_VALUE, rs.getString(VARCHAR_COLUMN));

            assertEquals(BOOL_VALUE, rs.getBoolean(BOOL_COLUMN));
            assertEquals(BOOLINT_VALUE, rs.getInt(BOOLINT_COLUMN));

            assertEquals(FLOAT_VALUE, rs.getFloat(FLOAT_COLUMN), 0.0001);
            assertEquals(DOUBLE_VALUE, rs.getDouble(DOUBLE_COLUMN), 0.0001);
            assertEquals(DECIMAL_VALUE, rs.getBigDecimal(DECIMAL_COLUMN));

            assertEquals(DATE_VALUE, rs.getDate(DATE_COLUMN));
            assertEquals(DATETIME_VALUE, rs.getTimestamp(DATETIME_COLUMN));
            assertEquals(TIMESTAMP_VALUE, rs.getTimestamp(TIMESTAMP_COLUMN));

            assertFalse(rs.next());
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }
}

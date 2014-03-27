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

package com.google.appengine.tck.datastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN_OR_EQUAL;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)

public class QueryFilteringByBasicPropertyTypesTest extends QueryTestBase {

    @Test
    public void testBooleanProperty() throws Exception {
        testEqualityQueries(Boolean.TRUE, Boolean.FALSE);
    }

    @Test
    public void testInequalityFilterWithNegativeInteger() {
        String methodName = "testInequalityFilterWithNegativeInteger";
        Entity parentEntity = createTestEntityWithUniqueMethodNameKey(TEST_ENTITY_KIND, methodName);
        Key key = parentEntity.getKey();

        Entity minus2 = buildTestEntity(key).withProperty("prop", -2).store();
        Entity minus1 = buildTestEntity(key).withProperty("prop", -1).store();
        Entity zero = buildTestEntity(key).withProperty("prop", 0).store();
        Entity plus1 = buildTestEntity(key).withProperty("prop", 1).store();
        Entity plus2 = buildTestEntity(key).withProperty("prop", 2).store();

        assertSet(queryReturns(zero, plus1, plus2), whenFilteringBy(GREATER_THAN, -1, key));
        assertSet(queryReturns(minus2, minus1), whenFilteringBy(LESS_THAN_OR_EQUAL, -1, key));

        clearData(TEST_ENTITY_KIND, key, 0);
    }

    @Test
    public void testIntegerProperty() {
        testEqualityQueries(1, 2);
        testInequalityQueries(2, 10, 30);
        testInequalityQueries(-3, -2, -1);
    }

    @Test
    public void testByteProperty() {
        testEqualityQueries((byte) 1, (byte) 2);
        testInequalityQueries((byte) 2, (byte) 10, (byte) 30);
        testInequalityQueries((byte) -3, (byte) -2, (byte) -1);
    }

    @Test
    public void testShortProperty() {
        testEqualityQueries((short) 1, (short) 2);
        testInequalityQueries((short) 2, (short) 10, (short) 30);
        testInequalityQueries((short) -3, (short) -2, (short) -1);
    }

    @Test
    public void testLongProperty() {
        testEqualityQueries(1L, 2L);
        testInequalityQueries(2L, 10L, 30L);
        testInequalityQueries(-3L, -2L, -1L);
    }

    @Test
    public void testFloatProperty() {
        testEqualityQueries(1f, 2f);
        testInequalityQueries(2f, 10f, 30f);
        testInequalityQueries(-3f, -2f, -1f);
    }

    @Test
    public void testDoubleProperty() {
        testEqualityQueries(1.0, 2.0);
        testInequalityQueries(2.0, 10.0, 30.0);
        testInequalityQueries(-3.0, -2.0, -1.0);
    }

}

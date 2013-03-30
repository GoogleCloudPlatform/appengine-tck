/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package com.google.appengine.tck.datastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN_OR_EQUAL;
import static org.junit.Assert.assertThat;

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

        assertThat(whenFilteringBy(GREATER_THAN, -1, key), queryReturns(zero, plus1, plus2));
        assertThat(whenFilteringBy(LESS_THAN_OR_EQUAL, -1, key), queryReturns(minus2, minus1));

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

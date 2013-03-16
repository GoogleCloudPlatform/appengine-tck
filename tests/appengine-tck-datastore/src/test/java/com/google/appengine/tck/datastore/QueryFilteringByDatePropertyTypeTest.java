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

import java.util.Arrays;
import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;

import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.IN;
import static org.junit.Assert.assertThat;

/**
 * Datastore querying tests.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)

public class QueryFilteringByDatePropertyTypeTest extends QueryTestBase {


    @Test
    public void queryByEqualReturnsEntityWithEqualPropertyValue() throws Exception {
        testEqualityQueries(createDate(2011, 10, 15), createDate(2011, 10, 16));
        testInequalityQueries(createDate(2011, 1, 1), createDate(2011, 1, 15), createDate(2011, 2, 1));
    }

    @Test
    public void testQueryByIn() throws Exception {
        Date october15 = createDate(2011, 10, 15);
        Date october20 = createDate(2011, 10, 20);
        Date november1 = createDate(2011, 11, 1);
        Entity october15Entity = storeTestEntityWithSingleProperty(october15);
        Entity october20Entity = storeTestEntityWithSingleProperty(october20);
        Entity november1Entity = storeTestEntityWithSingleProperty(november1);

        assertThat(whenFilteringBy(IN, Arrays.asList(october15, november1)), queryReturns(october15Entity, november1Entity));
    }
}

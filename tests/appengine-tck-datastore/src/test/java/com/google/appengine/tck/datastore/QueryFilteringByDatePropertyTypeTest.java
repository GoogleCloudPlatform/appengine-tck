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

import java.util.Arrays;
import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.datastore.Query.FilterOperator.IN;

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

        assertSet(whenFilteringBy(IN, Arrays.asList(october15, november1)), queryReturns(october15Entity, november1Entity));
    }
}

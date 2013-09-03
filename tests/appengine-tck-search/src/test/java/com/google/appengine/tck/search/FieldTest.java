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

package com.google.appengine.tck.search;

import java.util.Date;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Document.Builder;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Field.FieldType;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.users.User;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:hchen@google.com">Hannah Chen</a>
 */
@RunWith(Arquillian.class)
public class FieldTest extends SearchTestBase {

    @Test
    public void testDocFields() throws Exception {
        String indexName = "test-doc-fields";
        Index index = searchService.getIndex(IndexSpec.newBuilder().setName(indexName));
        delDocs(index);

        Builder docBuilder = Document.newBuilder();
        Field field = Field.newBuilder().setName("textfield").setText("text field").build();
        docBuilder.addField(field);
        field = Field.newBuilder().setName("numberfield").setNumber(123).build();
        docBuilder.addField(field);
        Date now = new Date();
        field = Field.newBuilder().setName("datefield").setDate(now).build();
        docBuilder.addField(field);
        field = Field.newBuilder().setName("htmlfield").setHTML("<html>html field</html>").build();
        docBuilder.addField(field);
        User currentUser = new User("prometheus-qa@appenginetest.com", "appenginetest.com");
        field = Field.newBuilder().setName("atomfield").setAtom(currentUser.getAuthDomain()).build();
        docBuilder.addField(field);
        GeoPoint geoPoint = new GeoPoint((double) -10, 10.000001);
        field = Field.newBuilder().setName("geofield").setGeoPoint(geoPoint).build();
        docBuilder.addField(field);
        index.put(docBuilder);
        sync();

        Results<ScoredDocument> result = searchDocs(index, "", 0);
        assertEquals(1, result.getNumberReturned());
        ScoredDocument doc = result.iterator().next();
        Field retField = doc.getOnlyField("textfield");
        assertEquals(FieldType.TEXT, retField.getType());
        assertEquals("textfield", retField.getName());
        assertEquals("text field", retField.getText());

        retField = doc.getOnlyField("numberfield");
        assertEquals(FieldType.NUMBER, retField.getType());
        assertEquals(new Double("123"), retField.getNumber());

        retField = doc.getOnlyField("datefield");
        assertEquals(FieldType.DATE, retField.getType());
        assertEquals(now, retField.getDate());

        retField = doc.getOnlyField("htmlfield");
        assertEquals(FieldType.HTML, retField.getType());
        assertEquals("<html>html field</html>", retField.getHTML());

        retField = doc.getOnlyField("atomfield");
        assertEquals(FieldType.ATOM, retField.getType());
        assertEquals(currentUser.getAuthDomain(), retField.getAtom());

        retField = doc.getOnlyField("geofield");
        assertEquals(FieldType.GEO_POINT, retField.getType());
        assertEquals(-10, retField.getGeoPoint().getLatitude(), 0);
        assertEquals(10.000001, retField.getGeoPoint().getLongitude(), 0.000000);
    }

    @Test
    public void testValidNull() {
        Field field = Field.newBuilder().setName("textfield").setText(null).build();
        assertEquals(null, field.getText());
        field = Field.newBuilder().setName("numberfield").setHTML(null).build();
        assertEquals(null, field.getNumber());
        field = Field.newBuilder().setName("htmlfield").setHTML(null).build();
        assertEquals(null, field.getHTML());
        field = Field.newBuilder().setName("atomfield").setAtom(null).build();
        assertEquals(null, field.getAtom());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDateNull() {
        Field.newBuilder().setName("datefield").setDate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGeoNull() {
        Field.newBuilder().setName("geofield").setGeoPoint(null);
    }
}

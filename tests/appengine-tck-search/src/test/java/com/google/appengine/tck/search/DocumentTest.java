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
import java.util.Iterator;
import java.util.Locale;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.Document.Builder;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Field.FieldType;
import org.jboss.arquillian.junit.Arquillian;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:hchen@google.com">Hannah Chen</a>
 */
@RunWith(Arquillian.class)
public class DocumentTest extends SearchTestBase {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreateDocument() throws Exception {
        String indexName = "test-doc";
        Index index = searchService.getIndex(IndexSpec.newBuilder().setName(indexName));
        delDocs(index);
        Builder docBuilder = Document.newBuilder().setId("tck").setLocale(Locale.FRENCH).setRank(8);
        docBuilder.addField(Field.newBuilder().setName("field1").setText("text field"));
        docBuilder.addField(Field.newBuilder().setName("field1").setNumber(987));
        docBuilder.addField(Field.newBuilder().setName("field2").setNumber(123));
        docBuilder.addField(Field.newBuilder().setName("field3").setDate(new Date()));
        index.put(docBuilder.build());
        sync();

        Results<ScoredDocument> result = searchDocs(index, "", 0);
        assertEquals(1, result.getNumberReturned());
        ScoredDocument retDoc = result.iterator().next();
        assertEquals("tck", retDoc.getId());
        assertEquals(Locale.FRENCH, retDoc.getLocale());
        assertEquals(8, retDoc.getRank());
        assertEquals(2, retDoc.getFieldCount("field1"));
        assertEquals(1, retDoc.getFieldCount("field3"));
        assertEquals(3, retDoc.getFieldNames().size());

        Iterator<Field> fields = retDoc.getFields().iterator();
        int count = 0;
        for ( ; fields.hasNext() ; ++count ) {
            fields.next();
        }
        assertEquals(4, count);

        fields = retDoc.getFields("field1").iterator();
        count = 0;
        for ( ; fields.hasNext() ; ++count ) {
            fields.next();
        }
        assertEquals(2, count);

        Field field = retDoc.getOnlyField("field2");
        assertEquals(FieldType.NUMBER, field.getType());
    }

    @Test
    public void testMultipleNumField() {
        Builder docBuilder = Document.newBuilder();
        docBuilder.addField(Field.newBuilder().setName("numfield").setNumber(123));
        thrown.expect(IllegalArgumentException.class);
        docBuilder.addField(Field.newBuilder().setName("numfield").setNumber(789));
    }

    @Test
    public void testMultipleDateField() {
        Builder docBuilder = Document.newBuilder();
        docBuilder.addField(Field.newBuilder().setName("datefield").setDate(new Date()));
        thrown.expect(IllegalArgumentException.class);
        docBuilder.addField(Field.newBuilder().setName("datefield").setDate(new Date()));
    }
}

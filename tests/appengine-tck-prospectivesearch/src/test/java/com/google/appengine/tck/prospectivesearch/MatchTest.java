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

package com.google.appengine.tck.prospectivesearch;

import java.util.Arrays;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.prospectivesearch.FieldType;
import com.google.appengine.tck.prospectivesearch.support.MatchResponseServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_BATCH_SIZE;
import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_RELATIVE_URL;
import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_TASK_QUEUE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:hchen@google.com">Hannah Chen</a>
 */
@RunWith(Arquillian.class)
public class MatchTest extends MatchTestBase {

    @Test
    public void testMatchInvokesServletWhenSearchMatches() throws Exception {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));

        Entity entity = articleWithTitle("Hello World");
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testMatchDoesNotInvokeServletWhenSearchDoesNotMatch() throws Exception {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:foo", createSchema("title", FieldType.STRING));

        Entity entity = articleWithTitle("Bar");
        service.match(entity, TOPIC);

        assertServletWasNotInvoked();
    }

    @Test
    public void testMatchUsesGoogleQuerySyntax() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:\"Hello World\" body:article", createSchema("title", FieldType.STRING, "body", FieldType.STRING));

        Entity entity = articleWithTitleAndBody("Hello World", "This is the body of the article");
        service.match(entity, TOPIC);
        assertServletWasInvokedWith(entity);

        MatchResponseServlet.clear();

        entity = articleWithTitleAndBody("Hello World", "This body does not contain the word matched by foo subscription");
        service.match(entity, TOPIC);
        assertServletWasNotInvoked();
    }

    @Test
    public void testMatchOnlyMatchesDocumentsInSameTopic() throws Exception {
        service.subscribe("topic1", "foo", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe("topic2", "bar", 0, "title:hello", createSchema("title", FieldType.STRING));

        Entity entity = articleWithTitle("Hello World");
        service.match(entity, "topic1");

        assertServletReceivedSubIds("foo");
    }

    @Test
    public void testMatchHonorsResultRelativeUri() throws Exception {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));

        Entity entity = articleWithTitle("Hello World");
        service.match(entity, TOPIC, "", SPECIAL_RESULT_RELATIVE_URI, DEFAULT_RESULT_TASK_QUEUE_NAME, DEFAULT_RESULT_BATCH_SIZE, true);

        assertSpecialServletWasInvokedWith(entity);
    }

    @Test
    public void testServletReceivesCorrectSubscriptionIds() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "foo2", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "bar", 0, "title:bar", createSchema("title", FieldType.STRING));
        service.match(articleWithTitle("Foo foo"), TOPIC);

        assertServletReceivedSubIds("foo1", "foo2");
    }

    @Test
    public void testServletReceivesResultKeyParameter() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));

        String expectedKey = "myResultKey";
        service.match(articleWithTitle("Foo foo"), TOPIC, expectedKey);

        waitForSync();
        assertServletWasInvoked();

        String receivedKey = MatchResponseServlet.getLastInvocationData().getKey();
        assertEquals("servlet was invoked with wrong key", expectedKey, receivedKey);
    }

    @Test
    public void testServletReceivesTopicParameter() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.match(articleWithTitle("Foo foo"), TOPIC);

        waitForSync();
        assertServletWasInvoked();

        String receivedTopic = MatchResponseServlet.getLastInvocationData().getTopic();
        assertEquals("servlet was invoked with wrong topic", TOPIC, receivedTopic);
    }

    @Test
    public void testServletReceivesDocumentOnlyWhenFlagIsTrue() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));

        boolean resultReturnDocument = false;
        service.match(articleWithTitle("Foo foo"), TOPIC, "", DEFAULT_RESULT_RELATIVE_URL, DEFAULT_RESULT_TASK_QUEUE_NAME, DEFAULT_RESULT_BATCH_SIZE, resultReturnDocument);

        waitForSync();
        assertServletWasInvoked();
        assertNull("servlet should not have received document", MatchResponseServlet.getLastInvocationData().getDocument());
    }

    @Test
    public void testMatchOnStringField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:happy", createSchema("title", FieldType.STRING));

        Entity entity = new Entity("article");
        entity.setProperty("title", "happy feet");
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testMatchOnStringListField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:sad", createSchema("title", FieldType.STRING));

        Entity entity = new Entity("article");
        entity.setProperty("title", Arrays.asList("happy feet", "sad head"));
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testMatchOnTextField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:happy", createSchema("title", FieldType.TEXT));

        Entity entity = new Entity("article");
        entity.setProperty("title", new Text("happy feet"));
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testMatchOnIntegerField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "length=500", createSchema("length", FieldType.INT32));

        Entity entity = new Entity("article");
        entity.setProperty("length", 500L);
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testMatchOnBooleanField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "correct:true", createSchema("correct", FieldType.BOOLEAN));

        Entity entity = new Entity("article");
        entity.setProperty("correct", true);
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testMatchOnDoubleField() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "distance=10.12", createSchema("distance", FieldType.DOUBLE));

        Entity entity = new Entity("article");
        entity.setProperty("distance", 10.12);
        service.match(entity, TOPIC);

        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testSearchOperatorOrWithoutMatch() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "manager OR developer", createSchema("title", FieldType.STRING, "company", FieldType.STRING));

        Entity entity = createCompanyEntity("tech");
        service.match(entity, TOPIC);

        assertServletWasNotInvoked();
    }

    @Test
    public void testSearchOperatorORWithMatch() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "tester OR developer", createSchema("title", FieldType.STRING));

        Entity entity = createCompanyEntity(null);
        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testSearchOperatorORWithMatchPrefixed() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:tester OR title:developer", createSchema("title", FieldType.STRING));

        Entity entity = createCompanyEntity(null);
        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testSearchOperatorANDWithoutMatch() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:tester AND company:Google", createSchema("title", FieldType.STRING, "company", FieldType.STRING));

        @SuppressWarnings("UnusedDeclaration")
        Entity entity = createCompanyEntity("tech");
        assertServletWasNotInvoked();
    }

    @Test
    public void testSearchOperatorANDWithMatch() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:tester AND company:Google", createSchema("title", FieldType.STRING, "company", FieldType.STRING));

        Entity entity = createCompanyEntity("google");
        assertServletWasInvokedWith(entity);
    }

    @Test
    public void testSearchOperatorNOTWithoutMatch() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:tester NOT company:Google", createSchema("title", FieldType.STRING, "company", FieldType.STRING));

        @SuppressWarnings("UnusedDeclaration")
        Entity entity = createCompanyEntity("google");
        assertServletWasNotInvoked();
    }

    @Test
    public void testSearchOperatorNOTWithMatch() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:tester NOT company:Google", createSchema("title", FieldType.STRING, "company", FieldType.STRING));

        Entity entity = createCompanyEntity("RedHat");
        assertServletWasInvokedWith(entity);
    }

    private Entity createCompanyEntity(String fieldVal) {
        Entity entity = new Entity("article");
        entity.setProperty("title", "tester");
        if (fieldVal != null) {
            entity.setProperty("company", fieldVal);
        }
        service.match(entity, TOPIC);
        return entity;
    }

    @Test
    public void testNumericOperatorWithoutMatch() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:tester distance < 16.20", createSchema("title", FieldType.STRING, "distance", FieldType.DOUBLE));

        @SuppressWarnings("UnusedDeclaration")
        Entity entity = createDistanceEntity(22.22);
        assertServletWasNotInvoked();
    }

    @Test
    public void testNumericOperatorWithMatch() throws Exception {
        service.subscribe(TOPIC, "foo", 0, "title:tester distance > 16.20", createSchema("title", FieldType.STRING, "distance", FieldType.DOUBLE));

        Entity entity = createDistanceEntity(22.22);
        assertServletWasInvokedWith(entity);
    }

    private Entity createDistanceEntity(double fieldVal) {
        Entity entity = new Entity("article");
        entity.setProperty("title", "tester");
        entity.setProperty("distance", fieldVal);
        service.match(entity, TOPIC);
        return entity;
    }
}

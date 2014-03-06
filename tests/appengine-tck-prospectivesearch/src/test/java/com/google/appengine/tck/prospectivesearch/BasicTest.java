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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.appengine.api.prospectivesearch.FieldType;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchService;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchServiceFactory;
import com.google.appengine.api.prospectivesearch.QuerySyntaxException;
import com.google.appengine.api.prospectivesearch.Subscription;
import com.google.appengine.tck.env.Environment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class BasicTest extends ProspectiveTestBase {
    @Deployment
    public static WebArchive getDeployemnt() {
        return getBaseDeployment();
    }

    @Test
    public void testTopicIsCreatedWhenFirstSubscriptionForTopicIsCreated() {
        ProspectiveSearchService pss = ProspectiveSearchServiceFactory.getProspectiveSearchService();
        pss.subscribe(TOPIC, "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        assertTopicExists(TOPIC);
    }

    @Test
    public void testTopicIsRemovedWhenLastSubscriptionForTopicIsDeleted() {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "mySubscription2", 0, "body:foo", createSchema("body", FieldType.STRING));

        service.unsubscribe(TOPIC, "mySubscription1");
        assertTopicExists(TOPIC);
        service.unsubscribe(TOPIC, "mySubscription2");
        assertTopicNotExists(TOPIC);
    }

    @Test(expected = QuerySyntaxException.class)
    public void testSubscribeThrowsQuerySyntaxExceptionWhenSchemaIsEmpty() {
        service.subscribe("foo", "bar", 0, "title:hello", new HashMap<String, FieldType>());
        fail("Expected QuerySyntaxException: Schema is empty");
    }

    @Test
    public void testSubscriptionIsAutomaticallyRemovedAfterLeaseDurationSeconds() throws Exception {
        service.subscribe("foo", "bar", 5, "title:hello", createSchema("title", FieldType.STRING));
        assertSubscriptionExists("foo", "bar");
        sleepSeconds(10);
        assertSubscriptionNotExists("foo", "bar");
    }

    @Test
    public void testUnsubscribeRemovesSubscription() {
        service.subscribe(TOPIC, "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.unsubscribe(TOPIC, "mySubscription");
        assertSubscriptionNotExists(TOPIC, "mySubscription");
    }

    @Test
    public void testSubscribeOverwritesPreviousSubscriptionWithSameId() {
        service.subscribe(TOPIC, "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "mySubscription", 0, "body:foo", createSchema("body", FieldType.STRING));

        assertEquals(1, service.listSubscriptions(TOPIC).size());

        Subscription subscription = service.getSubscription(TOPIC, "mySubscription");
        assertEquals("body:foo", subscription.getQuery());
    }

    @Test(expected = Exception.class)
    public void testUnsubscribeThrowsIllegalArgumentExceptionWhenTopicNotExists() {
        service.unsubscribe(TOPIC, "mySubscription1");
    }

    @Test(expected = Exception.class)
    public void testUnsubscribeThrowsIllegalArgumentExceptionWhenSubIdNotExists() {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.unsubscribe(TOPIC, "nonExistentSubscription");
    }

    @Test
    public void testGetSubscription() {
        service.subscribe(TOPIC, "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        Subscription subscription = service.getSubscription(TOPIC, "mySubscription");

        assertEquals("mySubscription", subscription.getId());
        assertEquals("title:hello", subscription.getQuery());
    }

    @Test
    public void testSubscriptionWithoutLeaseTimeSecondsPracticallyNeverExpires() {
        assumeEnvironment(Environment.APPSPOT, Environment.CAPEDWARF);

        service.subscribe(TOPIC, "mySubscription", 0, "title:hello", createSchema("title", FieldType.STRING));
        Subscription subscription = service.getSubscription(TOPIC, "mySubscription");
        long expirationTime = subscription.getExpirationTime();

        long expected = todayPlusHundredYears().getTime() / 1000;
        assertTrue("subscription should not expire at least 100 years", expirationTime > expected);
    }

    @Test
    public void testSubscriptionWithLeaseTimeSecondsHasCorrectExpirationTime() {
        service.subscribe(TOPIC, "mySubscription", 500, "title:hello", createSchema("title", FieldType.STRING));
        Subscription subscription = service.getSubscription(TOPIC, "mySubscription");
        assertEquals(System.currentTimeMillis() / 1000 + 500, subscription.getExpirationTime(), 10.0);
    }

    private Date todayPlusHundredYears() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 100);
        return cal.getTime();
    }

    @Test(expected = Exception.class)
    public void testGetSubscriptionThrowsIllegalArgumentExceptionWhenNotExists() {
        service.getSubscription(TOPIC, "nonExistentSubscription");
    }

    @Test
    public void testListSubscriptions() {
        service.subscribe(TOPIC, "mySubscription1", 0, "title:hello", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "mySubscription2", 0, "body:foo", createSchema("body", FieldType.STRING));

        List<Subscription> subList = service.listSubscriptions(TOPIC, "", 1, 0);
        assertEquals(1, subList.size());

        List<Subscription> subscriptions = service.listSubscriptions(TOPIC);
        assertEquals(2, subscriptions.size());

        sortBySubId(subscriptions);

        Subscription subscription1 = subscriptions.get(0);
        assertEquals("mySubscription1", subscription1.getId());
        assertEquals("title:hello", subscription1.getQuery());

        Subscription subscription2 = subscriptions.get(1);
        assertEquals("mySubscription2", subscription2.getId());
        assertEquals("body:foo", subscription2.getQuery());
    }

    @Test
    public void testListTopicsReturnsInLexicographicalOrder() {
        service.subscribe("ccc", "subId", 0, "foo", createSchema("all", FieldType.STRING)); // TODO: what should the schema be like?
        service.subscribe("aaa", "subId", 0, "foo", createSchema("all", FieldType.STRING));
        service.subscribe("bbb", "subId", 0, "foo", createSchema("all", FieldType.STRING));

        List<String> topics = service.listTopics("", 1000);
        assertEquals(Arrays.asList("aaa", "bbb", "ccc"), topics);
    }

    @Test
    public void testSubscriptionInit() {
        Subscription subscription = new Subscription("subId", "", 0);
        sync(1000);
        assertEquals(Subscription.State.OK, subscription.getState());
    }

    @Test
    public void testSubscriptionInit2() {
        Subscription subscription = new Subscription("subId", "", 0, Subscription.State.PENDING, "error message");
        sync(1000);
        assertEquals(Subscription.State.PENDING, subscription.getState());
    }

    private void assertTopicExists(String topic) {
        assertTrue("topic '" + topic + "' does not exist", getAllTopics().contains(topic));
    }

    private void assertTopicNotExists(String topic) {
        assertFalse("topic '" + topic + "' exists, but it shouldn't", getAllTopics().contains(topic));
    }

    private void assertSubscriptionExists(String topic, String subId) {
        try {
            service.getSubscription(topic, subId);
        } catch (IllegalArgumentException e) {
            fail("subscription with topic " + topic + " and subId " + subId + " does not exists, but it should");
        }
    }

    private void assertSubscriptionNotExists(String topic, String subId) {
        try {
            service.getSubscription(topic, subId);
            fail("subscription with topic " + topic + " and subId " + subId + " exists, but it shouldn't");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    private List<String> getAllTopics() {
        return service.listTopics("", 1000);
    }

    private void sleepSeconds(int seconds) throws Exception {
        sync(1000L * seconds);
    }

}

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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.prospectivesearch.FieldType;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchService;
import com.google.appengine.api.prospectivesearch.ProspectiveSearchServiceFactory;
import com.google.appengine.api.prospectivesearch.Subscription;
import com.google.appengine.tck.prospectivesearch.support.MatchResponseServlet;
import com.google.appengine.tck.prospectivesearch.support.SpecialMatchResponseServlet;
import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.base.TestContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class ProspectiveTestBase extends TestBase {

    protected ProspectiveSearchService service;
    protected static final String TOPIC = "myTopic";

    @Before
    public void setUp() {
        service = ProspectiveSearchServiceFactory.getProspectiveSearchService();
    }

    @Deployment
    public static WebArchive getDeployment() {
        TestContext context = new TestContext().setUseSystemProperties(true).setCompatibilityProperties(TCK_PROPERTIES);
        context.setWebXmlFile("match-web.xml");

        WebArchive war = getTckDeployment(context);
        war.addClasses(ProspectiveTestBase.class, MatchTestBase.class);
        war.addClasses(MatchResponseServlet.class, SpecialMatchResponseServlet.class);
        return war;
    }

    @After
    public void tearDown() throws Exception {
        removeAllSubscriptions();
    }

    protected void removeAllSubscriptions() {
        List<String> topics = service.listTopics("", 1000);
        for (String topic : topics) {
            List<Subscription> subscriptions = service.listSubscriptions(topic);
            for (Subscription subscription : subscriptions) {
                service.unsubscribe(topic, subscription.getId());
            }
        }
    }

    protected void sortBySubId(List<Subscription> subscriptions) {
        Collections.sort(subscriptions, new Comparator<Subscription>() {
            public int compare(Subscription o1, Subscription o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    protected Map<String, FieldType> createSchema(String field, FieldType type) {
        Map<String, FieldType> schema = new HashMap<String, FieldType>();
        schema.put(field, type);
        return schema;
    }

    protected Map<String, FieldType> createSchema(String field1, FieldType type1, String field2, FieldType type2) {
        Map<String, FieldType> schema = new HashMap<String, FieldType>();
        schema.put(field1, type1);
        schema.put(field2, type2);
        return schema;
    }

}

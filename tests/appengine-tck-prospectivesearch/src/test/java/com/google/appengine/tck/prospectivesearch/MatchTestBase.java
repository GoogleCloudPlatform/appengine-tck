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
import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tck.prospectivesearch.support.MatchResponseServlet;
import com.google.appengine.tck.prospectivesearch.support.SpecialMatchResponseServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class MatchTestBase extends ProspectiveTestBase {
    protected static final String SPECIAL_RESULT_RELATIVE_URI = "/_ah/prospective_search_special";

    @Deployment
    public static WebArchive getDeployemnt() {
        return getBaseDeployment().addClass(MatchTestBase.class);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        MatchResponseServlet.clear();
        SpecialMatchResponseServlet.clear();
    }

    protected void assertServletWasInvoked() {
        if (!MatchResponseServlet.isInvoked()) {
            fail("servlet was not invoked");
        }
    }

    protected Entity articleWithTitle(String title) {
        Entity entity = new Entity("article");
        entity.setProperty("title", title);
        return entity;
    }

    protected Entity articleWithTitleAndBody(String title, String body) {
        Entity entity = new Entity("article");
        entity.setProperty("title", title);
        entity.setProperty("body", body);
        return entity;
    }

    protected void assertServletWasInvokedWith(Entity entity) throws Exception {
        waitForSync();

        assertServletWasInvoked();

        Entity lastReceivedDocument = MatchResponseServlet.getLastInvocationData().getDocument();
        if (lastReceivedDocument == null) {
            fail("servlet was invoked without a document (document was null)");
        }

        assertTrue("servlet was invoked with some other entity: " +
                   lastReceivedDocument, lastReceivedDocument.getProperties().equals(entity.getProperties()));
    }

    protected void assertServletReceivedSubIds(String... subIds) throws Exception {
        waitForSync();

        assertServletWasInvoked();

        Set<String> expectedSubIds = new HashSet<>(Arrays.asList(subIds));
        Set<String> receivedSubIds = new HashSet<>(MatchResponseServlet.getAllSubIds());
        assertEquals("servlet was invoked with wrong subIds", expectedSubIds, receivedSubIds);
    }

    @SuppressWarnings("UnusedParameters")
    protected void assertSpecialServletWasInvokedWith(Entity entity) throws Exception {
        waitForSync();

        if (SpecialMatchResponseServlet.isInvoked() == false) {
            fail("servlet was not invoked");
        }

        Entity lastReceivedDocument = SpecialMatchResponseServlet.getLastReceivedDocument();
        if (lastReceivedDocument == null) {
            fail("servlet was invoked without a document (document was null)");
        }
    }

    protected void assertServletWasNotInvoked() throws Exception {
        waitForSync();

        if (MatchResponseServlet.isInvoked()) {
            Entity lastReceivedDocument = MatchResponseServlet.getLastInvocationData().getDocument();
            fail("servlet was invoked with: " + lastReceivedDocument);
        }
    }

    protected void waitForSync() throws InterruptedException {
        sync(3000);
    }

}

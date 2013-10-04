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

import com.google.appengine.api.prospectivesearch.FieldType;
import com.google.appengine.tck.prospectivesearch.support.MatchResponseServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_RELATIVE_URL;
import static com.google.appengine.api.prospectivesearch.ProspectiveSearchService.DEFAULT_RESULT_TASK_QUEUE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class SlowMatchTest extends MatchTestBase {

    @Test
    public void testMatchHonorsResultBatchSize() throws Exception {
        service.subscribe(TOPIC, "foo1", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "foo2", 0, "title:foo", createSchema("title", FieldType.STRING));
        service.subscribe(TOPIC, "foo3", 0, "title:foo", createSchema("title", FieldType.STRING));

        int resultBatchSize = 2;
        service.match(articleWithTitle("Foo foo"), TOPIC, "", DEFAULT_RESULT_RELATIVE_URL, DEFAULT_RESULT_TASK_QUEUE_NAME, resultBatchSize, true);

        assertServletReceivedSubIds("foo1", "foo2", "foo3");

        int expectedInvocationCount = 2; // Math.ceil(3 / 2) = 2
        assertEquals("incorrect servlet invocation count", expectedInvocationCount, MatchResponseServlet.getInvocationCount());

        for (MatchResponseServlet.InvocationData invocationData : MatchResponseServlet.getInvocations()) {
            assertTrue("batch was too large", invocationData.getSubIds().length <= resultBatchSize);
        }
    }

}

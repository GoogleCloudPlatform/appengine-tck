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

package com.google.appengine.tck.taskqueue;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.QueueStatistics;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class StatsTest extends QueueTestBase {

    /**
     * Because the stats processing is approximate, this test only verifies the basics.
     * The dev_appserver generates random statistics.
     */
    @Test
    public void testStatsApiBasic() throws Exception {
        final Queue queue = QueueFactory.getQueue("pull-queue");
        QueueStatistics stats = queue.fetchStatistics();
        Assert.assertNotNull(stats);
        Assert.assertEquals("pull-queue", stats.getQueueName());
        Assert.assertTrue(stats.getEnforcedRate() >= 0);

        Assert.assertTrue(stats.getNumTasks() >= 0);
        Assert.assertTrue(stats.getExecutedLastMinute() >= 0);
        Assert.assertTrue(stats.getOldestEtaUsec() == null || stats.getOldestEtaUsec() >= 0);
        Assert.assertTrue(stats.getRequestsInFlight() >= 0);
    }
}

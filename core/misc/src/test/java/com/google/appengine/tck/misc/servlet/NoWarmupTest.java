/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.appengine.tck.misc.servlet;

import com.google.appengine.tck.misc.servlet.support.WarmupData;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class NoWarmupTest extends AbstractWarmupTest {

    @Deployment
    public static WebArchive getDeployment() {
        return getBaseDeployment(false);
    }

    @Test
    public void testWarmup() throws Exception {
        WarmupData warmupData = pollForTempData(WarmupData.class, 10);
        Assert.assertNull(warmupData);
    }
}

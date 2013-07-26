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

package com.google.appengine.tck.logservice.configuration;

import java.util.logging.Level;

import com.google.appengine.tck.category.IgnoreMultisuite;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(IgnoreMultisuite.class)
public class LogLevelFinerTest extends LoggingConfigurationTestBase {

    @Deployment
    public static Archive getDeployment() {
        return getDeploymentWithLoggingLevelSetTo(Level.FINER);
    }

    @Test
    public void testLogLevelFiner() {
        assertLogOnlyLogsMessagesAboveOrAtLevel(Level.FINER);
    }
}

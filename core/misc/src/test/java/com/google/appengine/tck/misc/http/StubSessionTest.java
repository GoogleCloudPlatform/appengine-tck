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

package com.google.appengine.tck.misc.http;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class StubSessionTest extends AbstractHttpSessionTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        return getBaseDeployment(false);
    }

    @Test
    public void testCheckAttribute() throws Exception {
        Integer x = (Integer) getSession().getAttribute("xyz");
        if (x == null) {
            x = -1;
        }
        try {
            getSession().setAttribute("xyz", ++x);
            Assert.fail("Non-writtable attribute!");
        } catch (Exception ignored) {
        }
        try {
            getSession().removeAttribute("xyz");
            Assert.fail("Non-writtable attribute!");
        } catch (Exception ignored) {
        }
    }

}

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

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class HttpSessionTest extends AbstractHttpSessionTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        return getBaseDeployment(true);
    }

    @Test
    public void testCheckAttribute() throws Exception {
        Integer x = (Integer) getSession().getAttribute("xyz");
        if (x == null) {
            x = -1;
        }
        getSession().setAttribute("xyz", ++x);
    }

    @Test
    @InSequence(1)
    @RunAsClient
    public void testSet(@ArquillianResource URL url) throws Exception {
        getClient().post(url + "/session?action=set&string=ales");
    }

    @Test
    @InSequence(2)
    @RunAsClient
    public void testGet(@ArquillianResource URL url) throws Exception {
        Assert.assertEquals("alesj", getClient().post(url + "/session?action=get&suffix=j"));
    }
}

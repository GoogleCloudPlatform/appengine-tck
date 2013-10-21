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

package com.google.appengine.tck.appidentity;

import com.google.apphosting.api.ApiProxy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
@RunWith(Arquillian.class)
public class AppIdentityBasicTest extends AppIdentityTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        return getDefaultDeployment();
    }

    @Test
    public void testGetAppId() {
        String expectedAppId = getExpectedAppId("testGetAppId");
        String appId = ApiProxy.getCurrentEnvironment().getAppId();
        // AppIds in the US are prefixed with s~ and in Europe e~ so just match the end.
        String errMsg = "The appId should end with " + expectedAppId + ", but was " + appId;
        Assert.assertTrue(errMsg, appId.endsWith(expectedAppId));
    }

    @Test
    public void testGetVersionedHostname() {
        String expectedHostname = getExpectedAppHostname("testGetVersionedHostname");
        ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
        String hostname = (String) env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
        String errMsg = "The versioned hostname should end with " + expectedHostname + ", but was " + hostname;
        Assert.assertTrue(errMsg, hostname.endsWith(expectedHostname));
    }
}

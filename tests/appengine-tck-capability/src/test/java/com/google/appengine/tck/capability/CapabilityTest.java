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

package com.google.appengine.tck.capability;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityState;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tck.base.TestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Tests for capability service.
 *
 * @author hchen@google.com (Hannah Chen)
 */
@RunWith(Arquillian.class)
public class CapabilityTest extends TestBase {
    private CapabilitiesService capabilitiesService;
    private String[] TEST_DATA = {"blobstore", "datastore_v3", "datastore_v3,write", "images", "mail", "memcache", "taskqueue", "urlfetch", "xmpp"};

    @Before
    public void setUp() {
        capabilitiesService = CapabilitiesServiceFactory.getCapabilitiesService();
    }

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = getTckDeployment();
        war.addAsWebInfResource(new StringAsset("capability"), "dummy.txt");
        return war;
    }

    @Test
    public void testGetStatus() {
        Capability capability;
        for (String p : TEST_DATA) {
            if (p.indexOf(',') > 0) {
                String in[] = p.split(",");
                capability = new Capability(in[0], in[1]);
                p = in[0];
            } else {
                capability = new Capability(p);
            }
            CapabilityState cState = capabilitiesService.getStatus(capability);
            assertEquals(p, cState.getCapability().getPackageName());
            assertEquals(CapabilityStatus.ENABLED, cState.getStatus());
        }
    }

    /**
     * check unknown service.
     */
    @Test
    public void testDummyService() {
        // only check this in appserver since everything in dev_appserver has ENABLED status.
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            String pName = "dummy";
            Capability capability = new Capability(pName);
            CapabilityState cState = capabilitiesService.getStatus(capability);
            assertEquals(pName, cState.getCapability().getPackageName());
            assertEquals(CapabilityStatus.UNKNOWN, cState.getStatus());
        }
    }
}

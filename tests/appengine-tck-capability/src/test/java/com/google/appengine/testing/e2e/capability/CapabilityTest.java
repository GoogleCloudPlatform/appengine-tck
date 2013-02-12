// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.appengine.testing.e2e.capability;

import static org.junit.Assert.assertEquals;


import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityState;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.utils.SystemProperty;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.test.capedwarf.common.support.All;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.experimental.categories.Category;

/**
 * Tests for capability service.
 *
 * @author hchen@google.com (Hannah Chen)
 */

@RunWith(Arquillian.class)
@Category(All.class)
public class CapabilityTest {
  private CapabilitiesService capabilitiesService =
                                           CapabilitiesServiceFactory.getCapabilitiesService();
  private String[] TEST_DATA = {"blobstore", "datastore_v3", "datastore_v3,write", "images",
                                "mail", "memcache", "taskqueue", "urlfetch", "xmpp"};

  @Test
  public void testGetStatus() {
    /*
    try {
      Thread.sleep(60000);
    } catch (InterruptedException ie) {
      //
    }
    */
    Capability capability = null;
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

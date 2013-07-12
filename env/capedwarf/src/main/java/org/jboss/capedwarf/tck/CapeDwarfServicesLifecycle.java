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

package org.jboss.capedwarf.tck;

import java.lang.reflect.Method;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.log.LogService;
import com.google.appengine.tck.event.AbstractServiceLifecycle;
import com.google.appengine.tck.event.ServiceLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycle;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(TestLifecycle.class)
public class CapeDwarfServicesLifecycle extends AbstractServiceLifecycle {
    protected void doBefore(ServiceLifecycleEvent event) {
    }

    protected void doAfter(ServiceLifecycleEvent event) {
        Object service = event.getService();
        if (service instanceof DatastoreService) {
            invoke(service, "clearCache");
        } else if (service instanceof LogService) {
            invoke(service, "clearLog");
        }
    }

    protected void invoke(Object service, String method) {
        final Class<?> clazz = service.getClass();
        try {
            Method m = clazz.getMethod(method);
            m.setAccessible(true);
            m.invoke(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

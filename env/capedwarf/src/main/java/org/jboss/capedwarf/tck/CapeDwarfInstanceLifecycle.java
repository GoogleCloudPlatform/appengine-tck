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

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.google.appengine.tck.event.AbstractInstanceLifecycle;
import com.google.appengine.tck.event.InstanceLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycle;
import com.google.appengine.tck.mail.EmailAddressFormatter;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(TestLifecycle.class)
public class CapeDwarfInstanceLifecycle extends AbstractInstanceLifecycle {
    @SuppressWarnings("unchecked")
    protected void doBefore(InstanceLifecycleEvent event) {
        Class<?> instanceType = event.getInstanceType();
        if (Session.class.equals(instanceType)) {
            Session session = lookup(Session.class, "java:jboss/mail/Default");
            event.setInstance(session);
        } else if (EmailAddressFormatter.class.equals(instanceType)) {
            event.setInstance(CapedwarfEmailAddressFormatter.INSTANCE);
        }
    }

    protected void doAfter(InstanceLifecycleEvent event) {
    }

    private <T> T lookup(Class<T> instanceType, String jndiName) {
        try {
            Context context = new InitialContext();
            try {
                Object result = context.lookup(jndiName);
                return instanceType.cast(result);
            } finally {
                context.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

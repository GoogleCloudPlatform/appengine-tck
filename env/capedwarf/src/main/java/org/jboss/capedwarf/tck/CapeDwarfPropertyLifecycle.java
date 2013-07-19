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

import java.util.Collections;
import java.util.Set;

import com.google.appengine.tck.event.AbstractPropertyLifecycle;
import com.google.appengine.tck.event.PropertyLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycle;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(TestLifecycle.class)
public class CapeDwarfPropertyLifecycle extends AbstractPropertyLifecycle {
    private static final Set<String> REQUIRED_PROPERTIES = Collections.emptySet(); // empty atm

    protected void doBefore(PropertyLifecycleEvent event) {
        event.setRequired(REQUIRED_PROPERTIES.contains(event.getPropertyName()));
    }

    protected void doAfter(PropertyLifecycleEvent event) {
    }
}

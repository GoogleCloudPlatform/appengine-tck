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

package com.google.appengine.tck.ns;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.NamespaceManager;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.Test;

/**
 * @author Aslak Knutsen
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class NamespaceExtensionContainer implements RemoteLoadableExtension {
    public void register(ExtensionBuilder builder) {
        builder.observer(NamespaceTestObserver.class);
    }

    public static class NamespaceTestObserver {
        public void execute(@Observes EventContext<Test> context) {
            Test event = context.getEvent();

            Method testMethod = event.getTestMethod();
            WithinNamespace ns = testMethod.getAnnotation(WithinNamespace.class);
            if (ns == null) {
                ns = event.getTestClass().getAnnotation(WithinNamespace.class);
                if (ns == null) {
                    Class<?> testClass = event.getTestClass().getJavaClass();
                    ns = testClass.getPackage().getAnnotation(WithinNamespace.class);
                }
            }

            if (ns != null) {
                runWithinNamespaces(context, ns.value());
            } else {
                context.proceed();
            }
        }

        private void runWithinNamespaces(EventContext<Test> context, String[] namespaces) {
            final List<FailedNamespaceException> exceptions = new ArrayList<>();
            final String original = NamespaceManager.get();
            try {
                for (String namespace : namespaces) {
                    try {
                        NamespaceManager.set(namespace);
                        context.proceed();
                    } catch (Exception e) {
                        exceptions.add(new FailedNamespaceException(e, namespace));
                    }
                }
            } finally {
                NamespaceManager.set(original);
            }
            if (exceptions.size() > 1) {
                throw new MultipleExceptions(exceptions);
            } else if (exceptions.size() == 1) {
                throw exceptions.get(0);
            }
        }
    }
}

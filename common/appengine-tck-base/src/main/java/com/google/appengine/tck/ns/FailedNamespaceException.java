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

/**
 * @author Aslak Knutsen
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FailedNamespaceException extends RuntimeException {
    private final String namespace;

    public FailedNamespaceException(Throwable cause, String namespace) {
        super(cause);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getMessage() {
        return String.format("Ns[%s] - %s", namespace, getCause().getMessage());
    }
}

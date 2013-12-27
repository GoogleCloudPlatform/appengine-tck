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

import java.util.Collections;
import java.util.List;

/**
 * @author Aslak Knutsen
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MultipleExceptions extends RuntimeException {
    private final List<? extends Exception> exceptions;

    public MultipleExceptions(List<? extends Exception> exceptions) {
        if (exceptions == null) {
            throw new IllegalArgumentException("Null exceptions!");
        }
        this.exceptions = exceptions;
    }

    public List<Exception> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    @Override
    public String getMessage() {
        return exceptions.toString();
    }
}

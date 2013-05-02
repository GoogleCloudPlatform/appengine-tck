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

package com.google.appengine.tck.coverage;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class SoutPrinter implements Printer {
    static final Printer INSTANCE = new SoutPrinter();

    public void print(Map<String, Map<Tuple, Set<CodeLine>>> report) {
        StringBuilder builder = new StringBuilder("\n");
        for (String iface : report.keySet()) {
            builder.append("\nInterface / Class: ").append(iface).append("\n");
            Map<Tuple, Set<CodeLine>> map = report.get(iface);
            for (Map.Entry<Tuple, Set<CodeLine>> entry : map.entrySet()) {
                builder.append("\t").append(entry.getKey()).append("\n");
                Set<CodeLine> value = entry.getValue();
                if (value.isEmpty()) {
                    builder.append("\t\t").append("MISSING -- TODO?").append("\n");
                } else {
                    for (CodeLine cl : value) {
                        builder.append("\t\t").append(cl).append("\n");
                    }
                }
            }
        }
        System.out.println(builder);
    }
}

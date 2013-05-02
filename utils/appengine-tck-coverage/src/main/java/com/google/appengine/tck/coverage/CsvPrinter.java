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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:terryok@google.com">Terry Okamoto</a>
 */
public class CsvPrinter implements Printer {

    private File index;

    CsvPrinter(File index) {
        this.index = index;
    }

    @Override
    public void print(Map<String, Map<Tuple, Set<CodeLine>>> report) throws Exception {
        if (index.exists()) index.delete();

        BufferedWriter writer = new BufferedWriter(new FileWriter(index));
        try {
            writer.write("Interface/Class, Method, Call Count\n");
            StringBuilder builder = new StringBuilder();
            for (String iface : report.keySet()) {
                Map<Tuple, Set<CodeLine>> map = report.get(iface);
                for (Map.Entry<Tuple, Set<CodeLine>> entry : map.entrySet()) {
                    builder.append(iface);  // Interface/Class name
                    builder.append(", ").append(entry.getKey());  // Method
                    Set<CodeLine> value = entry.getValue();
                    builder.append(", ").append(value.size()).append("\n");  // Call Count
                }
            }
            writer.write(builder.toString());
        } finally {
            writer.close();
        }
    }
}

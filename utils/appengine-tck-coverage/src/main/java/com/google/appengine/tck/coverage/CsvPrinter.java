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

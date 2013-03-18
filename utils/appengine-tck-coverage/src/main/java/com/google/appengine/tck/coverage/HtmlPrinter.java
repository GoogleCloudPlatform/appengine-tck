package com.google.appengine.tck.coverage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class HtmlPrinter implements Printer {
    private File index;

    HtmlPrinter(File index) {
        this.index = index;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void print(Map<String, Map<Tuple, Set<CodeLine>>> report) throws Exception {
        if (index.exists()) index.delete();

        BufferedWriter writer = new BufferedWriter(new FileWriter(index));
        try {
            writer.write("<html><title>GAE API Code Coverage</title><body>");
            for (String iface : report.keySet()) {
                writer.append("<a href=\"").append(iface.replace('.', '/')).append(".class").append("\">").append("Interface / Class: ").append(iface).append("</a>");
                Map<Tuple, Set<CodeLine>> map = report.get(iface);
                writer.write("<ul>");
                for (Map.Entry<Tuple, Set<CodeLine>> entry : map.entrySet()) {
                    writer.append("<li>").append(esc(entry.getKey().toString()));
                    writer.append("<ul>");
                    Set<CodeLine> value = entry.getValue();
                    if (value.isEmpty()) {
                        writer.append("<li>").append("MISSING -- TODO?").append("</li>");
                    } else {
                        for (CodeLine cl : value) {
                            writer.append("<li>").append(toLink(cl)).append("</li>");
                        }
                    }
                    writer.append("</ul>");
                    writer.append("</li>");
                    writer.newLine();
                }
                writer.write("</ul>");
            }
            writer.write("</body></html>");
        } finally {
            writer.close();
        }
    }

    protected static String toLink(CodeLine cl) {
        return "<a href=\"" + cl.getClassName().replace('.', '/') + "#" + cl.getMethodName() + "\">" + esc(cl.getClassName() + " @ " + cl.getMethodName() + " # " + cl.getLine()) + "</a>";
    }

    static String esc(String token) {
        token = token.replace("<", "&lt;");
        token = token.replace(">", "&gt;");
        return token;
    }
}

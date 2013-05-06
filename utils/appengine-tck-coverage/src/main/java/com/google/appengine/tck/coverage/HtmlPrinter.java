package com.google.appengine.tck.coverage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
class HtmlPrinter implements Printer {
    public static final String GITHUB_USER = "GoogleCloudPlatform";
    public static final String GITHUB_PROJECT = "appengine-tck";
    public static final String GITHUB_BRANCH = "master";

    private File baseDir;
    private File index;

    HtmlPrinter(File baseDir, File index) {
        this.baseDir = baseDir;
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

    protected String toLink(CodeLine cl) {
        String url = createGitHubUrl(GITHUB_USER, GITHUB_PROJECT, GITHUB_BRANCH, getPath(cl), cl.getLine());
        String text = esc(cl.getClassName() + "@" + cl.getMethodName() + "#" + cl.getLine());
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

    private String getPath(CodeLine cl) {
        return "/tests/" + baseDir.getName() + "/src/test/java/" + cl.getClassName().replace('.', '/') + ".java";
    }

    private static String createGitHubUrl(String user, String project, String branch, String path, int lineNumber) {
        return "http://github.com/" + user + "/" + project + "/blob/" + branch + path + "#L" + lineNumber;
    }

    static String esc(String token) {
        token = token.replace("<", "&lt;");
        token = token.replace(">", "&gt;");
        return token;
    }
}

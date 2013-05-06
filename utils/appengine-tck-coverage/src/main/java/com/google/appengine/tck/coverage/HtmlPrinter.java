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
            writer.write("<html>\n" +
                "<head>" +
                "<title>GAE API Code Coverage</title>" +
                "    <style type=\"text/css\">\n" +
                "body {\n" +
                "    font-family: Arial, sans-serif;\n" +
                "    font-size: 12px;\n" +
                "}\n" +
                "\n" +
                "div.apiClass {\n" +
                "    background-color: #dee3e9;\n" +
                "    border: 1px solid #9eadc0;\n" +
                "    margin: 5px 5px 50px 5px;\n" +
                "    padding: 2px 5px;\n" +
                "}\n" +
                "\n" +
                "ul.blockList {\n" +
                "    margin:10px 0 10px 0;\n" +
                "    padding:0;\n" +
                "}\n" +
                "ul.blockList li.blockList {\n" +
                "    list-style:none;\n" +
                "    margin-bottom:5px;\n" +
                "    padding:5px 20px 5px 10px;\n" +
                "    border:1px solid #9eadc0;\n" +
                "    background-color:#f9f9f9;\n" +
                "}\n" +
                "ul.blockList li.blockList h3 {\n" +
                "    margin: 3px;\n" +
                "    color: black;\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "ul.blockList ul.blockList {\n" +
                "    padding:5px 5px 5px 8px;\n" +
                "    background-color:#ffffff;\n" +
                "    border:1px solid #9eadc0;\n" +
                "}\n" +
                "ul.blockList ul.blockList li.blockList{\n" +
                "    border: 0;\n" +
                "    padding:0 0 5px 8px;\n" +
                "    background-color:#ffffff;\n" +
                "    padding: 3px;\n" +
                "    margin: 0px;\n" +
                "}\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>");
//            writer.write("<h1>GAE API Code Coverage</h1>");
            writer.write("<h1>" + baseDir.getName() + "</h1>");
            for (String iface : report.keySet()) {
                writer.append("<div class=\"apiClass\">");
                writer.append("<h2>").append(iface).append("</h2>");
//                writer.append("<h2><a href=\"").append(classJavaDocUrl).append("\">").append(iface).append("</a></h2>");
                Map<Tuple, Set<CodeLine>> map = report.get(iface);
                writer.write("<ul class=\"blockList\">");
                for (Map.Entry<Tuple, Set<CodeLine>> entry : map.entrySet()) {
                    String methodSignature = SignatureConverter.convertMethodSignature(entry.getKey().getMethodName(), entry.getKey().getMethodDesc());
                    String methodJavaDocUrl = "http://developers.google.com/appengine/docs/java/javadoc/" + iface.replace('.', '/') + "#" + methodSignature;
                    writer.append("<li class=\"blockList\">").append("<a href=\"").append(methodJavaDocUrl).append("\">").append("<h3>").append(esc(methodSignature)).append("</h3></a>");
                    writer.append("<ul class=\"blockList\">");
                    Set<CodeLine> value = entry.getValue();
                    if (value.isEmpty()) {
                        writer.append("<li class=\"blockList\">").append("MISSING -- TODO?").append("</li>");
                    } else {
                        for (CodeLine cl : value) {
                            writer.append("<li class=\"blockList\">").append(toLink(cl)).append("</li>");
                        }
                    }
                    writer.append("</ul>");
                    writer.append("</li>");
                    writer.newLine();
                }
                writer.write("</ul>");
                writer.write("</div>");
            }
            writer.write("</body></html>");
        } finally {
            writer.close();
        }
    }

    protected String toLink(CodeLine cl) {
        String url = createGitHubUrl(GITHUB_USER, GITHUB_PROJECT, GITHUB_BRANCH, getPath(cl), cl.getLine());
        String text = esc(cl.getSimpleClassName() + ".java" + ":" + cl.getLine());
        return esc(cl.getClassName() + "." + cl.getMethodName()) + " (<a href=\"" + url + "\">" + esc(text) + "</a>)";
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

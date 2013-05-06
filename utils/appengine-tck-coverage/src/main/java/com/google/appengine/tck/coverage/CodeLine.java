package com.google.appengine.tck.coverage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings("unchecked")
public class CodeLine implements Comparable<CodeLine> {
    private String className;
    private String methodName;
    private int line;

    public CodeLine(String className, String methodName, int line) {
        this.className = className;
        this.methodName = methodName;
        this.line = line;
    }

    public int compareTo(CodeLine cl) {
        int diff = className.compareTo(cl.className);
        if (diff != 0)
            return diff;

        diff = methodName.compareTo(cl.methodName);
        if (diff != 0)
            return diff;

        diff = line - cl.line;
        if (diff != 0)
            return diff;

        return 0;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return className + " @ " + methodName + " # " + line;
    }

    public String getSimpleClassName() {
        int lastDotIndex = className.lastIndexOf('.');
        return lastDotIndex == -1 ? className : className.substring(lastDotIndex+1);
    }
}

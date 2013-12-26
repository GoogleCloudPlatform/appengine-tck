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

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings("unchecked")
public class CodeLine implements Comparable<CodeLine> {
    static String JSP = ".jsp";
    static String JSP_PREFIX = "org.apache" + JSP + ".";
    static String JSP_SUFFIX = "_jsp";

    private String type = "java";
    private String className;
    private String ext = ".java";
    private String methodName;
    private int line;

    public CodeLine(String className, String methodName, int line) {
        this.className = className;
        this.methodName = methodName;
        this.line = line;
    }

    public CodeLine modify() {
        if (className.startsWith(JSP_PREFIX) && className.endsWith(JSP_SUFFIX)) {
            className = className.substring(JSP_PREFIX.length(), className.length() - JSP_SUFFIX.length());
            type = "resources";
            ext = JSP;
            line = 0;
        }
        return this;
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

    public String getType() {
        return type;
    }

    public String getClassName() {
        return className;
    }

    public String getExt() {
        return ext;
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
        return lastDotIndex == -1 ? className : className.substring(lastDotIndex + 1);
    }
}

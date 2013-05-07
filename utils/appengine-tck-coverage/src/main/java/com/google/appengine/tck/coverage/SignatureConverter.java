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

import java.util.StringTokenizer;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SignatureConverter {

    public static String convertMethodSignature(String methodName, String descriptor) {
        return convertToMethodSignature(methodName, descriptor, false);
    }

    public static String convertFullMethodSignature(String methodName, String descriptor) {
        return convertToMethodSignature(methodName, descriptor, true);
    }

    protected static String convertToMethodSignature(String methodName, String descriptor, boolean full) {
        if (descriptor.charAt(0) != '(') {
            throw new IllegalArgumentException("Can't convert " + descriptor);
        }

        int p = descriptor.lastIndexOf(')');
        String params = descriptor.substring(1, p);

        StringTokenizer tokenizer = new StringTokenizer(params, ";");
        StringBuilder sb = new StringBuilder();

        if (full) {
            String retParam = descriptor.substring(p + 1);
            if (retParam.endsWith(";")) {
                retParam = retParam.substring(0, retParam.length() - 1);
            }
            String ret = convertParam(retParam);
            // only use simple name for return type
            int r = ret.lastIndexOf(".");
            if (r > 0) {
                ret = ret.substring(r + 1);
            }
            sb.append(ret).append("  ");
        }

        sb.append(methodName).append("(");
        while (tokenizer.hasMoreTokens()) {
            String param = tokenizer.nextToken();
            sb.append(convertParam(param));
            if (tokenizer.hasMoreTokens()) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private static String convertParam(String param) {
        int i = 0;
        StringBuilder appendix = new StringBuilder();
        for (; param.charAt(i) == '['; i++) {
            appendix.append("[]");
        }

        String subparam = param.substring(i);
        String result = convertNonArrayParam(subparam) + appendix;
        if (isPrimitive(subparam) && subparam.length() > 1) {
            return result + ", " + convertParam(subparam.substring(1));
        } else {
            return result;
        }
    }

    private static String convertNonArrayParam(String param) {
        switch (param.charAt(0)) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'Z':
                return "boolean";
            case 'V':
                return "void";
//            case 'L':
//                int lastDotIndex = param.lastIndexOf('/');
//                return lastDotIndex == -1 ? param.substring(1) : param.substring(lastDotIndex+1);
            case 'L':
                return param.substring(1).replace('/', '.');
            default:
                throw new IllegalArgumentException("Unknown param type " + param);
        }
    }

    private static boolean isPrimitive(String param) {
        switch (param.charAt(0)) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 'V':
                return true;
            default:
                return false;
        }
    }
}

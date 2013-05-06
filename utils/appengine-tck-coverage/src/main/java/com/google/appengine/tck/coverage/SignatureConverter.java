package com.google.appengine.tck.coverage;

import java.util.StringTokenizer;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class SignatureConverter {

    public static String convertMethodSignature(String methodName, String descriptor) {
        if (descriptor.charAt(0) != '(') {
            throw new IllegalArgumentException("Can't convert " + descriptor);
        }
        String params = descriptor.substring(1, descriptor.lastIndexOf(')'));
        StringTokenizer tokenizer = new StringTokenizer(params, ";");
        StringBuilder sb = new StringBuilder();
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
        int i=0;
        StringBuilder appendix = new StringBuilder();
        for (; param.charAt(i) == '['; i++) {
            appendix.append("[]");
        }

        return convertNonArrayParam(param.substring(i)) + appendix;
    }

    private static String convertNonArrayParam(String param) {
        switch (param.charAt(0)) {
            case 'B': return "byte";
            case 'C': return "char";
            case 'D': return "double";
            case 'F': return "float";
            case 'I': return "int";
            case 'J': return "long";
            case 'S': return "short";
            case 'Z': return "boolean";
            case 'V': return "void";
//            case 'L':
//                int lastDotIndex = param.lastIndexOf('/');
//                return lastDotIndex == -1 ? param.substring(1) : param.substring(lastDotIndex+1);
            case 'L': return param.substring(1).replace('/', '.');
            default: throw new IllegalArgumentException("Unknown param type " + param.charAt(0));
        }
    }
}

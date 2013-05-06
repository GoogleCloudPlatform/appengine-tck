package com.google.appengine.tck.coverage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
class Tuple implements Comparable<Tuple> {
    String methodName;
    String methodDesc;

    Tuple(String name, String methodDesc) {
        this.methodName = name;
        this.methodDesc = methodDesc;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public int compareTo(Tuple o) {
        return toString().compareTo(o.toString());
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple tuple = (Tuple) o;

        if (methodDesc.equals(tuple.methodDesc) == false) return false;
        if (methodName.equals(tuple.methodName) == false) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = methodName.hashCode();
        result = 31 * result + methodDesc.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return methodName + "@" + methodDesc;
    }
}

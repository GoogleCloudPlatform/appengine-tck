package com.google.appengine.tck.users.support;

public class ServletAnswer {
    public final String rawAnswer;
    public final String env;
    public final String returnVal;

    public ServletAnswer(String serverAnswer) {
        rawAnswer = serverAnswer;
        int splitPoint = serverAnswer.indexOf(",");
        env = serverAnswer.substring(0, splitPoint);
        returnVal = serverAnswer.substring(splitPoint + 1);
    }

    public String toString() {
        return rawAnswer;
    }
}
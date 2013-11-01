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

package com.google.appengine.tck.oauth.support;

/**
 * Package the UserServiceServlet response.
 * <p/>
 * Wraps the response which is in the form:
 * [Production|Development],[true|false|...]
 */
public class OAuthServletAnswer {
    private final String rawAnswer;
    private final String env;
    private final String returnVal;

    public OAuthServletAnswer(String serverAnswer) {
        rawAnswer = serverAnswer;
        int splitPoint = serverAnswer.indexOf(",");
        env = serverAnswer.substring(0, splitPoint).trim();
        returnVal = serverAnswer.substring(splitPoint + 1).trim();
    }

    public String getReturnVal() {
        return returnVal;
    }

    public String getEnv() {
        return env;
    }

    public boolean isEnvironmentProd() {
        return env.equals("Production");
    }

    public boolean isEnvironmentDev() {
        return env.equals("Development");
    }

    public String getRawAnswer() {
        return rawAnswer;
    }
}

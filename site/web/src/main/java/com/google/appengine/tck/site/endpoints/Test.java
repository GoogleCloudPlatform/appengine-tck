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

package com.google.appengine.tck.site.endpoints;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a test execution in a {@link com.google.appengine.tck.site.endpoints.TestReport}.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:kevin.pollet@serli.com">Kevin Pollet</a>
 */
//TODO use Optional type for error
public class Test implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Returns a {@code Test} instance representing the given test {@link org.json.JSONObject} representation.
     *
     * @param jsonObject the JSON object.
     * @return the {@code Test} instance.
     * @throws java.lang.NullPointerException if {@code jsonObject} is {@code null}.
     */
    public static Test valueOf(JSONObject jsonObject) {
        checkNotNull(jsonObject, "'jsonObject' parameter cannot be null");

        try {
            return new Test(
                    jsonObject.getString("packageName"),
                    jsonObject.getString("className"),
                    jsonObject.getString("methodName"),
                    jsonObject.getString("error")
            );

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String packageName;
    private String className;
    private String methodName;
    private String error;

    /**
     * Constructs an instance of {@code Test}.
     *
     * @param packageName the package name.
     * @param className   the class name.
     * @param methodName  the method name.
     * @throws java.lang.NullPointerException     if {@code packageName}, {@code className} and/or {@code methodName} is {@code null}.
     * @throws java.lang.IllegalArgumentException if {@code packageName}, {@code className} and/or {@code methodName} are empty.
     */
    public Test(String packageName, String className, String methodName) {
        this(packageName, className, methodName, null);
    }

    /**
     * Constructs an instance of {@code Test}.
     *
     * @param packageName the package name.
     * @param className   the class name.
     * @param methodName  the method name.
     * @param error       the execution stacktrace.
     * @throws java.lang.NullPointerException     if {@code packageName}, {@code className} and/or {@code methodName} are {@code null}.
     * @throws java.lang.IllegalArgumentException if {@code packageName}, {@code className} and/or {@code methodName} are empty.
     */
    public Test(String packageName, String className, String methodName, String error) {
        checkNotNull(packageName, "'packageName' parameter cannot be null");
        checkArgument(!packageName.isEmpty(), "'packageName' parameter cannot be empty");
        checkNotNull(className, "'className' parameter cannot be null");
        checkArgument(!className.isEmpty(), "'className' parameter cannot be empty");
        checkNotNull(methodName, "'methodName' parameter cannot be null");
        checkArgument(!methodName.isEmpty(), "'methodName' parameter cannot be empty");

        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
        this.error = error;
    }

    /**
     * For JSON.
     */
    private Test() {
    }

    /**
     * Returns the test package name.
     *
     * @return the test package name, never {@code null}.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the test class name.
     *
     * @return the test class name, never {@code null}.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the test method name.
     *
     * @return the test method name, never {@code null}.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Returns the test execution error.
     *
     * @return test execution error or {@code null} if none.
     */
    public String getError() {
        return error;
    }

    /**
     * Returns a {@link org.json.JSONObject} instance representing {@code this} test instance.
     *
     * @return the {@link org.json.JSONObject} instance.
     */
    public JSONObject asJson() {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("packageName", packageName);
            jsonObject.put("className", className);
            jsonObject.put("methodName", methodName);
            jsonObject.put("error", error);
            return jsonObject;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

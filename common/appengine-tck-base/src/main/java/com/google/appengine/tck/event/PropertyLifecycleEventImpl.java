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

package com.google.appengine.tck.event;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class PropertyLifecycleEventImpl extends AbstractTestLifecycleEventImpl<String> implements PropertyLifecycleEvent {
    private Boolean required;
    private String value;

    PropertyLifecycleEventImpl(Class<?> caller, String propertyName) {
        super(caller, propertyName);
    }

    public String getPropertyName() {
        return getContext();
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Boolean required() {
        return required;
    }

    public void setPropertyValue(String value) {
        this.value = value;
    }

    public String getPropertyValue() {
        return value;
    }
}

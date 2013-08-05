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

package com.google.appengine.tck.base;

import java.util.Properties;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestContext implements Cloneable {
    private boolean subdeployment;
    private String archiveName;

    private String webXmlContent = "<web/>";
    private String webXmlFile;

    private String appEngineWebXmlFile;

    private ContextRoot contextRoot;

    private String compatibilityProperties;
    private Properties properties = new Properties();
    private boolean useSystemProperties;

    private boolean callbacks;

    public TestContext() {
    }

    public TestContext(String archiveName) {
        this.archiveName = archiveName;
    }

    public boolean isSubdeployment() {
        return subdeployment;
    }

    public TestContext setSubdeployment(boolean subdeployment) {
        this.subdeployment = subdeployment;
        return this;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public TestContext setArchiveName(String archiveName) {
        this.archiveName = archiveName;
        return this;
    }

    public String getWebXmlContent() {
        return webXmlContent;
    }

    public TestContext setWebXmlContent(String webXmlContent) {
        this.webXmlContent = webXmlContent;
        return this;
    }

    public String getWebXmlFile() {
        return webXmlFile;
    }

    public TestContext setWebXmlFile(String webXmlFile) {
        this.webXmlFile = webXmlFile;
        return this;
    }

    public String getAppEngineWebXmlFile() {
        return appEngineWebXmlFile;
    }

    public TestContext setAppEngineWebXmlFile(String appEngineWebXmlFile) {
        this.appEngineWebXmlFile = appEngineWebXmlFile;
        return this;
    }

    public ContextRoot getContextRoot() {
        return contextRoot;
    }

    public TestContext setContextRoot(ContextRoot contextRoot) {
        this.contextRoot = contextRoot;
        return this;
    }

    public String getCompatibilityProperties() {
        return compatibilityProperties;
    }

    public TestContext setCompatibilityProperties(String compatibilityProperties) {
        this.compatibilityProperties = compatibilityProperties;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isUseSystemProperties() {
        return useSystemProperties;
    }

    public TestContext setUseSystemProperties(boolean useSystemProperties) {
        this.useSystemProperties = useSystemProperties;
        return this;
    }

    public boolean hasCallbacks() {
        return callbacks;
    }

    public TestContext setCallbacks(boolean callbacks) {
        this.callbacks = callbacks;
        return this;
    }
}
